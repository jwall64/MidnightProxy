package dev.jwall64.mdproxy.requirement;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.midnightlib.requirement.Check;

import java.util.Collection;

public interface ConnectionCheck extends Check<ConnectionContext> {

    boolean requiresAuth();

    @NotNull
    Collection<Identifier> getRequiredCookies();

}
