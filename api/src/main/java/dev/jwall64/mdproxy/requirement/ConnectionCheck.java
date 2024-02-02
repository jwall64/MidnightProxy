package dev.jwall64.mdproxy.requirement;

import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.function.Predicate;

public abstract class ConnectionCheck implements Predicate<ConnectionContext> {

    private final boolean requiresAuth;
    private final Collection<Identifier> cookies;

    protected ConnectionCheck(boolean requiresAuth, Collection<Identifier> cookies) {
        this.requiresAuth = requiresAuth;
        this.cookies = cookies;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public Collection<Identifier> getRequiredCookies() {
        return cookies;
    }

    public static <T extends ConnectionCheck> Serializer<ConnectionCheck> forClass(Class<T> clazz, Serializer<T> in) {
        return new Serializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, ConnectionCheck req) {
                if(req.getClass() != clazz && !clazz.isAssignableFrom(req.getClass())) {
                    return SerializeResult.failure(req + " is not an instance of " + clazz.getName());
                }
                return in.serialize(ctx, clazz.cast(req));
            }

            @Override
            public <O> SerializeResult<ConnectionCheck> deserialize(SerializeContext<O> ctx, O o) {
                return in.deserialize(ctx, o).flatMap(req -> req);
            }
        };
    }
}
