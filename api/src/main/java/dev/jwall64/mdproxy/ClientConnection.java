package dev.jwall64.mdproxy;

import dev.jwall64.mdproxy.packet.ServerboundHandshakePacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginPacket;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.UUID;

public interface ClientConnection {

    boolean authenticated();
    boolean cookiesAvailable();
    boolean canTransfer();

    String hostname();

    int port();

    String username();

    UUID uuid();

    byte[] getCookie(Identifier id);

    ServerboundHandshakePacket handshakePacket(ServerboundHandshakePacket.Intent intent);

    ServerboundLoginPacket loginPacket();

}
