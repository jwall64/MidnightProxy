package dev.jwall64.mdproxy;

import org.jetbrains.annotations.NotNull;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ModernSerializer;
import dev.jwall64.mdcfg.serializer.NumberSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

import java.util.Collection;

public record StatusEntry(int priority, Integer playersOverride, Integer maxPlayersOverride,
                          Component message, Collection<PlayerInfo> playerSample, boolean secureChat,
                          boolean previewChat, String passthroughHost, int passthroughPort,
                          WrappedRequirement requirement) implements Comparable<StatusEntry> {

    public boolean shouldPassthrough() {
        return passthroughHost != null;
    }

    @Override
    public int compareTo(@NotNull StatusEntry o) {
        return o.priority - priority;
    }

    public static final Serializer<StatusEntry> SERIALIZER = ObjectSerializer.create(
            Serializer.INT.entry("priority", StatusEntry::priority).orElse(0),
            Serializer.INT.entry("players_override", StatusEntry::playersOverride).optional(),
            Serializer.INT.entry("max_players_override", StatusEntry::maxPlayersOverride).optional(),
            ModernSerializer.INSTANCE.forContext(GameVersion.MAX).entry("message", StatusEntry::message).optional(),
            PlayerInfo.SERIALIZER.listOf().entry("player_sample", StatusEntry::playerSample).optional(),
            Serializer.BOOLEAN.entry("secure_chat", StatusEntry::secureChat).orElse(false),
            Serializer.BOOLEAN.entry("preview_chat", StatusEntry::previewChat).orElse(false),
            Serializer.STRING.entry("passthrough_hostname", StatusEntry::passthroughHost).optional(),
            NumberSerializer.forInt(1,65535).entry("passthrough_port", StatusEntry::passthroughPort).optional(),
            WrappedRequirement.SERIALIZER.entry("requirement", StatusEntry::requirement).optional(),
            StatusEntry::new
    );


}
