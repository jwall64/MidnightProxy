package dev.jwall64.mdproxy;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.registry.Identifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public record RegistryData(Identifier key, Map<Identifier, Optional<ConfigObject>> entries) {
    
    public static Builder builder(String key) {
        return new Builder(key);
    }

    public static Builder builder(Identifier key) {
        return new Builder(key);
    }

    public static class Builder {
        private final Identifier key;
        private final ImmutableMap.Builder<Identifier, Optional<ConfigObject>> entries;

        public Builder(String key) {
            this(Identifier.parseOrDefault(key, "minecraft"));
        }

        public Builder(Identifier key) {
            this.key = key;
            this.entries = new ImmutableMap.Builder<>();
        }

        public Builder add(String id) {
            return add(Identifier.parseOrDefault(id, "minecraft"));
        }

        public Builder add(String... ids) {
            for(String s : ids) {
                add(s);
            }
            return this;
        }

        public Builder add(Identifier id) {
            entries.put(id, Optional.empty());
            return this;
        }

        public Builder add(String id, ConfigObject data) {
            return add(Identifier.parseOrDefault(id, "minecraft"), data);
        }

        public Builder add(Identifier id, ConfigObject data) {
            entries.put(id, Optional.ofNullable(data));
            return this;
        }

        public RegistryData build() {
            return new RegistryData(key, entries.build());
        }
    }


    public record TagList(Identifier key, List<Integer> entries) { 

        public static Builder builder(String key) {
            return new Builder(key);
        }

        public static Builder builder(Identifier key) {
            return new Builder(key);
        }

        public static class Builder {
            
            private final Identifier key;
            private final ImmutableList.Builder<Integer> entries;

            public Builder(String key) {
                this(Identifier.parseOrDefault(key, "minecraft"));
            }

            public Builder(Identifier key) {
                this.key = key;
                this.entries = new ImmutableList.Builder<>();
            }

            public Builder add(int id) {
                entries.add(id);
                return this;
            }

            public TagList build() {
                return new TagList(key, entries.build());
            }
        }
    }
}
