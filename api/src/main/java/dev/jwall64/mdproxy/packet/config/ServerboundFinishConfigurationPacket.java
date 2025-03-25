package dev.jwall64.mdproxy.packet.config;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;

public class ServerboundFinishConfigurationPacket implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(3, (ver, phase, buf) -> new ServerboundFinishConfigurationPacket());


    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        throw new UnsupportedOperationException("Cannot write serverbound packet");
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }
}
