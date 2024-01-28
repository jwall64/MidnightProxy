package dev.jwall64.mdproxy.packet.config;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;

public class ServerboundSettingsPacket implements Packet {

    public static final PacketType TYPE = PacketType.of(0, buf -> new ServerboundSettingsPacket());
    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) { }
}
