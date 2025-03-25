package dev.jwall64.mdproxy.whitelist;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.requirement.ConnectionCheck;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.Collections;

public class WhitelistCheck implements ConnectionCheck {

    private final boolean requireAuth;
    private final String listId;
    private Whitelist whitelist;

    public WhitelistCheck(boolean requireAuth, String whitelist) {
        this.requireAuth = requireAuth;
        this.listId = whitelist;
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
    public boolean check(ConnectionContext conn) {

        if(whitelist == null || whitelist.isInvalid()) {
            whitelist = conn.getProxy().getPluginManager().get(WhitelistPlugin.class).getLists().get(listId);
            if(whitelist == null) return false;
        }

        return whitelist.isWhitelisted(conn.getConnection());
    }

    @Override
    public Type type() {
        return TYPE;
    }

    private static final Serializer<WhitelistCheck> SERIALIZER = ObjectSerializer.create(
            Serializer.BOOLEAN.<WhitelistCheck>entry("require_auth", chk -> chk.requireAuth).orElse(true),
            Serializer.STRING.entry("whitelist", chk -> chk.listId),
            WhitelistCheck::new
    );

    public static class Type implements ConnectionCheckType<WhitelistCheck> {

        @Override
        public Serializer<WhitelistCheck> serializer() {
            return SERIALIZER;
        }
    }

    public static final Type TYPE = new Type();


}
