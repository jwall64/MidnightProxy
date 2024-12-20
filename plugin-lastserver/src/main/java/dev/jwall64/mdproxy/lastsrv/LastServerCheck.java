package dev.jwall64.mdproxy.lastsrv;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.TypeReference;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.requirement.ConnectionCheck;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.CheckType;

import java.util.Collection;
import java.util.Collections;

public class LastServerCheck implements ConnectionCheck {

    private final boolean requireAuth;

    public LastServerCheck(boolean requireAuth) {
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

    @Override
    public boolean check(ConnectionContext ctx) {

        LastServerPlugin pl = ctx.getProxy().getPluginManager().get(LastServerPlugin.class);
        if(pl == null) {
            LastServerPlugin.LOGGER.warn("Attempt to use last_server check when plugin is disabled!");
            return false;
        }

        String data = pl.getDataManager().getData(ctx.getConnection().uuid().toString()).getOrDefault("last_server", (String) null);
        if(data == null) return false;

        ctx.setMetaProperty("last_server.backend", data);
        return true;
    }

    @Override
    public Type type() {
        return TYPE;
    }

    private static final Serializer<LastServerCheck> SERIALIZER = ObjectSerializer.create(
            Serializer.BOOLEAN.entry("require_auth", LastServerCheck::requiresAuth).orElse(true),
            LastServerCheck::new
    );

    public static class Type implements ConnectionCheckType<LastServerCheck> {

        @Override
        public TypeReference<LastServerCheck> type() {
            return new TypeReference<>() {};
        }

        @Override
        public Serializer<LastServerCheck> serializer() {
            return SERIALIZER;
        }
    }

    public static final Type TYPE = new Type();
}
