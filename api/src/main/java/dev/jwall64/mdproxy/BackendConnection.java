package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdproxy.packet.Packet;
import dev.jwall64.mdproxy.packet.ServerboundPacketHandler;

public interface BackendConnection {

    boolean isConnected();

    boolean isForwarding();

    default boolean isEphemeral(Proxy proxy) {
        return getBackendId(proxy) == null;
    }

    @Nullable String getBackendId(Proxy proxy);

    void send(Packet<ServerboundPacketHandler> packet);

    Backend getBackend();

}
