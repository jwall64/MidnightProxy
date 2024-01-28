package dev.jwall64.mdproxy.packet.config;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundTransferPacket(String host, int port) implements Packet {

    public static final PacketType TYPE = PacketType.of(10, buf -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, host);
        PacketBufferUtil.writeVarInt(buf, port);
    }
}
