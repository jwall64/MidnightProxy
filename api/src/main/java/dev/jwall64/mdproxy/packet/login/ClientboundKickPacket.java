package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundKickPacket(Component message) implements Packet {

    public static final PacketType TYPE = PacketType.of(0, buf -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, message.toJSONString());
    }

}
