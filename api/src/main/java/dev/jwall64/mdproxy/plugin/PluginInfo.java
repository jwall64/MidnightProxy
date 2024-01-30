package dev.jwall64.mdproxy.plugin;

import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.midnightlib.Version;

public record PluginInfo(String name, Version version, String mainClass) {

    public static final Serializer<PluginInfo> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("name", PluginInfo::name),
            Version.SERIALIZER.entry("version", PluginInfo::version),
            Serializer.STRING.entry("main", PluginInfo::mainClass),
            PluginInfo::new
    );

}
