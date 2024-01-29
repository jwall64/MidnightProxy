package dev.jwall64.mdproxy;

import dev.jwall64.mdproxy.packet.ServerboundHandshakePacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginPacket;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.UUID;

public interface ClientConnection {

    boolean nameAvailable();
    boolean authenticated();
    boolean cookiesAvailable();
    boolean canTransfer();
    boolean localeAvailable();

    boolean bypassesPlayerLimit(Proxy proxy);

    String hostname();

    int port();

    String username();

    UUID uuid();

    byte[] getCookie(Identifier id);

    String locale();

    ServerboundHandshakePacket handshakePacket(ServerboundHandshakePacket.Intent intent);

    ServerboundLoginPacket loginPacket();

}
