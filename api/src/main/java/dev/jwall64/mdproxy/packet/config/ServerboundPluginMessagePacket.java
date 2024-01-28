package dev.jwall64.mdproxy.packet.config;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;

public class ServerboundPluginMessagePacket implements Packet {

    public static final PacketType TYPE = PacketType.of(2, buf -> null);
    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) { }
}
