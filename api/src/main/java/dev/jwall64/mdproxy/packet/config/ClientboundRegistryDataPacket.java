package dev.jwall64.mdproxy.packet.config;

import java.util.Map;
import java.util.Optional;

import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdproxy.RegistryData;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import io.netty.buffer.ByteBuf;

public record ClientboundRegistryDataPacket(RegistryData registry) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(7,
        (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, registry.key().toString());
        PacketBufferUtil.writeVarInt(buf, registry.entries().size());
        for(Map.Entry<Identifier, Optional<ConfigObject>> entry : registry.entries().entrySet()) {
            PacketBufferUtil.writeUtf(buf, entry.getKey().toString());
            buf.writeBoolean(entry.getValue().isPresent());
            if(entry.getValue().isPresent()) {
                PacketBufferUtil.writeNBT(buf, entry.getValue().get());
            }
        }
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Cannot handle clientbound packet!");
    }


}



