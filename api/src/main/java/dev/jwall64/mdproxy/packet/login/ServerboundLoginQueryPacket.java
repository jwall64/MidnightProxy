package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.VarInt;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ServerboundLoginQueryPacket(int messageId, Identifier channel, ByteBuf data) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(4, ServerboundLoginQueryPacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
        new VarInt(messageId).write(buf);
        PacketBufferUtil.writeUtf(buf, channel.toString());
        buf.writeBytes(data);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundLoginQueryPacket read(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
        VarInt messageId = VarInt.read(buf, 4);
        String sid = PacketBufferUtil.readUtf(buf);
        Identifier id = Identifier.parseOrDefault(sid, "minecraft");

        ServerboundLoginQueryPacket out = new ServerboundLoginQueryPacket(messageId.value(), id, buf.retainedSlice());
        buf.skipBytes(buf.readableBytes());

        return out;
    }
}
