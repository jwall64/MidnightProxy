package dev.jwall64.mdproxy.resources;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ConfigSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.packet.config.ClientboundAddResourcePackPacket;

import java.util.UUID;

public record ResourcePack(UUID uuid, String url, @Nullable String hash, boolean required, @Nullable Component message) {

    public static final Serializer<ResourcePack> SERIALIZER = ObjectSerializer.create(
            Serializer.UUID.entry("uuid", ResourcePack::uuid),
            Serializer.STRING.entry("url", ResourcePack::url),
            Serializer.STRING.entry("hash", ResourcePack::hash).optional(),
            Serializer.BOOLEAN.entry("required", ResourcePack::required).orElse(false),
            ConfigSerializer.INSTANCE.entry("message", ResourcePack::message).optional(),
            ResourcePack::new
    );

    public ClientboundAddResourcePackPacket toPacket() {
        return new ClientboundAddResourcePackPacket(uuid, url, hash, required, message);
    }

}
