package dev.jwall64.mdproxy.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mcore.MidnightCoreAPI;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;

import java.io.File;

public class JWTPlugin implements Plugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("JWTPlugin");
    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("key_store_path", "keystore");

    private KeyStore keyStore;

    @Override
    public void initialize(Proxy proxy) {

        File configFolder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("jwt").toFile();
        if(!configFolder.isDirectory() && !configFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }

        FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);

        File keyStoreDir = new File(configFolder, config.getRoot().asSection().getString("key_store_path"));
        if(!keyStoreDir.isDirectory() && !keyStoreDir.mkdirs()) {
            throw new IllegalStateException("Unable to create key store directory!");
        }
        keyStore = new FileKeyStore(keyStoreDir, FileKeyStore.DEFAULT_TYPES);

        proxy.getCommands().register("jwt", new JWTCommand());
        ConnectionCheckType.REGISTRY.register("jwt", JWTCheck.TYPE);
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }
}
