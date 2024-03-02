package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.StringCheck;

import java.util.Collection;
import java.util.Set;

public class Cookie implements ConnectionCheck {
    private final Identifier cookie;
    private final Set<String> values;

    public Cookie(Identifier cookie, Collection<String> values) {
        this.cookie = cookie;
        this.values = Set.copyOf(values);
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Set.of(cookie);
    }

    @Override
    public boolean check(ConnectionContext ctx) {
        byte[] c = ctx.getConnection().getCookie(cookie);
        String str = c == null ? "" : new String(c);
        return values.contains(str);
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx) {
        return SERIALIZER.serialize(ctx, this);
    }

    public static final Serializer<Cookie> SERIALIZER = ObjectSerializer.create(
            Identifier.serializer("minecraft").entry("cookie", c -> c.cookie),
            StringCheck.STRING_SERIALIZER.entry("value", c -> c.values),
            Cookie::new
    );

    public static final ConnectionCheckType TYPE = new ConnectionCheckType() {
        @Override
        protected <O> SerializeResult<ConnectionCheck> deserializeCheck(SerializeContext<O> ctx, O value) {
            return SERIALIZER.deserialize(ctx, value).flatMap(cook -> cook);
        }
    };
}
