package dev.jwall64.mdproxy.packet;

import dev.jwall64.mdproxy.packet.login.ClientboundLoginQueryPacket;
import dev.jwall64.mdproxy.packet.status.ClientboundPingPacket;
import dev.jwall64.mdproxy.packet.status.ClientboundStatusPacket;

public interface ClientboundPacketHandler {

    void handle(ClientboundPingPacket ping);

    void handle(ClientboundStatusPacket status);

    void handle(ClientboundLoginQueryPacket message);

}
