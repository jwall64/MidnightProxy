package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.util.MessageUtil;
import dev.jwall64.pseudonym.PipelineContext;
import dev.jwall64.pseudonym.PartialMessage;

public record UnresolvedBackend(PartialMessage<String> hostname, @Nullable PartialMessage<String> port, @Nullable PartialMessage<String> redirect, @Nullable PartialMessage<String> haproxy) implements BackendRef {


    public Backend get(ConnectionContext ctx) {
        return resolve(ctx.toPipelineContext()).getOrThrow();
    }


    public SerializeResult<Backend> resolve(PipelineContext ctx) {

        String hostname = PartialMessage.resolve(this.hostname, ctx);
        int port = 25565;

        try {
            if (this.port != null) {
                try {
                    port = Integer.parseInt(PartialMessage.resolve(this.port, ctx));
                } catch (NumberFormatException e) {
                    return SerializeResult.failure("Unable to resolve port! Not a number");
                }
            }

            if(port < 1 || port > 65535) {
                return SerializeResult.failure("Unable to resolve port! Number not in the range [1,65535]");
            }

        } catch (NumberFormatException ex) {
            return SerializeResult.failure("Unable to resolve port! Invalid number!");
        }


        boolean redirect = false;
        if (this.redirect != null) {
            redirect = Boolean.parseBoolean(PartialMessage.resolve(this.redirect, ctx));
        }

        boolean haproxy = false;
        if (this.haproxy != null) {
            haproxy = Boolean.parseBoolean(PartialMessage.resolve(this.haproxy, ctx));
        }

        return SerializeResult.success(new Backend(hostname, port, redirect, haproxy));
    }

    public static final Serializer<UnresolvedBackend> SERIALIZER = ObjectSerializer.create(
            MessageUtil.PARSE_SERIALIZER.entry("hostname", UnresolvedBackend::hostname),
            MessageUtil.PARSE_SERIALIZER.entry("port", UnresolvedBackend::port).optional(),
            MessageUtil.PARSE_SERIALIZER.entry("redirect", UnresolvedBackend::redirect).optional(),
            MessageUtil.PARSE_SERIALIZER.entry("haproxy", UnresolvedBackend::haproxy).optional(),
            UnresolvedBackend::new
    );

}
