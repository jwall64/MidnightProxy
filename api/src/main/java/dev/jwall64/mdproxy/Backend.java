package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.serializer.NumberSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

public record Backend(String hostname, int port, boolean redirect) {
    public static final Serializer<Backend> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("hostname", Backend::hostname),
            NumberSerializer.forInt(1, 65535).entry("port", Backend::port).orElse(25565),
            Serializer.BOOLEAN.entry("redirect", Backend::redirect).orElse(false),
            Backend::new
    );

}
