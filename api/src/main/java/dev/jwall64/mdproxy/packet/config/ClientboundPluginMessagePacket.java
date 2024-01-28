package dev.jwall64.mdproxy.packet.config;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.midnightlib.registry.Identifier;

public record ClientboundPluginMessagePacket(Identifier channel, ByteBuf data) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(ver -> ver.hasFeature(GameVersion.Feature.TRANSFER_PACKETS) ? 1 : 0, (ver, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, channel.toString());
        buf.writeBytes(data);
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
