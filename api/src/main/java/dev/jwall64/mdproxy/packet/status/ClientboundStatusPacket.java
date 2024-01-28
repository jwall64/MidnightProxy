package dev.jwall64.mdproxy.packet.status;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ModernSerializer;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdproxy.packet.ClientboundPacketHandler;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.PacketType;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

public record ClientboundStatusPacket(ConfigSection data) implements Packet<ClientboundPacketHandler> {


    public ClientboundStatusPacket(GameVersion version, Component motd, int maxPlayers, int onlinePlayers, boolean enforcesSecureChat, boolean previewsChat) {
        this(new ConfigSection()
                .with("version", new ConfigSection()
                        .with("name", version.getId())
                        .with("protocol", version.getProtocolVersion())
                )
                .with("players", new ConfigSection()
                        .with("max", maxPlayers)
                        .with("online", onlinePlayers)
                )
                .with("description", motd, ModernSerializer.INSTANCE.forContext(version))
                .with("enforcesSecureChat", enforcesSecureChat)
                .with("previewsChat", previewsChat));
    }

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(0, ClientboundStatusPacket::read);

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(GameVersion ver, ByteBuf buf) {

        PacketBufferUtil.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, data));
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ClientboundStatusPacket read(GameVersion version, ByteBuf buf) {

        return new ClientboundStatusPacket(JSONCodec.loadConfig(PacketBufferUtil.readUtf(buf)).asSection());
    }
}
