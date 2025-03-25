package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.GameVersion;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ProtocolPhase;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundEncryptionPacket(String serverId, byte[] publicKey, byte[] verifyToken, boolean authEnabled) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(1, (ver, phase, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, serverId);
        PacketBufferUtil.writeVarInt(buf, publicKey.length);
        buf.writeBytes(publicKey);
        PacketBufferUtil.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);

        if(GameVersion.hasFeature(version, PacketBufferUtil.TRANSFER_PACKETS)) {
            buf.writeBoolean(authEnabled);
        }

    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
