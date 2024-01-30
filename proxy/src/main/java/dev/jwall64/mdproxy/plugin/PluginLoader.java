package dev.jwall64.mdproxy.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.codec.DecodeException;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeException;
import dev.jwall64.mdproxy.Proxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("PluginLoader");
    private final File pluginFolder;

    public PluginLoader(File pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void loadAll(Proxy proxy) {

        File[] files = pluginFolder.listFiles();

        if(files == null) return;

        for(File f : files) {
            if(f.getName().endsWith(".jar")) {
                tryLoad(f, proxy);
            }
        }
    }

    private void tryLoad(File f, Proxy proxy) {


        try {

            ClassLoader loader = new URLClassLoader(new URL[] { f.toURI().toURL() }, PluginLoader.class.getClassLoader());

            PluginInfo info;
            try (InputStream pluginDesc = loader.getResourceAsStream("plugin.json")) {

                if(pluginDesc == null) {
                    LOGGER.warn("File " + f.getName() + " did not have a plugin description!");
                    return;
                }

                info = JSONCodec.minified().decode(ConfigContext.INSTANCE, PluginInfo.SERIALIZER, pluginDesc).getOrThrow();
            } catch (DecodeException | SerializeException ex) {
                LOGGER.error("An error occurred while reading a plugin description for {}!", f.getName(), ex);
                return;
            }

            Class<?> main;
            try {
                main = loader.loadClass(info.mainClass());
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Plugin main class {} was not found in the archive!", info.mainClass(), ex);
                return;
            }

            Plugin plugin;
            try {
                plugin = (Plugin) main.getConstructor().newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ex) {

                LOGGER.error("Plugin main class {} did not have a public default constructor!", info.mainClass(), ex);
                return;

            } catch (ClassCastException ex) {
                LOGGER.error("Plugin main class {} does not implement Plugin!", info.mainClass(), ex);
                return;
            }

            try {
                plugin.initialize(proxy);
            } catch (Exception ex) {
                LOGGER.error("An exception occurred while initializing {} version {}", info.name(), info.version(), ex);
            }

        } catch (IOException ex) {

            LOGGER.error("An error occurred while loading a plugin!", ex);

        }


    }

}
