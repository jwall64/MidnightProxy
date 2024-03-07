package dev.jwall64.mdproxy.whitelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mcore.MidnightCoreAPI;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.midnightlib.registry.StringRegistry;

import java.io.File;

public class WhitelistPlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("WhitelistPlugin");
    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("lists", new ConfigSection());
    private final StringRegistry<Whitelist> lists = new StringRegistry<>();
    private FileWrapper<ConfigObject> config;

    @Override
    public void initialize(Proxy proxy) {

        File configFolder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("jwt").toFile();
        if(!configFolder.isDirectory() && !configFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }
        config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);

        ConnectionCheckType.REGISTRY.register("whitelist", WhitelistCheck.TYPE);

        reload();
    }

    public StringRegistry<Whitelist> getLists() {
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
