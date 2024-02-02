package dev.jwall64.mdproxy;

import dev.jwall64.mcore.text.Component;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundHandshakePacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginPacket;
import dev.jwall64.midnightlib.registry.Identifier;

import java.net.InetAddress;
import java.util.UUID;

public interface ClientConnection {

    boolean playerInfoAvailable();
    boolean authenticated();

    InetAddress address();
    String hostname();
    int port();
    int protocolVersion();

    String username();

    UUID uuid();

    PlayerInfo playerInfo();

    byte[] getCookie(Identifier id);

    String locale();

    ServerboundHandshakePacket handshakePacket(ServerboundHandshakePacket.Intent intent);

    ServerboundLoginPacket loginPacket();

    boolean isConnected();

    BackendConnection getBackendConnection();

    TestResult bypassesPlayerLimit(Proxy proxy);

    void send(Packet<ClientboundPacketHandler> packet);

    void disconnect(ProtocolPhase phase, Component message);

    void disconnect();

}
