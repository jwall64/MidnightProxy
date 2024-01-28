package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundEncryptionPacket(String serverId, byte[] publicKey, byte[] verifyToken, boolean authEnabled) implements Packet<ClientboundPacketHandler> {


    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(1, (ver, buf) -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, serverId);
        PacketBufferUtil.writeVarInt(buf, publicKey.length);
        buf.writeBytes(publicKey);
        PacketBufferUtil.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);

        if(version.hasFeature(GameVersion.Feature.TRANSFER_PACKETS)) {
            buf.writeBoolean(authEnabled);
        }

    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        throw new UnsupportedOperationException("Unable to handle clientbound packet");
    }
}
