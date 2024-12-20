package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mcore.lang.UnresolvedComponent;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdcfg.Either;
import dev.jwall64.mdproxy.requirement.ConnectionRequirement;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.registry.Registry;

import java.util.Collection;
import java.util.List;

public record Route(@Nullable Either<UnresolvedComponent, UnresolvedBackend> backend, @Nullable ConnectionRequirement requirement, boolean kickOnFail, String kickMessage) {

    private static final Logger LOGGER = LoggerFactory.getLogger("Route");

    public Collection<Identifier> getRequiredCookies() {

        if(requirement == null) return List.of();
        Collection<Identifier> out = requirement.getRequiredCookies();

        return out == null ? List.of() : out;

    }

    public TestResult canUse(ConnectionContext ctx) {

        if(requirement == null) return TestResult.PASS;
        return requirement.test(ctx);
    }

    public @Nullable Backend resolveBackend(ConnectionContext ctx, Registry<String, Backend> backends) {

        if(backend == null) {
            LOGGER.warn("Route has no backend!");
            return null;
        }

        Backend out;
        if(backend.hasLeft()) {
            String id = backend.leftOrThrow().resolveFlat(ctx.toPlaceholderContext());
            out = backends.get(id);
            if(out == null) {
                LOGGER.warn("No backend with ID {} was found!", id);
                return null;
            }
        } else {
            SerializeResult<Backend> res = backend.rightOrThrow().resolve(ctx.toPlaceholderContext());
            if(!res.isComplete()) {
                LOGGER.warn("Unable to resolve unresolved backend!", res.getError());
                return null;
            }
            out = res.getOrThrow();
        }
        return out;
    }

    public static final Serializer<Route> SERIALIZER = ObjectSerializer.create(
            Serializer.either(UnresolvedComponent.SERIALIZER, UnresolvedBackend.SERIALIZER).entry("backend", Route::backend).optional(),
            ConnectionRequirement.SERIALIZER.entry("requirement", Route::requirement).optional(),
            Serializer.BOOLEAN.entry("kick_on_fail", Route::kickOnFail).orElse(false),
            Serializer.STRING.entry("kick_message", Route::kickMessage).orElse("error.generic_route_failed"),
            Route::new
    );

}
