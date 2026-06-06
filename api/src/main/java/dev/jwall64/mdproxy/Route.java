package dev.jwall64.mdproxy;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.serializer.InlineSerializer;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.requirement.ConnectionRequirement;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdcfg.registry.Registry;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public record Route(BackendSet backends, @Nullable ConnectionRequirement requirement, boolean kickOnFail, String kickMessage, BalanceStrategy balanceStrategy, int priority) implements Comparable<Route> {

    public Route(BackendSet backends, @Nullable ConnectionRequirement requirement, boolean kickOnFail, String kickMessage, BalanceStrategy balanceStrategy) {
        this(backends, requirement, kickOnFail, kickMessage, balanceStrategy, 0);
    }

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

    public @Nullable Backend resolveBackend(ConnectionContext ctx, Registry<String, Backend> registry) {
        if(backends == null) return null;
        return backends.next(ctx, registry, balanceStrategy);
    }

    @Override
    public int compareTo(Route other) {
        return Integer.compare(priority, other.priority);
    }

    public static final Serializer<Route> SERIALIZER = ObjectSerializer.create(
            BackendSet.SERIALIZER.entry("backend", Route::backends).optional(),
            ConnectionRequirement.SERIALIZER.entry("requirement", Route::requirement).optional(),
            Serializer.BOOLEAN.entry("kick_on_fail", Route::kickOnFail).orElse(false),
            Serializer.STRING.entry("kick_message", Route::kickMessage).orElse("error.generic_route_failed"),
            BalanceStrategy.SERIALIZER.entry("balance_strategy", Route::balanceStrategy).orElse(BalanceStrategy.ROUND_ROBIN),
            Route::new
    );

    public enum BalanceStrategy {
        ROUND_ROBIN("round_robin"),
        MIN_CONNECTIONS("min_connections");

        String id;
        BalanceStrategy(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static BalanceStrategy byId(String id) {
            for(BalanceStrategy b : values()) {
                if(b.id.equals(id)) {
                    return b;
                }
            }
            return null;
        }

        public static final Serializer<BalanceStrategy> SERIALIZER = InlineSerializer.of(BalanceStrategy::id, BalanceStrategy::byId);
    };

    public static class BackendSet {
        private int lastUsed;
        private final List<BackendRef> backends;
        private final List<AtomicInteger> connectionCounts;

        public BackendSet(List<BackendRef> backends) {
            this.backends = backends;
            this.connectionCounts = backends.stream().map(e -> new AtomicInteger()).toList();
        }

        private int nextIndex(ConnectionContext ctx, BalanceStrategy strategy) {

            if(strategy == BalanceStrategy.MIN_CONNECTIONS) {
                int min = 0;
                int index = 0;
                for(int i = 0 ; i < connectionCounts.size() ; i++) {
                    int count = connectionCounts.get(i).get();
                    if(count < min) {
                        index = i;
                        min = count;
                    }
                }

                final int finalIndex = index;
                connectionCounts.get(finalIndex).incrementAndGet();
                ctx.getConnection().disconnectEvent().register(this, t2 -> {
                    connectionCounts.get(finalIndex).decrementAndGet();
                });

                return finalIndex;
            } else {
                lastUsed = (lastUsed + 1) % backends.size();
                return lastUsed;
            } 
        }

        @Deprecated
        public Backend next(ConnectionContext ctx, Registry<String, Backend> registry, BalanceStrategy strategy) {
            return next(ctx, strategy);
        }

        public Backend next(ConnectionContext ctx, BalanceStrategy strategy) {

            if(backends.isEmpty()) {
                LOGGER.warn("Route has no backend!");
                return null;
            }

            int index = nextIndex(ctx, strategy);
            return backends.get(index).get(ctx);
        }

        public String toString() {
            return String.join(", ", backends.stream().map(Objects::toString).toList());
        }


        public static final Serializer<BackendSet> SERIALIZER = BackendRef.SERIALIZER.listOf().mapToList()
            .flatMap(bs -> bs.backends, BackendSet::new)
            .or(BackendRef.SERIALIZER
                .map(
                    bs -> SerializeResult.failure("Cannot serialize single!"), 
                    ref -> SerializeResult.success(new BackendSet(List.of(ref))
                )
            ));

    }

}
