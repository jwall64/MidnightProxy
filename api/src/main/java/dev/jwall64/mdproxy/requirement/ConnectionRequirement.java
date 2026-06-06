package dev.jwall64.mdproxy.requirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdproxy.TestResult;
import dev.jwall64.mdcfg.Tuples;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.midnightlib.requirement.Check;
import dev.jwall64.midnightlib.requirement.Requirement;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConnectionRequirement extends Requirement<ConnectionContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger("ConnectionRequirement");

    private final Map<String, String> successMeta;
    private final Map<String, String> failMeta;

    public ConnectionRequirement(Check<ConnectionContext> check, boolean invert) {
        this(check, invert, Collections.emptyMap(), Collections.emptyMap());
    }

    public ConnectionRequirement(Check<ConnectionContext> check, boolean invert, Map<String, String> successMeta, Map<String, String> failMeta) {
        super(check, invert);
        this.successMeta = successMeta;
        this.failMeta = failMeta;
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
            if(check(conn)) {
                conn.addMetaProperties(successMeta);
                return TestResult.PASS;
            } else { 
                conn.addMetaProperties(failMeta);
                return TestResult.FAIL;
            }
        } catch (Throwable ex) {
            LOGGER.error("An error occurred while checking a requirement!", ex);
            return TestResult.FAIL;
        }
    }

    public static final Serializer<ConnectionRequirement> SERIALIZER = 
        Requirement.serializer(ConnectionCheckType.REGISTRY, ConnectionRequirement::new).and(
            Serializer.STRING.mapOf().fieldOf("success_meta").and(Serializer.STRING.mapOf().fieldOf("fail_meta"))
        ).flatMap(req -> {
            return new Tuples.T2<>(req, new Tuples.T2<>(req.successMeta, req.failMeta));
        }, t2 -> {
            ConnectionRequirement req = t2.p1;
            return new ConnectionRequirement(req.check, req.invert, t2.p2.p1, t2.p2.p2);
        });

}
