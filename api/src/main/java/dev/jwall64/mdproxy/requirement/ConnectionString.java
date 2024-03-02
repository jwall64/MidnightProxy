package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.StringCheck;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class ConnectionString extends StringCheck<ConnectionContext> implements ConnectionCheck {

    private final boolean requireAuth;

    public ConnectionString(Function<ConnectionContext, String> getter, Collection<String> coll, boolean requireAuth) {
        super(getter, coll);
        this.requireAuth = requireAuth;
    }

    @Override
    public boolean requiresAuth() {
        return requireAuth;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Collections.emptyList();
    }


    public static ConnectionCheckType type(Function<ConnectionContext, String> getter, boolean requireAuth) {
        return new ConnectionCheckType() {
            @Override
            protected <O> SerializeResult<ConnectionCheck> deserializeCheck(SerializeContext<O> ctx, O value) {
                return StringCheck.STRING_SERIALIZER.fieldOf("value").deserialize(ctx, value).flatMap(str -> new ConnectionString(getter, str, requireAuth));
            }
        };
    }



}
