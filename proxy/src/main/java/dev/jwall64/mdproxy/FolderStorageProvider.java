package dev.jwall64.mdproxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

public class FolderStorageProvider implements StorageProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderStorageProvider.class);

    private Registry<String, Backend> backends = Registry.createStringRegistry();
    private final List<StatusEntry> statusEntries = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private final List<AuthRoute> authRoutes = new ArrayList<>();

    private final Path rootFolder;

    public FolderStorageProvider(Path rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public Registry<String, Backend> backends() {
        return backends;
    }

    @Override
    public List<Route> routes() { 
        return routes;
    }

    @Override
    public List<StatusEntry> statusEntries() {
        return statusEntries;
    }

    @Override
    public List<AuthRoute> authRoutes() {
        return authRoutes;
    }

    @Override
    public void reload(Proxy proxy) {

        Registry<String, Backend> backends = Registry.createStringRegistry();

        try { 
            Path backendFolder = rootFolder.resolve("backends");
            if(Files.isDirectory(backendFolder)) Files.list(backendFolder).forEach(file -> {            
                if(file.getFileName().toString().endsWith(".json")) {
                    try(InputStream is = Files.newInputStream(file)) {
                        ConfigObject obj = JSONCodec.loadConfig(is);
                        Backend.SERIALIZER.filteredMapOf(
                                (key, err) -> LOGGER.warn("Could not deserialize a backend with id {}!", key, err)
                        ).deserialize(ConfigContext.INSTANCE, obj)
                                .getOrThrow()
                                .forEach(backends::register);
                    } catch(IOException ex) {
                        throw new RuntimeException("Failed to read a backend file!", ex);
                    }
                }
            });
            this.backends = backends.freeze();
            LOGGER.info("Loaded {} backends", this.backends.getSize());

            this.statusEntries.clear();
            Path statusFolder = rootFolder.resolve("status");
            if(Files.isDirectory(statusFolder)) Files.list(statusFolder).forEach(file -> {            
                if(file.getFileName().toString().endsWith(".json")) {
                    try(InputStream is = Files.newInputStream(file)) {
                        ConfigObject obj = JSONCodec.loadConfig(is);
                        StatusEntry.SERIALIZER.filteredListOf(
                                (err) -> LOGGER.warn("Could not deserialize a status entry!", err)
                        ).deserialize(ConfigContext.INSTANCE, obj)
                                .getOrThrow()
                                .forEach(statusEntries::add);
                    } catch(IOException ex) {
                        throw new RuntimeException("Failed to read a status entry file!", ex);
                    }
                }
            });
            LOGGER.info("Loaded {} status entries", this.statusEntries.size());

            this.routes.clear();
            Path routeFolder = rootFolder.resolve("routes");
            if(Files.isDirectory(routeFolder)) Files.list(routeFolder).forEach(file -> {            
                if(file.getFileName().toString().endsWith(".json")) {
                    try(InputStream is = Files.newInputStream(file)) {
                        ConfigObject obj = JSONCodec.loadConfig(is);
                        Route.SERIALIZER.filteredListOf(
                                (err) -> LOGGER.warn("Could not deserialize a route!", err)
                        ).deserialize(ConfigContext.INSTANCE, obj)
                                .getOrThrow()
                                .forEach(routes::add);
                    } catch(IOException ex) {
                        throw new RuntimeException("Failed to read a route file!", ex);
                    }
                }
            });
            LOGGER.info("Loaded {} routes", this.routes.size());

            this.authRoutes.clear();
            Path authRouteFolder = rootFolder.resolve("auth_routes");
            if(Files.isDirectory(authRouteFolder)) Files.list(authRouteFolder).forEach(file -> {            
                if(file.getFileName().toString().endsWith(".json")) {
                    try(InputStream is = Files.newInputStream(file)) {
                        ConfigObject obj = JSONCodec.loadConfig(is);
                        AuthRoute.serializer(proxy).filteredListOf(
                                (err) -> LOGGER.warn("Could not deserialize a route!", err)
                        ).deserialize(ConfigContext.INSTANCE, obj)
                                .getOrThrow()
                                .forEach(authRoutes::add);
                    } catch(IOException ex) {
                        throw new RuntimeException("Failed to read an auth route file!", ex);
                    }
                }
            });
            LOGGER.info("Loaded {} auth routes", this.authRoutes.size());

        } catch(IOException ex) { 
            throw new RuntimeException(ex);
        }

    }


    public static final Serializer<FolderStorageProvider> SERIALIZER = ObjectSerializer.create(
        Serializer.PATH.<FolderStorageProvider>entry("folder", fsp -> fsp.rootFolder).orElse(Path.of(System.getProperty("user.dir")).resolve("storage")),
        FolderStorageProvider::new
    );



}
