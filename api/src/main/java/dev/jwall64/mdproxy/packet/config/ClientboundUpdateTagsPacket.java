package dev.jwall64.mdproxy.packet.config;

import java.util.List;
import java.util.Map;

import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdproxy.RegistryData.TagList;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import io.netty.buffer.ByteBuf;

public record ClientboundUpdateTagsPacket(Map<Identifier, List<TagList>> tags) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(13,
        (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeVarInt(buf, tags.size());
        for(Map.Entry<Identifier, List<TagList>> entry : tags.entrySet()) {

            PacketBufferUtil.writeUtf(buf, entry.getKey().toString());
            PacketBufferUtil.writeVarInt(buf, entry.getValue().size());

            for(TagList tagList : entry.getValue()) {

                PacketBufferUtil.writeUtf(buf, tagList.key().toString());
                PacketBufferUtil.writeVarInt(buf, tagList.entries().size());

                for (int id : tagList.entries()) {
                    PacketBufferUtil.writeVarInt(buf, id);
                }
            }
        }
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Cannot handle clientbound packet!");
    }
}
