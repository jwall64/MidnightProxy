package dev.jwall64.mdproxy.packet;

import dev.jwall64.mdproxy.packet.config.ClientboundSetCookiePacket;
import dev.jwall64.mdproxy.packet.config.ClientboundTransferPacket;
import dev.jwall64.mdproxy.packet.login.*;
import dev.jwall64.mdproxy.packet.status.ClientboundStatusPacket;
import dev.jwall64.mdproxy.packet.status.ServerboundStatusPacket;
import dev.jwall64.mdproxy.packet.status.StatusPingPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private final Map<Integer, PacketType> packetTypes;

    public PacketRegistry(PacketType... packetTypes) {

        Map<Integer, PacketType> tp = new HashMap<>();
        for(PacketType pt : packetTypes) {
            tp.put(pt.getId(), pt);
        }

        this.packetTypes = Map.copyOf(tp);
    }

    public PacketType getPacketType(int id) {
        return packetTypes.get(id);
    }



    public static final PacketRegistry HANDSHAKE = new PacketRegistry(ServerboundHandshakePacket.TYPE);

    public static final PacketRegistry STATUS_CLIENTBOUND = new PacketRegistry(ClientboundStatusPacket.TYPE, StatusPingPacket.TYPE);
    public static final PacketRegistry STATUS_SERVERBOUND = new PacketRegistry(ServerboundStatusPacket.TYPE, StatusPingPacket.TYPE);

    public static final PacketRegistry LOGIN_CLIENTBOUND = new PacketRegistry(ClientboundKickPacket.TYPE, ClientboundEncryptionPacket.TYPE, ClientboundLoginFinishedPacket.TYPE, ClientboundCookieRequestPacket.TYPE);
    public static final PacketRegistry LOGIN_SERVERBOUND = new PacketRegistry(ServerboundLoginPacket.TYPE, ServerboundEncryptionPacket.TYPE, ServerboundLoginFinishedPacket.TYPE, ServerboundCookiePacket.TYPE);

    public static final PacketRegistry CONFIG_CLIENTBOUND = new PacketRegistry(ClientboundSetCookiePacket.TYPE, ClientboundTransferPacket.TYPE);

    public static PacketRegistry getRegistry(PacketFlow flow, ProtocolPhase phase) {

        if(flow == PacketFlow.SERVERBOUND) {

            return switch (phase) {
                case HANDSHAKE -> PacketRegistry.HANDSHAKE;
                case STATUS -> PacketRegistry.STATUS_SERVERBOUND;
                case LOGIN -> PacketRegistry.LOGIN_SERVERBOUND;
                case CONFIG -> throw new IllegalArgumentException("Cannot send config packets to backends!");
            };

        } else {
            return switch (phase) {
                case HANDSHAKE -> throw new IllegalArgumentException("Handshake packets are not sent to the client!");
                case STATUS -> PacketRegistry.STATUS_CLIENTBOUND;
                case LOGIN -> PacketRegistry.LOGIN_CLIENTBOUND;
                case CONFIG -> PacketRegistry.CONFIG_CLIENTBOUND;
            };
        }

    }

}
