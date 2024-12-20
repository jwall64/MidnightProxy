package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.TypeReference;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.CheckType;

import java.util.*;

public class CookieCheck implements ConnectionCheck {

    private final Type type;
    private final Identifier cookie;
    private final Set<String> values;

    public CookieCheck(Type type, Identifier cookie, Set<String> values) {
        this.type = type;
        this.cookie = cookie;
        this.values = values;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Collections.singleton(cookie);
    }

    @Override
    public boolean check(ConnectionContext data) {
        byte[] c = data.getConnection().getCookie(cookie);
        String str = c == null ? "" : new String(c);
        return values.contains(str);
    }

    @Override
    public Type type() {
        return type;
    }

    public Identifier cookie() {
        return cookie;
    }

    public Set<String> values() {
        return values;
    }

    public static final Type TYPE = new Type();

    public static class Type implements ConnectionCheckType<CookieCheck> {

        final Serializer<CookieCheck> serializer;

        public Type() {
            this.serializer = ObjectSerializer.create(
                    Identifier.serializer("minecraft").entry("cookie", CookieCheck::cookie),
                    Serializer.STRING.listOf().mapToSet()
                            .or(Serializer.STRING.map(set -> SerializeResult.ofNullable(set.stream().findFirst().orElse(null)), str -> SerializeResult.success(Collections.singleton(str))))
                            .entry("values", CookieCheck::values),
                    (cookie, values) -> new CookieCheck(this, cookie, values));
        }

        @Override
        public TypeReference<CookieCheck> type() {
            return new TypeReference<CookieCheck>() {};
        }

        @Override
        public Serializer<CookieCheck> serializer() {
            return serializer;
        }
    }
}
