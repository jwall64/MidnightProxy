package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundEncryptionPacket(String serverId, byte[] publicKey, byte[] verifyToken, boolean authEnabled) implements Packet {


    public static final PacketType TYPE = PacketType.of(1, buf -> {
        throw new UnsupportedOperationException("Cannot deserialize clientbound packet!");
    });

    @Override
    public PacketType getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketBufferUtil.writeUtf(buf, serverId, 20);
        PacketBufferUtil.writeVarInt(buf, publicKey.length);
        buf.writeBytes(publicKey);
        PacketBufferUtil.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);
        buf.writeBoolean(authEnabled);
    }
}
