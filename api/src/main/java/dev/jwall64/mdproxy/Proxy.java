package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdproxy.command.CommandExecutor;
import dev.jwall64.mdproxy.plugin.PluginManager;
import dev.jwall64.midnightlib.registry.RegistryBase;
import dev.jwall64.midnightlib.registry.StringRegistry;

import java.util.List;

public interface Proxy {

    int getPort();

    boolean isOnlineMode();

    boolean requiresAuth();

    ConfigSection getConfig();

    void shutdown();

    void reload();

    RegistryBase<String, Backend> getBackends();

    List<StatusEntry> getStatusEntries();
    List<Route> getRoutes();

    IconCache getIconCache();

    StringRegistry<CommandExecutor> getCommands();

    int getOnlinePlayers();

    int getPlayerLimit();

    boolean bypassesPlayerLimit(PlayerInfo info);

    PluginManager getPluginManager();

}
