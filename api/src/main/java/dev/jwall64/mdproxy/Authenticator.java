package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.registry.Registry;

public interface Authenticator {

    Type getType();

    boolean canAuthenticate(ClientConnection connection);
    boolean shouldClientAuthenticate();

    PlayerProfile authenticate(ClientConnection connection, String serverId);


    String MOJANG_ID = "mojang";
    Registry<String, Type> REGISTRY = Registry.createStringRegistry();


    interface Type {
        Authenticator create(Proxy proxy);
    }

}
