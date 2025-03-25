package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;

public class ServerboundStatusPacket implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(0, (ver, phase, buf) -> new ServerboundStatusPacket());

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int ver, ProtocolPhase phase, ByteBuf buf) { }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }
}
