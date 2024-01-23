package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;

public class ServerboundStatusPacket implements Packet {

    public static final PacketType TYPE = PacketType.of(0, buf -> new ServerboundStatusPacket());

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) { }
}
