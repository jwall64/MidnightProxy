package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;

public record ServerboundPingPacket(long value) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE =  PacketType.of(1, (ver, phase, buf) -> new ServerboundPingPacket(buf.readLong()));

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        buf.writeLong(value);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }
}
