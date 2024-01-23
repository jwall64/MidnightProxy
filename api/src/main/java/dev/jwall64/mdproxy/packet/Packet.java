package dev.jwall64.mdproxy.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {

    PacketType getType();

    void write(ByteBuf buf);

}
