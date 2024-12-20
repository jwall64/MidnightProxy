package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.TypeReference;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.math.Range;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.CheckType;
import dev.jwall64.midnightlib.requirement.NumberCheck;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ConnectionInt implements ConnectionCheck {

    private final Type type;
    private final Range<Integer> range;

    public ConnectionInt(Type type, Range<Integer> range) {
        this.type = type;
        this.range = range;
    }

    @Override
    public boolean requiresAuth() {
        return type.requireAuth;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Collections.emptyList();
    }

    @Override
    public boolean check(ConnectionContext data) {
        return range.isWithin(type.getter.apply(data));
    }

    @Override
    public Type type() {
        return type;
    }

    public Range<Integer> range() {
        return range;
    }

    public static class Type implements ConnectionCheckType<ConnectionInt> {

        final Function<ConnectionContext, Integer> getter;
        final boolean requireAuth;

        final Serializer<ConnectionInt> serializer;

        public Type(Function<ConnectionContext, Integer> getter, boolean requireAuth) {
            this.getter = getter;
            this.requireAuth = requireAuth;

            this.serializer = Range.INTEGER.fieldOf("value").flatMap(ConnectionInt::range, range -> new ConnectionInt(this, range));
        }

        @Override
        public TypeReference<ConnectionInt> type() {
            return new TypeReference<ConnectionInt>() {};
        }

        @Override
        public Serializer<ConnectionInt> serializer() {
            return serializer;
        }
    }
}
