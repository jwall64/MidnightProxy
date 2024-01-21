package dev.jwall64.mdproxy;

import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.registry.Registry;
import dev.jwall64.midnightlib.requirement.NumberRequirement;
import dev.jwall64.midnightlib.requirement.RequirementType;
import dev.jwall64.midnightlib.requirement.StringRequirement;

import java.util.Collection;

public interface ConnectionRequirement {

    boolean requiresAuth();

    boolean requiresCookies();

    Collection<Identifier> getRequiredCookies();

    Registry<RequirementType<ClientConnection>> REGISTRY = new Registry<>("mdp");


    RequirementType<ClientConnection> HOSTNAME = REGISTRY.register("hostname", StringRequirement.type(ClientConnection::hostname));
    RequirementType<ClientConnection> PORT = REGISTRY.register("port", NumberRequirement.type(ClientConnection::port));

}
