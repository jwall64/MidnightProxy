package dev.jwall64.mdproxy.lastsrv;

import dev.jwall64.mdcfg.sql.DataType;
import dev.jwall64.mdcfg.sql.SQLConnection;
import dev.jwall64.mdcfg.sql.TableSchema;

public class Tables {

    public static final String TABLE_NAME = "last_servers";

    public static final TableSchema TABLE_SCHEMA =
        TableSchema.builder()
            .withColumn("player", DataType.BINARY(16))
            .withColumn("server", DataType.VARCHAR(1024))
            .build();

    public void init(SQLConnection connection) {

        if (!connection.hasTable(TABLE_NAME)) {
            connection.createTable(TABLE_NAME, TABLE_SCHEMA).execute();
        }
    }
}
