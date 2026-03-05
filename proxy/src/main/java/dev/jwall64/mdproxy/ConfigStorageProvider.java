package dev.jwall64.mdproxy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.mdcfg.serializer.ConfigContext;

public class ConfigStorageProvider implements StorageProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStorageProvider.class);

    private Registry<String, Backend> backends = Registry.createStringRegistry();
    private final List<StatusEntry> statusEntries = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    private final List<AuthRoute> authRoutes = new ArrayList<>();

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

        ConfigSection config = proxy.getConfig();

        Registry<String, Backend> backends = Registry.createStringRegistry();
        if(config.hasSection("backends")) {
            Backend.SERIALIZER.filteredMapOf(
                    (key, err) -> LOGGER.warn("Could not deserialize a backend with id {}!", key, err)
            ).deserialize(ConfigContext.INSTANCE, config.getSection("backends"))
                    .getOrThrow()
                    .forEach(backends::register);
        }
        this.backends = backends.freeze();

        this.statusEntries.clear();
        if(config.hasList("status")) {
            this.statusEntries.addAll(config.getListFiltered("status", StatusEntry.SERIALIZER, ex -> {
                LOGGER.warn("Unable to decode a status entry!", ex);
            }));
        }

        this.routes.clear();
        if(config.hasList("routes")) {
            this.routes.addAll(config.getListFiltered("routes", Route.SERIALIZER, ex -> {
                LOGGER.warn("Unable to decode a route!", ex);
            }));
        }

        this.authRoutes.clear();
        if(config.hasList("auth_routes")) {
            this.authRoutes.addAll(config.getListFiltered("auth_routes", AuthRoute.serializer(proxy), ex -> {
                LOGGER.warn("Unable to decode an auth route!", ex);
            }));
        }

    }

}
