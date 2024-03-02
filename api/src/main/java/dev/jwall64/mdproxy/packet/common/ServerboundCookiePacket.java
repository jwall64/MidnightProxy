package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ServerboundCookiePacket(Identifier key, byte[] data) implements Packet<ServerboundPacketHandler> {


    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of((ver, phase) -> phase == ProtocolPhase.LOGIN ? 4 : 1, ServerboundCookiePacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, key.toString());
        PacketBufferUtil.writeOptional(buf, data, ByteBuf::writeBytes);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundCookiePacket read(GameVersion ver, ProtocolPhase phase, ByteBuf buf) {

        Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), "minecraft");
        byte[] data = PacketBufferUtil.readOptional(buf, buf1 -> {

            byte[] out = new byte[PacketBufferUtil.readVarInt(buf1)];
            buf1.readBytes(out);

            return out;

        }).orElse(new byte[0]);

        return new ServerboundCookiePacket(id, data);
    }
}
