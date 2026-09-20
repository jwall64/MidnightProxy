package dev.jwall64.mdproxy.packet.config;

import java.util.List;

import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.VersionSelector;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import io.netty.buffer.ByteBuf;

public record ClientboundKnownPacksPacket(List<KnownPack> packs) implements Packet<ClientboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .afterVersion(777, 333, 15)
            .orElse(14)
            .build();

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(ID_SELECTOR::select,
        (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeVarInt(buf, packs.size());
        for(KnownPack pack : packs) {
            pack.write(buf);
        }
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Cannot handle clientbound packet!");
    }

}



