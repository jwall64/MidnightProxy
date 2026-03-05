package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.requirement.ConnectionRequirement;

public record AuthRoute(Authenticator authenticator, ConnectionRequirement requirement, boolean kickOnFail, String kickMessage, int priority) implements Comparable<AuthRoute> {

    public AuthRoute(Authenticator authenticator, ConnectionRequirement requirement, boolean kickOnFail) {
        this(authenticator, requirement, kickOnFail, 0);
    }
    public AuthRoute(Authenticator authenticator, ConnectionRequirement requirement, boolean kickOnFail, int priority) {
        this(authenticator, requirement, kickOnFail, "error.generic_auth_failed", priority);
    }

    public static Serializer<AuthRoute> serializer(Proxy proxy) {
        return ObjectSerializer.create(
                Serializer.STRING.entry("type", (route) -> Authenticator.REGISTRY.getId(route.authenticator().getType())),
                ConnectionRequirement.SERIALIZER.<AuthRoute>entry("requirement", (route) -> route.requirement).optional(),
                Serializer.BOOLEAN.<AuthRoute>entry("kick_on_fail", (route) -> route.kickOnFail).orElse(false),
                Serializer.STRING.<AuthRoute>entry("kick_message", (route) -> route.kickMessage).orElse("error.generic_auth_failed"),
                Serializer.INT.entry("priority", AuthRoute::priority).orElse(0),
                (auth, req, kick, msg, priority) -> new AuthRoute(proxy.getAuthenticator(auth), req, kick, msg, priority));
    }

    public boolean canUse(ConnectionContext context) {
        if(!authenticator.canAuthenticate(context.getConnection())) {
            return false;
        }

        if(requirement != null) {
            TestResult res = requirement.test(context);
            return res == TestResult.PASS;
        }

        return true;
    }

    public PlayerProfile authenticate(ConnectionContext context, String serverId) {

        return authenticator.authenticate(context.getConnection(), serverId);
    }

    @Override
    public int compareTo(AuthRoute other) {
        return Integer.compare(priority, other.priority);
    }

}
