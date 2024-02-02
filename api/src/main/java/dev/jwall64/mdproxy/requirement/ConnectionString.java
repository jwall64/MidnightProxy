package dev.jwall64.mdproxy.requirement;

import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ClientConnection;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.requirement.StringCheck;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class ConnectionString extends ConnectionCheck {

    private final Function<ConnectionContext, String> getter;
    private final Set<String> values;
    public ConnectionString(Function<ConnectionContext, String> getter, Collection<String> coll, boolean requireAuth, boolean requireLocale) {
        super(requireAuth, null);
        this.getter = getter;
        this.values = Set.copyOf(coll);
    }

    @Override
    public boolean test(ConnectionContext conn) {
        return values.contains(getter.apply(conn));
    }

    public static Serializer<ConnectionString> serializer(Function<ConnectionContext, String> getter, boolean requireAuth, boolean requireLocale) {
        return StringCheck.serializer(as -> as.values, values -> new ConnectionString(getter, values, requireAuth, requireLocale));
    }
}
