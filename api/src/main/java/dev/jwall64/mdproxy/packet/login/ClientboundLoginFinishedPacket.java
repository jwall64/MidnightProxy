package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.PlayerProfile;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import java.util.Collection;

public record ClientboundLoginFinishedPacket(PlayerProfile gameProfile) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(2, (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }

    @Override
    public void write(GameVersion version, ProtocolPhase phase, ByteBuf buf) {

        PacketBufferUtil.writeUUID(buf, gameProfile.uuid());
        PacketBufferUtil.writeUtf(buf, gameProfile.username(), 16);

        Collection<PlayerProfile.Property> props = gameProfile.properties();
        PacketBufferUtil.writeVarInt(buf, props.size());
        for(PlayerProfile.Property prop : props) {

            PacketBufferUtil.writeUtf(buf, prop.name());
            PacketBufferUtil.writeUtf(buf, prop.value());
            PacketBufferUtil.writeOptional(buf, prop.signature(), PacketBufferUtil::writeUtf);
        }

        if(version.getProtocolVersion() < 768) {
            buf.writeBoolean(true); // Strict error handling
        }
    }
}
