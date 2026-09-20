package dev.jwall64.mdproxy.whitelist;

import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ClientConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Whitelist {

    private final List<UUID> uuids = new ArrayList<>();
    private final List<String> usernames = new ArrayList<>();

    private boolean invalid = false;

    public boolean isWhitelisted(ClientConnection conn) {
        return uuids.contains(conn.uuid()) || usernames.contains(conn.username());
    }

    public Whitelist(Collection<UUID> uuids, Collection<String> usernames) {
        if(uuids != null) {
            this.uuids.addAll(uuids);
        }
        if(usernames != null) {
            this.usernames.addAll(usernames);
        }
    }

    void invalidate() {
        this.invalid = true;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public static final Serializer<Whitelist> SERIALIZER = ObjectSerializer.create(
            Serializer.UUID.listOf().<Whitelist>entry("uuids", wl -> wl.uuids).optional(),
            Serializer.STRING.listOf().<Whitelist>entry("names", wl -> wl.usernames).optional(),
            Whitelist::new
    );

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        public Builder() { }

        private final List<UUID> uuids = new ArrayList<>();
        private final List<String> usernames = new ArrayList<>();

        public Builder add(UUID uuid) { uuids.add(uuid); return this; }
        public Builder add(String username) { usernames.add(username); return this; }

        public Whitelist build() { return new Whitelist(uuids, usernames); }

    }


}
