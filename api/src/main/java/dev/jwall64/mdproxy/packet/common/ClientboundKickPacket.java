package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ModernSerializer;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundKickPacket(Component message) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of((ver, phase) -> phase == ProtocolPhase.LOGIN ? 0 : 2, (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion ver, ProtocolPhase phase, ByteBuf buf) {

        if (phase == ProtocolPhase.LOGIN) {
            PacketBufferUtil.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, ModernSerializer.INSTANCE.serialize(ConfigContext.INSTANCE, message, ver).getOrThrow()));
        } else {
            PacketBufferUtil.writeNBTComponent(buf, message);
        }
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }

}
