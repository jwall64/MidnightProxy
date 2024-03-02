package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.math.Range;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.Check;
import dev.jwall64.midnightlib.requirement.NumberCheck;
import dev.jwall64.midnightlib.requirement.StringCheck;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class ConnectionInt extends NumberCheck<ConnectionContext, Integer> implements ConnectionCheck{

    private final boolean requireAuth;

    public ConnectionInt(Function<ConnectionContext, Integer> getter, Range<Integer> valid, boolean requireAuth) {
        super(Range.INTEGER, getter, valid);
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

    public static ConnectionCheckType type(Function<ConnectionContext, Integer> getter, boolean requireAuth) {
        return new ConnectionCheckType() {
            @Override
            protected <O> SerializeResult<ConnectionCheck> deserializeCheck(SerializeContext<O> ctx, O value) {
                return Range.INTEGER.fieldOf("value").deserialize(ctx, value).flatMap(str -> new ConnectionInt(getter, str, requireAuth));
            }
        };
    }
}
