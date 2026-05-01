package dev.jwall64.mdproxy;

import java.util.Collections;
import java.util.Map;

import dev.jwall64.mdcfg.serializer.NumberSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

public record Backend(String hostname, int port, boolean redirect, boolean haproxy, Map<String, String> meta) {

    public Backend(String hostname, int port, boolean redirect, boolean haproxy) {
        this(hostname, port, redirect, haproxy, Collections.emptyMap());
    }

    public static final Serializer<Backend> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("hostname", Backend::hostname),
            NumberSerializer.forInt(1, 65535).entry("port", Backend::port).orElse(25565),
            Serializer.BOOLEAN.entry("redirect", Backend::redirect).orElse(false),
            Serializer.BOOLEAN.entry("haproxy", Backend::haproxy).orElse(false),
            Serializer.STRING.mapOf().entry("meta", Backend::meta).orElse(Collections.emptyMap()),
            Backend::new
    );

}
