package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;

public record ClientboundPingPacket(long value) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE =  PacketType.of(1, (ver, phase, buf) -> new ClientboundPingPacket(buf.readLong()));

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        handler.handle(this);
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase,  ByteBuf buf) {
        buf.writeLong(value);
    }
}
