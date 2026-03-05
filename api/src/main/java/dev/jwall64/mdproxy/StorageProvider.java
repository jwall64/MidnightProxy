package dev.jwall64.mdproxy;

import java.util.List;

import dev.jwall64.mdcfg.registry.Registry;

public interface StorageProvider {

    Registry<String, Backend> backends();

    List<Route> routes();

    List<StatusEntry> statusEntries();

    List<AuthRoute> authRoutes();

    void reload(Proxy proxy);

}
