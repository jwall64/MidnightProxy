package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundTransferPacket(String host, int port) implements Packet<ClientboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .inPhase(ProtocolPhase.CONFIG, 11)
            .orElse(122)
            .build();

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(ID_SELECTOR::select, (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, host);
        PacketBufferUtil.writeVarInt(buf, port);
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
