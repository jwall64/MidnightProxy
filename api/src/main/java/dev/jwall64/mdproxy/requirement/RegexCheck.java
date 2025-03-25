package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.CheckType;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RegexCheck implements ConnectionCheck {

    private final Type type;
    private final Pattern pattern;

    public RegexCheck(Type type, Pattern pattern) {
        this.type = type;
        this.pattern = pattern;
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
        return pattern.matcher(type.getter.apply(data)).matches();
    }

    @Override
    public CheckType<ConnectionContext, ?> type() {
        return type;
    }

    public Pattern pattern() {
        return pattern;
    }

    public static class Type implements ConnectionCheckType<RegexCheck> {

        final Function<ConnectionContext, String> getter;
        final boolean requireAuth;

        final Serializer<RegexCheck> serializer;

        public Type(Function<ConnectionContext, String> getter, boolean requireAuth) {
            this.getter = getter;
            this.requireAuth = requireAuth;

            this.serializer = Serializer.STRING
                    .flatMap(Pattern::pattern, Pattern::compile)
                    .fieldOf("value")
                    .flatMap(RegexCheck::pattern, pattern -> new RegexCheck(this, pattern));
        }

        @Override
        public Serializer<RegexCheck> serializer() {
            return serializer;
        }
    }

}
