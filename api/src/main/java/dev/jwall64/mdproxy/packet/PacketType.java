package dev.jwall64.mdproxy.packet;

import io.netty.buffer.ByteBuf;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mdcfg.Functions;

public interface PacketType<T> {

    int getId(GameVersion version, ProtocolPhase phase);

    Packet<T> read(GameVersion version, ProtocolPhase phase, ByteBuf buf);

    static <T> PacketType<T> of(int id, Functions.F3<GameVersion, ProtocolPhase, ByteBuf, Packet<T>> reader) {
        return new PacketType<>() {
            @Override
            public int getId(GameVersion version, ProtocolPhase phase) {
                return id;
            }

            @Override
            public Packet<T> read(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
                return reader.apply(version, phase, buf);
            }
        };
    }

    static <T> PacketType<T> of(Functions.F2<GameVersion, ProtocolPhase, Integer> id, Functions.F3<GameVersion, ProtocolPhase, ByteBuf, Packet<T>> reader) {
        return new PacketType<>() {
            @Override
            public int getId(GameVersion version, ProtocolPhase phase) {
                return id.apply(version, phase);
            }

            @Override
            public Packet<T> read(GameVersion version, ProtocolPhase phase, ByteBuf buf) {
                return reader.apply(version, phase, buf);
            }
        };
    }

}
