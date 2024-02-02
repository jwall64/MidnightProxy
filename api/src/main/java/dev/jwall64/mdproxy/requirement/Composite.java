package dev.jwall64.mdproxy.requirement;


import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.ConnectionContext;
import dev.jwall64.midnightlib.math.Range;
import dev.jwall64.midnightlib.registry.Identifier;
import dev.jwall64.midnightlib.requirement.CompositeCheck;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Composite extends ConnectionCheck {
    private final Range<Integer> range;
    private final List<ConnectionRequirement> values;

    public Composite(Range<Integer> range, Collection<ConnectionRequirement> values) {
        super(false, null);
        this.range = range;
        this.values = List.copyOf(values);
    }
    @Override
    public boolean requiresAuth() {
        for(ConnectionRequirement r : values) {
            if(r.requiresAuth()) return true;
        }
        return false;
    }
    @Override
    public Collection<Identifier> getRequiredCookies() {
        Set<Identifier> out = new HashSet<>();
        for(ConnectionRequirement r : values) {
            Collection<Identifier> cookies = r.getRequiredCookies();
            if(cookies != null) out.addAll(cookies);
        }
        return out;
    }
    @Override
    public boolean test(ConnectionContext conn) {
        return CompositeCheck.checkAll(range, values, conn);
    }

    public static Serializer<ConnectionCheck> serializer(Serializer<ConnectionRequirement> serializer) {
        return CompositeCheck.serializer(c -> ((Composite) c).range, c -> ((Composite) c).values, Composite::new, serializer);
    }

}
