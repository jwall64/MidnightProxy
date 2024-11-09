package dev.jwall64.mdproxy.plugin;

public record LoadedPlugin(PluginClassLoader loader, PluginInfo info, Plugin plugin) {

}
