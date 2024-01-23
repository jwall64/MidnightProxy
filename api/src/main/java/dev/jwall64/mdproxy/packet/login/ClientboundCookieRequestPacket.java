package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ClientboundCookieRequestPacket(Identifier key) implements Packet {


    public static final PacketType TYPE = PacketType.of(5, buf -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, key.toString());
    }

    public static ClientboundCookieRequestPacket read(ByteBuf buf) {

        return new ClientboundCookieRequestPacket(Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), "minecraft"));
    }
}
