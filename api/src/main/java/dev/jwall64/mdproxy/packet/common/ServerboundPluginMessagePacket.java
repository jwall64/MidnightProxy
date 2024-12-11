package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ServerboundPluginMessagePacket(Identifier channel, ByteBuf data) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(
            (ver, phase) -> {
                if(phase == ProtocolPhase.CONFIG) {
                    return ver.hasFeature(GameVersion.Feature.TRANSFER_PACKETS) ? 2 : 1;
                }
                return 20;
            },
            ServerboundPluginMessagePacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, channel.toString());
        buf.writeBytes(data);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundPluginMessagePacket read(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
        Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), "minecraft");
        ServerboundPluginMessagePacket out = new ServerboundPluginMessagePacket(id, buf.retainedSlice());
        buf.skipBytes(buf.readableBytes());
        return out;
    }

}
