package dev.jwall64.mdproxy.whitelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mcore.MidnightCoreAPI;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
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

        Path configFolder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("whitelist");
        if(!Files.isDirectory(configFolder)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException e) {
                throw new RuntimeException("Could not create config directory", e);
            }
        }

        config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);
        ConnectionCheckType.REGISTRY.tryRegister("whitelist", WhitelistCheck.TYPE);

        reload();
    }

    public Registry<String, Whitelist> getLists() {
        return lists;
    }

    public void reload() {

        lists.clear();
        config.load();

        ConfigSection listSec = config.getRoot().asSection().getSection("lists");
        for(String key : listSec.getKeys()) {

            SerializeResult<Whitelist> out = Whitelist.SERIALIZER.deserialize(ConfigContext.INSTANCE, listSec.get(key));
            if(!out.isComplete()) {
                LOGGER.warn("Unable to deserialize a whitelist with key " + key + "! " + out.getError());
            }

            lists.register(key, out.getOrThrow());
        }
    }

}
