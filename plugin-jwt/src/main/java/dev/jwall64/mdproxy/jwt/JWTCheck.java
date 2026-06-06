package dev.jwall64.mdproxy.jwt;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.requirement.ConnectionCheck;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.jwt.Jwt;
import dev.jwall64.jwt.KeyVerifier;
import dev.jwall64.jwt.TokenValidator;
import dev.jwall64.mdcfg.registry.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class JWTCheck implements ConnectionCheck {

    private final Identifier cookie;
    private final boolean requireAuth;
    private final boolean requireEncryption;
    private final TokenValidator validator;
    private final String key;
    private final List<String> outputClaims;

    protected JWTCheck(boolean requireAuth, boolean requireEncryption, Identifier cookie, String key, TokenValidator validator, Collection<String> outputClaims) {
        this.requireAuth = requireAuth;
        this.requireEncryption = requireEncryption;
        this.cookie = cookie;
        this.key = key;
        this.validator = validator;
        this.outputClaims = List.copyOf(outputClaims);
    }

    @Override
    public boolean check(ConnectionContext ctx) {

        byte[] data = ctx.getConnection().getCookie(cookie);
        if(data == null || data.length == 0) {
            JWTPlugin.LOGGER.warn("Unable to find required JWT cookie for {}! ({})", ctx.username(), cookie);
            return false;
        }

        KeyStore store = ctx.getProxy().getPluginManager().get(JWTPlugin.class).getKeyStore();
        String str = new String(data, StandardCharsets.US_ASCII);

        Jwt token;
        try {
            token = Jwt.parseAndVerify(str, store.supplier(key), KeyVerifier.DEFAULT);
        } catch(Exception ex) {
            JWTPlugin.LOGGER.warn("Unable to parse JWT for {}!", ctx.username(), ex);
            return false;
        }

        if(requireEncryption && !(token instanceof Jwt.Encrypted)) {
            JWTPlugin.LOGGER.warn("JWT for {} was unencrypted!", ctx.username());
            return false;
        }

        if(validator != null && !validator.validate(token)) {
            JWTPlugin.LOGGER.warn("JWT for {} could not be validated!", ctx.username());
            return false;
        }

        for(String s : outputClaims) {

            String claimStr = token.payload().getOrDefault(s, (String) null);
            if(claimStr == null) {
                JWTPlugin.LOGGER.warn("Unable to find claim {} for {}", s, ctx.username());
                return false;
            }

            ctx.setMetaProperty("jwt_" + s, claimStr);
        }

        return true;
    }

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public boolean requiresAuth() {
        return requireAuth;
    }

    @Override
    public @NotNull Collection<Identifier> getRequiredCookies() {
        return Set.of(cookie);
    }


    public static final Serializer<JWTCheck> SERIALIZER = ObjectSerializer.create(
            Serializer.BOOLEAN.entry("require_auth", JWTCheck::requiresAuth).orElse(true),
            Serializer.BOOLEAN.<JWTCheck>entry("require_encryption", check -> check.requireEncryption).orElse(false),
            Identifier.serializer("minecraft").entry("cookie", check -> check.cookie),
            Serializer.STRING.<JWTCheck>entry("key", check -> check.key).orElse("default"),
            TokenValidator.SERIALIZER.<JWTCheck>entry("validator", check -> check.validator).orElse(null),
            Serializer.STRING.listOf().entry("output_claims", check -> check.outputClaims),
            JWTCheck::new
    );

    public static class Type implements ConnectionCheckType<JWTCheck> {

        @Override
        public Serializer<JWTCheck> serializer() {
            return SERIALIZER;
        }
    }


    public static final Type TYPE = new Type();
}
