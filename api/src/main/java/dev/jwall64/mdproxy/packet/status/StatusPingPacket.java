package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;

public record StatusPingPacket(long value) implements Packet {

    public static final PacketType TYPE =  PacketType.of(1, buf -> new StatusPingPacket(buf.readLong()));

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(value);
    }
}
