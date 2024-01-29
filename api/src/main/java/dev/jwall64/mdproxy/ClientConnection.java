package dev.jwall64.mdproxy;

import dev.jwall64.mdproxy.packet.ServerboundHandshakePacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginPacket;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.UUID;

public interface ClientConnection {

    boolean playerInfoAvailable();
    boolean authenticated();
    boolean cookiesAvailable();
    boolean canTransfer();
    boolean localeAvailable();

    TestResult bypassesPlayerLimit(Proxy proxy);

    String hostname();

    int port();

    String username();

    UUID uuid();

    PlayerInfo playerInfo();

    byte[] getCookie(Identifier id);

    String locale();

    ServerboundHandshakePacket handshakePacket(ServerboundHandshakePacket.Intent intent);

    ServerboundLoginPacket loginPacket();

}
