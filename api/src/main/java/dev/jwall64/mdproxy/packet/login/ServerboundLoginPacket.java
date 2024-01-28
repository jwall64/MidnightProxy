package dev.jwall64.mdproxy.packet.login;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import java.util.UUID;

public record ServerboundLoginPacket(String username, UUID uuid) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(0, ServerboundLoginPacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion version, ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, username, 16);
        PacketBufferUtil.writeUUID(buf, uuid);
    }
    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundLoginPacket read(GameVersion ver, ByteBuf buf) {

        String name = PacketBufferUtil.readUtf(buf, 16);
        UUID uid = PacketBufferUtil.readUUID(buf);

        return new ServerboundLoginPacket(name, uid);
    }


}
