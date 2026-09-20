package dev.jwall64.mdproxy.packet.common;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mdproxy.packet.*;
import dev.jwall64.mdproxy.util.PacketBufferUtil;

import java.util.UUID;

public record ServerboundResourcePackStatusPacket(UUID packId, Action action)
        implements Packet<ServerboundPacketHandler> {

    private static final VersionSelector<Integer> ID_SELECTOR = VersionSelector.<Integer>builder()
            .inPhase(ProtocolPhase.CONFIG, 6)
            .afterVersion(777, 325, 50)
            .afterVersion(774, 279, 49)
            .afterVersion(771, 245, 48)
            .orElse(47)
            .build();

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(ID_SELECTOR::select,
            (version, phase, data) -> new ServerboundResourcePackStatusPacket(PacketBufferUtil.readUUID(data),
                    Action.values()[PacketBufferUtil.readVarInt(data)]));

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(int version, ProtocolPhase phase, ByteBuf buf) {
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        DOWNLOAD_FAILED,
        ACCEPTED,
        DOWNLOAD_COMPLETE,
        INVALID_URL,
        RELOAD_FAILED,
        DISCARDED
    }

}
