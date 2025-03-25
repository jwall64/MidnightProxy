package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdproxy.VarInt;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ServerboundLoginQueryPacket(int messageId, @Nullable ByteBuf data) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(2, ServerboundLoginQueryPacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        new VarInt(messageId).write(buf);
        PacketBufferUtil.writeOptional(buf, data, ByteBuf::writeBytes);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundLoginQueryPacket read(int version, ProtocolPhase phase, ByteBuf buf) {
        VarInt messageId = VarInt.read(buf, 4);

        boolean message = buf.readBoolean();

        ByteBuf data = null;
        if(message) {
            data = buf.retainedSlice();
            buf.skipBytes(buf.readableBytes());
        }

        return new ServerboundLoginQueryPacket(messageId.value(), data);
    }
}
