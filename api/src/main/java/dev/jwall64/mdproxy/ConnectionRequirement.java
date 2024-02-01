package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.MultiRequirement;
import dev.jwall64.midnightlib.requirement.Requirement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConnectionRequirement extends Requirement<ClientConnection, ConnectionCheck> {

    public ConnectionRequirement(Serializer<ConnectionCheck> serializer, ConnectionCheck check, boolean invert) {
        super(serializer, check, invert);
    }

    public boolean requiresAuth() {
        return check.requiresAuth();
    }

    public boolean requiresCookies() {

        return check.requiresCookies();
    }

    public Collection<Identifier> getRequiredCookies() {
        return check.getRequiredCookies();
    }

    public boolean requiresLocale() {
        return check.requiresLocale();
    }

    public TestResult test(ClientConnection conn) {

        if ((requiresAuth() && !conn.authenticated()) ||
                (requiresCookies() && !conn.cookiesAvailable()) ||
                (requiresLocale() && !conn.localeAvailable())) {

            return TestResult.NOT_ENOUGH_INFO;
        }

        return check(conn) ? TestResult.PASS : TestResult.FAIL;
    }


    public static final Serializer<ConnectionRequirement> SERIALIZER = Requirement.serializer(ConnectionCheck.REGISTRY, ConnectionRequirement::new);

}
