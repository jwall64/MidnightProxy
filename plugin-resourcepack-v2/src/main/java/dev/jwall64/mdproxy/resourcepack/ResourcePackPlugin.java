package dev.jwall64.mdproxy.resourcepack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.ResourcePack;
import dev.jwall64.mdproxy.TestResult;
import dev.jwall64.mdproxy.plugin.Plugin;


public class ResourcePackPlugin implements Plugin {

    private static final Identifier REMOVE_ID = new Identifier("mdp", "clear_packs");
    private static final Logger logger = LoggerFactory.getLogger(ResourcePackPlugin.class);

    private Queue<ResourcePackRegistry> registries = new PriorityQueue<>();
    private ExecutorService reloadExecutor = new ThreadPoolExecutor(1, 4, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(16));


    @Override
    public void initialize(Proxy proxy) {

        HashProvider.TYPES.register("known", HashProvider.Known.TYPE);
        HashProvider.TYPES.register("query", HashProvider.Query.TYPE);
        HashProvider.TYPES.register("header", HashProvider.Header.TYPE);
        HashProvider.TYPES.register("compute", HashProvider.Compute.TYPE);

        proxy.getCommands().register("rp", (sender, args) -> {
            if(args.length == 0) {
                sender.sendMessage("Usage: rp reload");
                return;
            }
            if(args[0].equalsIgnoreCase("reload")) {
                registries.forEach(rpr -> rpr.reload(reloadExecutor));
                sender.sendMessage("Reloaded resource pack registries!");
            } else {
                sender.sendMessage("Unknown subcommand!");
            }
        });

        proxy.shutdownEvent().register(this, (_proxy) -> {
            reloadExecutor.close();
        });

        proxy.clientConnectEvent().register(this, connection -> {
            connection.events().enterConfiguration().register(this, preConnect -> {

                System.out.println("EnterConfiguration event for client " + preConnect.connection().username());

                List<CompletableFuture<?>> futures = new ArrayList<>();
                Set<UUID> packIds = new HashSet<>();

                registries.stream().flatMap(registry -> registry.entries()).forEach(entry -> {

                    if(packIds.contains(entry.uuid())) {
                        logger.warn("Duplicate resource pack with id {} found, skipping!", entry.uuid());
                        return;
                    }

                    logger.info("Testing resource pack {} for client {}", entry.uuid(), preConnect.connection().username());

                    if(entry.requirement() == null || entry.requirement().test(preConnect.context()) == TestResult.PASS) {

                            logger.info("Requirement for resource pack {} passed, sending to client {}", entry.uuid(), preConnect.connection().username());

                            ResourcePack pack = new ResourcePack(entry.uuid(), entry.url().resolve(entry).toString(), entry.hash().getHashNow(entry, Runnable::run, 5000L), entry.required(), entry.message());
                            futures.add(preConnect.connection().sendResourcePack(pack));
                            packIds.add(entry.uuid());
                        }
                });

                futures.add(preConnect.connection().requestCookie(REMOVE_ID).thenAccept(data -> {
                    if (data != null) {
                        List<UUID> toRemove = Arrays.stream(new String(data).split(";"))
                                .filter(str -> !str.isEmpty())
                                .map(UUID::fromString)
                                .toList();
                        for (UUID packId : toRemove) {
                            if (!packIds.contains(packId)) {
                                preConnect.connection().removeResourcePack(packId);
                            }
                        }
                    }
                }));

                logger.info("Waiting on {} futures", futures.size());
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    logger.info("Finished sending resource packs to client {}", preConnect.connection().username());
                }).join();
            });
        });

        // Default registry
        Path configFolder = proxy.getPluginManager().configFolder().resolve("resource_pack");
        try {
            Files.createDirectories(configFolder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create resource pack directory", e);
        }

        this.registries.add(new ResourcePackRegistry.ConfigBased(
            proxy.fileCodecRegistry().findOrCreate(ConfigContext.INSTANCE, "packs", configFolder), 0)
        );

        this.registries.add(new ResourcePackRegistry.FileBased(
            configFolder.resolve("packs"), 100, proxy.fileCodecRegistry())
        );

    }

    public void pushRegistry(ResourcePackRegistry registry) {
        registries.add(registry);
    }

}
