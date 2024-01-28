package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

import java.util.Optional;

public record ServerboundCookiePacket(Identifier key, Optional<byte[]> data) implements Packet {


    public static final PacketType TYPE = PacketType.of(4, ServerboundCookiePacket::read);

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, key.toString());
        PacketBufferUtil.writeOptional(buf, data.orElse(null), ByteBuf::writeBytes);
    }

    public static ServerboundCookiePacket read(ByteBuf buf) {

        Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), "minecraft");
        Optional<byte[]> data = PacketBufferUtil.readOptional(buf, buf1 -> {

            byte[] out = new byte[PacketBufferUtil.readVarInt(buf1)];
            buf1.readBytes(out);

            return out;
        });

        return new ServerboundCookiePacket(id, data);
    }
}
