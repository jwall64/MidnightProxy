package dev.jwall64.mdproxy.smi;

import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.registry.Registry;
import dev.jwall64.smi.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessengerPlugin implements Plugin {

    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("messengers", new ConfigSection());

    private MessengerManagerImpl manager;

    @Override
    public void initialize(Proxy proxy) {

        Path configFolder = proxy.getPluginManager().configFolder().resolve("messenger");
        try { Files.createDirectories(configFolder); } catch (IOException e) {
            throw new RuntimeException("Could not create messenger directory", e);
        }

        FileWrapper<ConfigObject> config = proxy.fileCodecRegistry().findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);
        manager = new MessengerManagerImpl(REGISTRY);
        manager.loadAll(config.getRoot().asSection().getSection("messengers"));

        if(MessengerManager.Holder.gInstance == null) {
            MessengerManagerImpl.register(manager);
        }

        proxy.shutdownEvent().register(this, prx -> {

            manager.clear();
            if(MessengerManager.Holder.gInstance == manager) {
                MessengerManager.Holder.gInstance = null;
            }
        });
    }

    public Messenger getMessenger(String name) {
        return manager.messenger(name);
    }

    private static final Registry<Identifier, MessengerType<?>> REGISTRY = Registry.create("smi");

    static {
        AmqpMessenger.register(REGISTRY);
    }

}
