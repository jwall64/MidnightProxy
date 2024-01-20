package dev.jwall64.mdproxy.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {

    int getId();

    void write(ByteBuf buf);

}
