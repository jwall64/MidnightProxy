package dev.jwall64.mdproxy.packet.config;

import java.util.List;

import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import io.netty.buffer.ByteBuf;

public record ServerboundKnownPacksPacket(List<KnownPack> packs) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(7,
        (ver, phase, buf) -> {
            int size = PacketBufferUtil.readVarInt(buf);
            List<KnownPack> packs = new java.util.ArrayList<>(size);
            for(int i = 0; i < size; i++) {
                packs.add(KnownPack.read(buf));
            }
            return new ServerboundKnownPacksPacket(packs);
    });

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
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
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

}



