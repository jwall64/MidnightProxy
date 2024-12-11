package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import java.util.UUID;

public record ClientboundAddResourcePackPacket(UUID packId, String url, String sha1, boolean forced, @Nullable Component message) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(
            (ver, phase) -> phase == ProtocolPhase.CONFIG ? 9 : 75,
            (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });


    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
        PacketBufferUtil.writeUUID(buf, packId);
        PacketBufferUtil.writeUtf(buf, url);
        PacketBufferUtil.writeUtf(buf, sha1);
        buf.writeBoolean(forced);
        PacketBufferUtil.writeOptional(buf, message, PacketBufferUtil::writeNBTComponent);
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
