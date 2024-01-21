package dev.jwall64.mdproxy;

import dev.jwall64.midnightlib.registry.Identifier;

import java.util.UUID;

public interface ClientConnection {

    boolean authenticated();

    boolean cookiesAvailable();

    String hostname();

    int port();

    String username();

    UUID uuid();

    byte[] getCookie(Identifier id);

}
