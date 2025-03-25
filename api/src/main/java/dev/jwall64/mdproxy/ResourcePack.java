package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdproxy.packet.common.ClientboundAddResourcePackPacket;
import dev.jwall64.pseudonym.text.Component;

import java.util.UUID;

public record ResourcePack(UUID uuid, String url, @Nullable String hash, boolean required, @Nullable Component message) {

    public ClientboundAddResourcePackPacket toPacket() {
        return new ClientboundAddResourcePackPacket(uuid, url, hash, required, message);
    }

}
