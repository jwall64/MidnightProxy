package dev.jwall64.mdproxy;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigList;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileCodecRegistry;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger("Main");


    private static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("port", 25565)
            .with("haproxy_protocol", false)
            .with("backends", new ConfigList())
            .with("online_mode", true)
            .with("reconnect_threads", 4)
            .with("reconnect_timeout", 3000)
            .with("backend_timeout", 5000)
            .with("client_timeout", 15000);


    public static void main(String[] args) {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        FileCodecRegistry reg = new FileCodecRegistry();
        reg.registerFileCodec(JSONCodec.fileCodec());

        File configFile = new File("config.json");
        FileWrapper<ConfigObject> config = new FileWrapper<>(ConfigContext.INSTANCE, JSONCodec.fileCodec(), configFile, StandardCharsets.UTF_8, DEFAULT_CONFIG);
        if(!configFile.exists()) {
            config.setRoot(DEFAULT_CONFIG);
            config.save();
        }

        ProxyServer ps = new ProxyServer(config);

        try {
            ps.startup();
        } catch (Exception ex) {
            LOGGER.error("An exception occurred while running the server!", ex);
        }
    }

}
