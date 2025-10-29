package dev.jwall64.mdproxy.sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.sql.DatabasePreset;
import dev.jwall64.mdcfg.sql.PresetRegistry;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.midnightlib.event.HandlerList;
import dev.jwall64.midnightlib.event.SingletonHandlerList;

public class SQLPlugin implements Plugin {

    private static final ConfigSection DEFAULT_CONFIG =
        new ConfigSection()
            .with("repository", new ConfigSection()
                                    .with("type", "maven")
                                    .with("folder", "config/sql/drivers"))
            .with("presets",
                  new ConfigSection().with(
                      "default",
                      new DatabasePreset("h2", "config/sql/h2", null, null,
                                         null, null, new ConfigSection()),
                      DatabasePreset.SERIALIZER));

    private PresetRegistry registry;

    @Override
    public void initialize(Proxy proxy) {

        Path configFolder =
            proxy.getPluginManager().configFolder().resolve("whitelist");
        try {
            Files.createDirectories(configFolder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create whitelist directory",
                                       e);
        }

        FileWrapper<ConfigObject> config =
            proxy.fileCodecRegistry().findOrCreate(
                ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);
        registry = PresetRegistry.SERIALIZER
                       .deserialize(ConfigContext.INSTANCE, config.getRoot())
                       .getOrThrow();

        registryCreateEvent.invoke(registry);
    }

    public HandlerList<PresetRegistry> registryCreateEvent =
        new SingletonHandlerList<>();

    public PresetRegistry getRegistry() { return registry; }
}
