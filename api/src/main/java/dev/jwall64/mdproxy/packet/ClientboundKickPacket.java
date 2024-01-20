package dev.jwall64.mdproxy.packet;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundKickPacket(Component message) implements Packet {

    public static final int ID = 0;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, message.toJSONString());
    }
}
