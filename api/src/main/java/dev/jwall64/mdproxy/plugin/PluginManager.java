package dev.jwall64.mdproxy.plugin;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface PluginManager {

    @Nullable Plugin get(String id);

    <T extends Plugin> @Nullable T get(Class<T> clazz);

    Path configFolder();

}
