package dev.jwall64.mdproxy.resourcepack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.codec.FileCodec;
import dev.jwall64.mdcfg.codec.FileCodecRegistry;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.ConfigContext;

public interface ResourcePackRegistry extends Comparable<ResourcePackRegistry> {

    static final Logger LOGGER = LoggerFactory.getLogger(ResourcePackRegistry.class);

    Stream<ResourcePackEntry> entries();

    int priority();

    default void reload(Executor executor) {
        entries().forEach(rpe -> rpe.reload(executor));
    }

    @Override
    default int compareTo(ResourcePackRegistry other) {
        return Integer.compare(this.priority(), other.priority());
    }

    public record Direct(List<ResourcePackEntry> entriesList, int priority) implements ResourcePackRegistry {
        @Override
        public Stream<ResourcePackEntry> entries() {
            return entriesList.stream();
        }
    }

    public class ConfigBased implements ResourcePackRegistry {

        private final FileWrapper<ConfigObject> config;
        private final int priority;
        private List<ResourcePackEntry> cachedEntries;

        public ConfigBased(FileWrapper<ConfigObject> config, int priority) {
            this.config = config;
            this.priority = priority;

            reload(Runnable::run);
        }

        @Override
        public void reload(Executor executor) {
            config.load();
            cachedEntries = ResourcePackEntry.SERIALIZER
                .listOf().mapToList()
                .deserialize(ConfigContext.INSTANCE, config.getRoot())
                .getOrThrow();

            ResourcePackRegistry.super.reload(executor);
        }

        @Override
        public Stream<ResourcePackEntry> entries() {
            return cachedEntries.stream();
        }

        @Override
        public int priority() {
            return priority;
        }

    }

    public class FileBased implements ResourcePackRegistry {

        private final Path path;
        private final int priority;
        private final FileCodecRegistry codecRegistry;

        private List<ResourcePackEntry> cachedEntries = null;

        public FileBased(Path path, int priority, FileCodecRegistry codecRegistry) {
            this.path = path;
            this.priority = priority;
            this.codecRegistry = codecRegistry;

            reload(Runnable::run);
        }

        @Override
        public void reload(Executor executor) {
            cachedEntries = new ArrayList<>();
            if(!Files.isDirectory(path)) {
                return;
            }
            try {
                Files.list(path)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {

                        FileCodec codec = codecRegistry.forFile(path);
                        if(codec == null) {
                            return;
                        }
                        try {
                            ConfigObject obj = codec.loadFromFile(ConfigContext.INSTANCE, file, StandardCharsets.UTF_8);
                            ResourcePackEntry.SERIALIZER.deserialize(ConfigContext.INSTANCE, obj).get().ifPresent(cachedEntries::add);
                        } catch (IOException e) {
                            // Log error and continue
                            LOGGER.error("Failed to load resource pack entry from file: " + file, e);
                        }
                });
            } catch (IOException e) {
                // Log error
                LOGGER.error("Failed to list pack files!", e);
            }

            ResourcePackRegistry.super.reload(executor);
        }

        @Override
        public Stream<ResourcePackEntry> entries() {
            return cachedEntries.stream();
        }

        @Override
        public int priority() {
            return priority;
        }

    }

}
