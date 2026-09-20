package dev.jwall64.mdproxy.whitelist;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
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


    public static final String META_NAME = "meta";
    public static final String WHITELIST_NAME = "whitelist";
    public static final String WHITELIST_PLAYER_NAME = "whitelist_player";


    public static final TableSchema META = TableSchema.builder()
        .withColumn("schema_version", DataType.INTEGER)
        .build();

    public static final TableSchema WHITELIST = TableSchema.builder()
        .withColumn(Column.builder("wId", DataType.INTEGER).withConstraint(Constraint.PRIMARY_KEY).withConstraint(Constraint.AUTO_INCREMENT))
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
            if(!setupDatabase(sql)) {
                return;
            }
            enumerateWhitelists(sql).forEach(lists::register);
        });
    }

    public static boolean setupDatabase(SQLConnection connection) {

        if(connection.hasTable(META_NAME)) {
            QueryResult result = connection.select(META_NAME).withColumn("schema_version").execute();
            if(result.rows() >= 1) {
                int schemaVersion = result.get(0).getInt(0);
                if(schemaVersion == 1) return true;
            }
            if(connection.insert(META_NAME, META).addRow(List.of(DataType.INTEGER.create(1))).execute().getAllAffectedRows().sum() == 0) {
                return false;
            }
        } else {
            connection.createTable(META_NAME, META).execute();
            if(connection.insert(META_NAME, META).addRow(List.of(DataType.INTEGER.create(1))).execute().getAllAffectedRows().sum() == 0) {
                return false;
            }
        }

        connection.createTable(WHITELIST_NAME, WHITELIST).execute();
        connection.createTable(WHITELIST_PLAYER_NAME, WHITELIST_PLAYER).execute();
        return true;
    }

    public static Map<String, Whitelist> enumerateWhitelists(SQLConnection connection) {

        QueryResult res = connection.select(WHITELIST_PLAYER_NAME).join(JoinType.RIGHT, WHITELIST_NAME, "wId").execute();

        Map<String, Whitelist.Builder> lists = new HashMap<>();


        for(int i = 0 ; i < res.rows() ; i++) {
            try {
                Row row = res.get(i);

                String name = row.getString("name");

                ByteBuffer uuidBytes = (ByteBuffer) row.getValue("uuid");
                String username = (String) row.getValue("username");

                Whitelist.Builder w = lists.computeIfAbsent(name, str -> {
                    return Whitelist.builder();
                });
                if(uuidBytes != null) {
                    long msb = uuidBytes.getLong();
                    long lsb = uuidBytes.getLong(8);
                    w.add(new UUID(msb,lsb));
                }
                else if(username != null) {
                    w.add(username);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return lists.entrySet()
            .stream()
            .map(e -> new T2<>(e.getKey(), e.getValue().build()))
            .collect(Collectors.toUnmodifiableMap(t -> t.p1, t -> t.p2));
    }


}
