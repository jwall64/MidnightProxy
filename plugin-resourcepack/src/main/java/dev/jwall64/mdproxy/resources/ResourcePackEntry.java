package dev.jwall64.mdproxy.resources;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ConfigSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ResourcePack;

import java.util.UUID;

public record ResourcePackEntry(UUID uuid, String url, @Nullable String hash, boolean required, @Nullable Component message, @Nullable Component kickMessage) {

    public static final Serializer<ResourcePackEntry> SERIALIZER = ObjectSerializer.create(
            Serializer.UUID.entry("uuid", ResourcePackEntry::uuid),
            Serializer.STRING.entry("url", ResourcePackEntry::url),
            Serializer.STRING.entry("hash", ResourcePackEntry::hash).optional(),
            Serializer.BOOLEAN.entry("required", ResourcePackEntry::required).orElse(false),
            ConfigSerializer.INSTANCE.entry("message", ResourcePackEntry::message).optional(),
            ConfigSerializer.INSTANCE.entry("kick_message", ResourcePackEntry::kickMessage).optional(),
            ResourcePackEntry::new
    );


    public ResourcePack toPack() {
        return new ResourcePack(uuid, url, hash, required, message);
    }

}
