package dev.jwall64.mdproxy.requirement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;

public class MetaCheck implements ConnectionCheck {

    private final Type type;
    private final String key;
    private final List<String> values;

    public MetaCheck(Type type, String key, List<String> values) {
        this.type = type;
        this.key = key;
        this.values = values;
    }

    public String key() {
        return key;
    }

    public List<String> validValues() {
        return values;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Collections.emptyList();
    }

    @Override
    public boolean check(ConnectionContext data) {

        String val = data.getMetaProperty(key);
        if(val == null) {
            return false;
        }
        return values.contains(val);
    }

    @Override
    public Type type() {
        return type;
    }

    public static final Type TYPE = new Type();

    public static class Type implements ConnectionCheckType<MetaCheck> {

        final Serializer<MetaCheck> serializer;

        public Type() {
            this.serializer = ObjectSerializer.create(
                    Serializer.STRING.entry("key", MetaCheck::key),
                    Serializer.STRING.listOf().mapToList().or(
                        Serializer.STRING.<List<String>>map(
                            list -> list.isEmpty() ? SerializeResult.success("") : SerializeResult.success(list.getFirst()),
                            str -> SerializeResult.success(List.of(str))
                    )).entry("value", MetaCheck::validValues),
                    (key, values) -> new MetaCheck(this, key, values)
            );
        }

        @Override
        public Serializer<MetaCheck> serializer() {
            return serializer;
        }
    }
}
