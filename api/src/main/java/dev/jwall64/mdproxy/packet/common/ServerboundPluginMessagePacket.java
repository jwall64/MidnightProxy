package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ServerboundPluginMessagePacket(Identifier channel, ByteBuf data) implements Packet<ServerboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .afterVersionInPhase(766, 171, ProtocolPhase.CONFIG, 2)
            .inPhase(ProtocolPhase.CONFIG, 1)
            .orElse(20)
            .build();


    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(ID_SELECTOR::select,
            ServerboundPluginMessagePacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, channel.toString());
        buf.writeBytes(data);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundPluginMessagePacket read(int  version, ProtocolPhase phase, ByteBuf buf) {
        Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), "minecraft");
        ServerboundPluginMessagePacket out = new ServerboundPluginMessagePacket(id, buf.retainedSlice());
        buf.skipBytes(buf.readableBytes());
        return out;
    }

}
