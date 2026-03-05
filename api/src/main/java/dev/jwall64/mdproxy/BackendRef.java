package dev.jwall64.mdproxy;

import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.util.MessageUtil;
import dev.jwall64.pseudonym.PartialMessage;

public interface BackendRef {

    Backend get(ConnectionContext ctx);

    public record Direct(Backend value) implements BackendRef { 
        @Override
        public Backend get(ConnectionContext ctx) {
            return value;
        }
    }

    public class ByName implements BackendRef {

        private final PartialMessage<String> name;
        private Backend cached;

        public ByName(PartialMessage<String> name) {
            this.name = name;
        }

        @Override
        public Backend get(ConnectionContext ctx) {
            cached = ctx.getProxy().getBackends().get(PartialMessage.resolve(name, ctx.toPipelineContext()));
            return cached;
        }

        public static final Serializer<ByName> SERIALIZER = MessageUtil.PARSE_SERIALIZER.flatMap(bn -> bn.name, ByName::new);
    }


    static Serializer<BackendRef> SERIALIZER = new Serializer<BackendRef>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, BackendRef value) {
            if(value instanceof ByName bn) {
                return ByName.SERIALIZER.serialize(ctx, bn);
            }
            if(value instanceof UnresolvedBackend ub) {
                return UnresolvedBackend.SERIALIZER.serialize(ctx, ub);
            }
            return SerializeResult.failure("Don't know how to serialize " + value);
        }

        @Override
        public <O> SerializeResult<BackendRef> deserialize(SerializeContext<O> ctx, O value) {
            if(ctx.isString(value)) {
                return ByName.SERIALIZER.deserialize(ctx, value).flatMap(bn -> bn);
            }
            return UnresolvedBackend.SERIALIZER.deserialize(ctx, value).flatMap(ub -> ub);
        }
    };

}
