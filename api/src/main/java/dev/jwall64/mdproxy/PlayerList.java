package dev.jwall64.mdproxy;

import dev.jwall64.mcore.text.Component;

import java.util.UUID;
import java.util.stream.Stream;

public interface PlayerList extends PlayerCountProvider {

    ClientConnection getPlayer(UUID uuid);

    void removePlayer(UUID uuid);
    void removePlayer(UUID uuid, Component message);

    Stream<UUID> getPlayerIds();

}
