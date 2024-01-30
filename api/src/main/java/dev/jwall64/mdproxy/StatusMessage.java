package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mcore.GameVersion;
import dev.jwall64.mcore.text.Component;
import dev.jwall64.mcore.text.ModernSerializer;
import dev.jwall64.mdcfg.ConfigSection;

import java.util.Collection;

public record StatusMessage(GameVersion version, int playersOnline, int maxPlayers, Collection<PlayerInfo> playerSample,
                            Component message, @Nullable String favicon, boolean secureChat, boolean previewChat) {

    public ConfigSection serialize() {

        return new ConfigSection()
                .with("version", new ConfigSection()
                        .with("name", version.getId())
                        .with("protocol", version.getProtocolVersion())
                )
                .with("players", new ConfigSection()
                        .with("max", maxPlayers)
                        .with("online", playersOnline)
                        .with("sample", playerSample, PlayerInfo.SERIALIZER.filteredListOf())
                )
                .with("description", message, ModernSerializer.INSTANCE.forContext(version))
                .with("favicon", favicon)
                .with("enforcesSecureChat", secureChat)
                .with("previewsChat", previewChat);
    }


}
