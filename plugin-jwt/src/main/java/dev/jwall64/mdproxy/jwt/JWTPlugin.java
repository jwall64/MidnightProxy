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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JWTPlugin implements Plugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("JWTPlugin");
    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("key_store_path", "keystore");

    private KeyStore keyStore;

    @Override
    public void initialize(Proxy proxy) {

        Path configFolder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("jwt");
        try { Files.createDirectories(configFolder); } catch (IOException e) {
            throw new RuntimeException("Could not create lang directory", e);
        }

        FileWrapper<ConfigObject> config = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);

        Path keyStoreDir = configFolder.resolve(config.getRoot().asSection().getString("key_store_path"));
        try { Files.createDirectories(keyStoreDir); } catch (IOException e) {
            throw new RuntimeException("Could not create key store directory", e);
        }
        keyStore = new FileKeyStore(keyStoreDir, FileKeyStore.DEFAULT_TYPES);

        proxy.getCommands().register("jwt", new JWTCommand());
        ConnectionCheckType.REGISTRY.tryRegister("jwt", JWTCheck.TYPE);
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }
}
