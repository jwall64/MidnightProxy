package dev.jwall64.mdproxy.whitelist;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.Tuples.T2;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.mdcfg.sql.Column;
import dev.jwall64.mdcfg.sql.ColumnRef;
import dev.jwall64.mdcfg.sql.Constraint;
import dev.jwall64.mdcfg.sql.DataType;
import dev.jwall64.mdcfg.sql.QueryResult;
import dev.jwall64.mdcfg.sql.SQLConnection;
import dev.jwall64.mdcfg.sql.TableConstraint;
import dev.jwall64.mdcfg.sql.TableSchema;
import dev.jwall64.mdcfg.sql.QueryResult.Row;
import dev.jwall64.mdcfg.sql.stmt.Select.JoinType;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.sql.SQLPlugin;

public class Tables {


    public static final String WHITELIST_NAME = "whitelist";
    public static final String WHITELIST_PLAYER_NAME = "whitelist_player";

    public static final TableSchema WHITELIST = TableSchema.builder()
        .withColumn(Column.builder("wId", DataType.INTEGER).withConstraint(Constraint.PRIMARY_KEY))
        .withColumn(Column.builder("name", DataType.VARCHAR(255)).withConstraint(Constraint.NOT_NULL))
        .build();

    public static final TableSchema WHITELIST_PLAYER = TableSchema.builder()
        .withColumn(Column.builder("wId", DataType.INTEGER).withConstraint(Constraint.NOT_NULL))
        .withColumn(Column.builder("uuid", DataType.BLOB(16)))
        .withColumn(Column.builder("username", DataType.VARCHAR(16)))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("wId", new ColumnRef(WHITELIST_NAME, "wId")))
        .build();


    public static void reload(Proxy proxy, Registry<String, Whitelist> lists, ConfigSection db) {
        SQLPlugin plugin = proxy.getPluginManager().get(SQLPlugin.class);
        plugin.getRegistry().connect(db).thenAccept(sql -> {
            if(!setupDatabase(sql)) return;
            enumerateWhitelists(sql).forEach(lists::register);
        });
    }

    public static boolean setupDatabase(SQLConnection connection) {

        return connection.createTable(WHITELIST_NAME, WHITELIST).ifNotExists(true).execute()
            && connection.createTable(WHITELIST_PLAYER_NAME, WHITELIST_PLAYER).ifNotExists(true).execute();
    }

    public static Map<String, Whitelist> enumerateWhitelists(SQLConnection connection) {

        QueryResult res = connection.select(WHITELIST_PLAYER_NAME).join(JoinType.INNER, WHITELIST_NAME, "wId").execute();

        Map<String, Whitelist.Builder> lists = new HashMap<>();

        for(int i = 0 ; i < res.rows() ; i++) {
            Row row = res.get(i);

            String name = row.getString("name");
            ByteBuffer uuidBytes = row.getBlob("uuid");
            String username = row.getString("username");

            Whitelist.Builder w = lists.computeIfAbsent(name, str -> Whitelist.builder());
            if(uuidBytes != null) {
                long msb = uuidBytes.getLong();
                long lsb = uuidBytes.getLong(8);
                w.add(new UUID(msb,lsb));
            }
            if(username != null) {
                w.add(username);
            }
        }

        return lists.entrySet()
            .stream()
            .map(e -> new T2<>(e.getKey(), e.getValue().build()))
            .collect(Collectors.toUnmodifiableMap(t -> t.p1, t -> t.p2));
    }


}
