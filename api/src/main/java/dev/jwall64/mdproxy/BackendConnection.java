package dev.jwall64.mdproxy;

import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;

public interface BackendConnection {

    boolean isConnected();

    boolean isForwarding();

    void send(Packet<ServerboundPacketHandler> packet);

}
