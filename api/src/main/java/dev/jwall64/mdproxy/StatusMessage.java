package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.pseudonym.text.Component;
import dev.jwall64.pseudonym.text.ProtocolContext;

import java.util.Collection;

public record StatusMessage(GameVersion version, int playersOnline, int maxPlayers, Collection<PlayerInfo> playerSample,
                            Component message, @Nullable String favicon, boolean secureChat, boolean previewChat) {

    public ConfigSection serialize() {

        return new ConfigSection()
                .with("version", new ConfigSection()
                        .with("name", version.name())
                        .with("protocol", version.protocolVersion())
                )
                .with("players", new ConfigSection()
                        .with("max", maxPlayers)
                        .with("online", playersOnline)
                        .with("sample", playerSample, PlayerInfo.SERIALIZER.filteredListOf())
                )
                .with("description", Component.SERIALIZER.serialize(new ProtocolContext<>(ConfigContext.INSTANCE, version.protocolVersion()), message).getOrThrow())
                .with("favicon", favicon)
                .with("enforcesSecureChat", secureChat)
                .with("previewsChat", previewChat);
    }


}
