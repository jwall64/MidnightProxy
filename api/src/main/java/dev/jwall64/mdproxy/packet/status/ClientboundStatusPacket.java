package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.StatusMessage;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundStatusPacket(ConfigSection data) implements Packet<ClientboundPacketHandler> {


    public ClientboundStatusPacket(StatusMessage message) {
        this(message.serialize());
    }

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(0, ClientboundStatusPacket::read);

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, data));
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ClientboundStatusPacket read(int  version, ProtocolPhase phase, ByteBuf buf) {

        return new ClientboundStatusPacket(JSONCodec.loadConfig(PacketBufferUtil.readUtf(buf)).asSection());
    }
}
