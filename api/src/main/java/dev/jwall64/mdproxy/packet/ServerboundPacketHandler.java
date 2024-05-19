package dev.jwall64.mdproxy.packet;

import dev.jwall64.mdproxy.packet.common.ServerboundCookiePacket;
import dev.jwall64.mdproxy.packet.config.ServerboundPluginMessagePacket;
import dev.jwall64.mdproxy.packet.config.ServerboundSettingsPacket;
import dev.jwall64.mdproxy.packet.login.ServerboundEncryptionPacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginFinishedPacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginPacket;
import dev.jwall64.mdproxy.packet.login.ServerboundLoginQueryPacket;
import dev.jwall64.mdproxy.packet.status.ServerboundPingPacket;
import dev.jwall64.mdproxy.packet.status.ServerboundStatusPacket;

public interface ServerboundPacketHandler {

    void handle(ServerboundHandshakePacket handshake);

    void handle(ServerboundStatusPacket ping);

    void handle(ServerboundPingPacket ping);

    void handle(ServerboundLoginPacket login);

    void handle(ServerboundLoginQueryPacket message);

    void handle(ServerboundEncryptionPacket encrypt);

    void handle(ServerboundLoginFinishedPacket finished);

    void handle(ServerboundCookiePacket cookie);

    void handle(ServerboundPluginMessagePacket message);

    void handle(ServerboundSettingsPacket settings);


}
