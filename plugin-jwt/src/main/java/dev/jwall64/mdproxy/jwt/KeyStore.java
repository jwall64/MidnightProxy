package dev.jwall64.mdproxy.jwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nullable;

import dev.jwall64.jwt.JsonWebKey;
import dev.jwall64.jwt.KeySupplier;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.codec.FileCodecRegistry;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;

public interface KeyStore {

    @Nullable
    JsonWebKey get(String kid);

    void put(String kid, JsonWebKey key);

    void clear(String kid);

    default KeySupplier supplier(String kid) {
        return header -> get(kid);
    }


    public class Folder implements KeyStore {
        
        private final Path folder;
        private final FileCodecRegistry registry;

        public Folder(Path folder, FileCodecRegistry registry) {
            this.folder = folder;
            this.registry = registry;
        }

        @Override
        public JsonWebKey get(String kid) {

            FileWrapper<ConfigObject> wrapper = registry.find(ConfigContext.INSTANCE, kid, folder);
            if(wrapper == null) return null;

            return JsonWebKey.SERIALIZER.deserialize(ConfigContext.INSTANCE, wrapper.getRoot()).get().orElse(null);
        }

        @Override
        public void put(String kid, JsonWebKey key) {

            FileWrapper<ConfigObject> wrapper = registry.findOrCreate(ConfigContext.INSTANCE, kid, folder); 
            wrapper.setRoot(JsonWebKey.SERIALIZER.serialize(ConfigContext.INSTANCE, key).getOrThrow());
            wrapper.save();
        }

        @Override
        public void clear(String kid) {
            
            FileWrapper<ConfigObject> wrapper = registry.find(ConfigContext.INSTANCE, kid, folder);
            if(wrapper == null) return;

            try { Files.delete(wrapper.getPath()); } catch(IOException ex) { throw new RuntimeException(ex); }
        }

    }

}
