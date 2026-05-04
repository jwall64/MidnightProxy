package dev.jwall64.mdproxy.packet.config;

import dev.jwall64.mdproxy.util.PacketBufferUtil;

import io.netty.buffer.ByteBuf;

public record KnownPack(String namespace, String name, String version) { 

    public void write(ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, namespace);
        PacketBufferUtil.writeUtf(buf, name);
        PacketBufferUtil.writeUtf(buf, version);
    }

    public static KnownPack read(ByteBuf buf) {
        String namespace = PacketBufferUtil.readUtf(buf);
        String name = PacketBufferUtil.readUtf(buf);
        String version = PacketBufferUtil.readUtf(buf);
        return new KnownPack(namespace, name, version);
    }

}
