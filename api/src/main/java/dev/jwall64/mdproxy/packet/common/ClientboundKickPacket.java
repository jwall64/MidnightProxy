package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;
import dev.jwall64.pseudonym.text.Component;
import dev.jwall64.pseudonym.text.ProtocolContext;

public record ClientboundKickPacket(Component message) implements Packet<ClientboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .inPhase(ProtocolPhase.LOGIN, 0)
            .inPhase(ProtocolPhase.CONFIG, 2)
            .afterVersion(770, 229, 27)
            .orElse(29)
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

        if (phase == ProtocolPhase.LOGIN) {
            PacketBufferUtil.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, Component.SERIALIZER.serialize(new ProtocolContext<>(ConfigContext.INSTANCE, version), message).getOrThrow()));
        } else {
            PacketBufferUtil.writeNBTComponent(buf, message);
        }
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }

}
