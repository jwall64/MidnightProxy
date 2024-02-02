package dev.jwall64.mdproxy.requirement;

import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.math.Range;
import dev.jwall64.midnightlib.requirement.NumberCheck;

import java.util.function.Function;

public class ConnectionInt extends ConnectionCheck {

    private final Range<Integer> valid;
    private final Function<ConnectionContext, Integer> getter;

    public ConnectionInt(Function<ConnectionContext, Integer> getter, Range<Integer> valid, boolean requireAuth) {
        super(requireAuth, null);
        this.valid = valid;
        this.getter = getter;
    }

    @Override
    public boolean test(ConnectionContext conn) {
        return valid.isWithin(getter.apply(conn));
    }

    public static Serializer<ConnectionInt> serializer(Function<ConnectionContext, Integer> getter, boolean requireAuth) {
        return NumberCheck.serializer(Range.INTEGER, prt -> prt.valid, valid -> new ConnectionInt(getter, valid, requireAuth));
    }
}
