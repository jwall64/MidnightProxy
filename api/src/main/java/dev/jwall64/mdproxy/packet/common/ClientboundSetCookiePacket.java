package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.mdcfg.registry.Identifier;

public record ClientboundSetCookiePacket(Identifier id, byte[] data) implements Packet<ClientboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .inPhase(ProtocolPhase.CONFIG, 10)
            .afterVersion(770, 229, 113)
            .orElse(114)
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
        PacketBufferUtil.writeUtf(buf, id.toString());
        if(data == null || data.length == 0) {
            buf.writeByte(0);
            return;
        }

        PacketBufferUtil.writeVarInt(buf, data.length);
        buf.writeBytes(data);

    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
