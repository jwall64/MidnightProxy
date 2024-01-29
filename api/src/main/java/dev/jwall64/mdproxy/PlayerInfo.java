package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

import java.util.UUID;

public record PlayerInfo(String username, UUID uuid) {

    public static final Serializer<PlayerInfo> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("username", PlayerInfo::username),
            Serializer.UUID.entry("uuid", PlayerInfo::uuid),
            PlayerInfo::new
    );

}
