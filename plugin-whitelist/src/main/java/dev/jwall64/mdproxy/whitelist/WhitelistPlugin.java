package dev.jwall64.mdproxy.whitelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.midnightlib.registry.Registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WhitelistPlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("WhitelistPlugin");
    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("lists", new ConfigSection());
    private final Registry<String, Whitelist> lists = Registry.createStringRegistry();
    private FileWrapper<ConfigObject> config;

    @Override
    public void initialize(Proxy proxy) {

        Path configFolder = proxy.getPluginManager().configFolder().resolve("whitelist");
        try { Files.createDirectories(configFolder); } catch (IOException e) {
            throw new RuntimeException("Could not create config directory", e);
        }

        config = proxy.fileCodecRegistry().findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);
        ConnectionCheckType.REGISTRY.tryRegister("whitelist", WhitelistCheck.TYPE);

        proxy.getCommands().register("whitelist", (sender, args) -> {
            if(args.length <= 1) {
                sender.sendMessage("Usage: whitelist [reload]");
                return;
            }

            if(args[1].equals("reload")) {
                reload();
                sender.sendMessage("Whitelists reloaded");
            } else {
                sender.sendMessage("Usage: whitelist [reload]");
            }
        });

        reload();
    }

    public Registry<String, Whitelist> getLists() {
        return lists;
    }

    public void reload() {

        lists.forEach(Whitelist::invalidate);

        lists.clear();
        config.load();

        Whitelist.SERIALIZER
                .filteredMapOf((key, err) -> LOGGER.error("Unable to deserialize a whitelist with key {}! {}", key, err))
                .fieldOf("lists")
                .deserialize(ConfigContext.INSTANCE, config.getRoot())
                .getOrThrow()
                .forEach(lists::register);
    }

}
