package dev.jwall64.mdproxy.requirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.TestResult;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.Check;
import dev.jwall64.midnightlib.requirement.Requirement;

import java.util.Collection;
import java.util.Collections;

public class ConnectionRequirement extends Requirement<ConnectionContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger("ConnectionRequirement");

    public ConnectionRequirement(Check<ConnectionContext> check, boolean invert) {
        super(check, invert);
    }


    public boolean requiresAuth() {
        return check instanceof ConnectionCheck cc && cc.requiresAuth();
    }

    public Collection<Identifier> getRequiredCookies() {
        if(check instanceof ConnectionCheck cc) {
            return cc.getRequiredCookies();
        }
        return Collections.emptyList();
    }


    public TestResult test(ConnectionContext conn) {

        if ((requiresAuth() && !conn.getConnection().authenticated())) {
            return TestResult.NOT_ENOUGH_INFO;
        }

        try {
            return check(conn) ? TestResult.PASS : TestResult.FAIL;
        } catch (Throwable ex) {
            LOGGER.error("An error occurred while checking a requirement!", ex);
            return TestResult.FAIL;
        }
    }

    public static final Serializer<ConnectionRequirement> SERIALIZER = Requirement.serializer(ConnectionCheckType.REGISTRY, ConnectionRequirement::new);

}
