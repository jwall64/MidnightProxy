package dev.jwall64.mdproxy.sql;

import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.sql.Column;
import dev.jwall64.mdcfg.sql.ColumnRef;
import dev.jwall64.mdcfg.sql.Constraint;
import dev.jwall64.mdcfg.sql.DataType;
import dev.jwall64.mdcfg.sql.QueryResult;
import dev.jwall64.mdcfg.sql.SQLConnection;
import dev.jwall64.mdcfg.sql.TableConstraint;
import dev.jwall64.mdcfg.sql.TableSchema;
import dev.jwall64.mdproxy.Route;
import dev.jwall64.mdproxy.requirement.ConnectionRequirement;

public class StorageTables {

    public static final int SCHEMA_VERSION = 1;
    
    public static final String META_NAME = "meta";
    public static final String BACKEND_NAME = "backend";
    public static final String ROUTE_NAME = "route";
    public static final String STATUS_ENTRY_NAME = "status_entry";
    public static final String AUTH_ROUTE_NAME = "auth_route";
    public static final String REQUIREMENT_NAME = "requirement";
    public static final String BACKEND_REF_NAME = "backend_ref";
    public static final String ROUTE_BACKEND_REF_NAME = "route_backend_ref";


    public static final TableSchema META = TableSchema.builder()
        .withColumn("schemaVersion", DataType.INTEGER)
        .build();


    public static final TableSchema BACKEND = TableSchema.builder()
        .withColumn(Column.builder("bId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn(Column.builder("name", DataType.VARCHAR(4096)).withConstraint(Constraint.UNIQUE))
        .withColumn(Column.builder("hostname", DataType.VARCHAR(4096)).withConstraint(Constraint.DEFAULT(DataType.VARCHAR.create("localhost"))))
        .withColumn(Column.builder("port", DataType.INTEGER).withConstraint(Constraint.DEFAULT(DataType.INTEGER.create(25565))))
        .withColumn(Column.builder("redirect", DataType.BOOLEAN).withConstraint(Constraint.DEFAULT(DataType.BOOLEAN.create(false))))
        .withColumn(Column.builder("haproxy", DataType.BOOLEAN).withConstraint(Constraint.DEFAULT(DataType.BOOLEAN.create(false))))
        .withTableConstraint(TableConstraint.PRIMARY_KEY("bId"))
        .build();


    public static final TableSchema ROUTE = TableSchema.builder()
        .withColumn(Column.builder("rId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn("rqId", DataType.INTEGER)
        .withColumn(Column.builder("kickOnFail", DataType.BOOLEAN).withConstraint(Constraint.DEFAULT(DataType.BOOLEAN.create(false))))
        .withColumn("kickMessage", DataType.VARCHAR(4096))
        .withColumn(Column.builder("balance_strategy", DataType.INTEGER).withConstraint(Constraint.DEFAULT(DataType.INTEGER.create(Route.BalanceStrategy.ROUND_ROBIN.ordinal()))))
        .withTableConstraint(TableConstraint.PRIMARY_KEY("rId"))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("rqId", new ColumnRef(REQUIREMENT_NAME, "rqId")))
        .build();


    public static final TableSchema STATUS_ENTRY = TableSchema.builder()
        .withColumn(Column.builder("sId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn(Column.builder("priority", DataType.INTEGER).withConstraint(Constraint.DEFAULT(DataType.INTEGER.create(0))))
        .withColumn("playersOverride", DataType.INTEGER)
        .withColumn("maxPlayersOverride", DataType.INTEGER)
        .withColumn("playerSample", DataType.VARCHAR(4096))
        .withColumn("message", DataType.VARCHAR(4096))
        .withColumn("icon", DataType.VARCHAR(4096))
        .withColumn("secureChat", DataType.BOOLEAN)
        .withColumn("previewChat", DataType.BOOLEAN)
        .withColumn("passthrough", DataType.VARCHAR(4096))
        .withColumn("rqId", DataType.INTEGER)
        .withTableConstraint(TableConstraint.PRIMARY_KEY("sId"))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("rqId", new ColumnRef(REQUIREMENT_NAME, "rqId")))
        .build();


    public static final TableSchema AUTH_ROUTE = TableSchema.builder()
        .withColumn(Column.builder("arId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn("type", DataType.VARCHAR(4096))
        .withColumn("rqId", DataType.INTEGER)
        .withColumn(Column.builder("kickOnFail", DataType.BOOLEAN).withConstraint(Constraint.DEFAULT(DataType.BOOLEAN.create(false))))
        .withColumn(Column.builder("kickMessage", DataType.VARCHAR(4096)))
        .withTableConstraint(TableConstraint.PRIMARY_KEY("sId"))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("rqId", new ColumnRef(REQUIREMENT_NAME, "rqId")))
        .build();


    public static final TableSchema REQUIREMENT = TableSchema.builder()
        .withColumn(Column.builder("rqId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn("config", DataType.VARCHAR(8192))
        .withTableConstraint(TableConstraint.PRIMARY_KEY("rqId"))
        .build();


    public static final TableSchema BACKEND_REF = TableSchema.builder()
        .withColumn(Column.builder("brId", DataType.INTEGER).withConstraint(Constraint.AUTO_INCREMENT).withConstraint(Constraint.NOT_NULL))
        .withColumn(Column.builder("indirect", DataType.BOOLEAN).withConstraint(Constraint.DEFAULT(DataType.BOOLEAN.create(false))))
        .withColumn("bId", DataType.INTEGER)
        .withColumn("indirectValue", DataType.VARCHAR(4096))
        .withTableConstraint(TableConstraint.PRIMARY_KEY("brId"))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("bId", new ColumnRef(BACKEND_NAME, "bId")))
        .build();

    public static final TableSchema ROUTE_BACKEND_REF = TableSchema.builder()
        .withColumn("rId", DataType.INTEGER)
        .withColumn("brId", DataType.INTEGER)
        .withTableConstraint(TableConstraint.FOREIGN_KEY("rId", new ColumnRef(ROUTE_NAME, "rId")))
        .withTableConstraint(TableConstraint.FOREIGN_KEY("brId", new ColumnRef(BACKEND_REF_NAME, "brId")))
        .build();


    public static boolean init(SQLConnection connection) {

        if(!connection.hasTable(META_NAME)) {

            connection.doTransaction(c1 -> {

                if(!c1.createTable(META_NAME, META).execute() ||
                    !c1.createTable(BACKEND_NAME, BACKEND).execute() ||
                    !c1.createTable(ROUTE_NAME, ROUTE).execute() ||
                    !c1.createTable(STATUS_ENTRY_NAME, STATUS_ENTRY).execute() ||
                    !c1.createTable(AUTH_ROUTE_NAME, AUTH_ROUTE).execute() ||
                    !c1.createTable(REQUIREMENT_NAME, REQUIREMENT).execute() ||
                    !c1.createTable(BACKEND_REF_NAME, BACKEND_REF).execute() ||
                    !c1.createTable(ROUTE_BACKEND_REF_NAME, ROUTE_BACKEND_REF).execute()
                ) return false;


                if(c1.insert(META_NAME, new ConfigSection().with("schemaVersion", SCHEMA_VERSION)).execute().getAllAffectedRows().sum() != 1) {
                    return false;
                }

                if(c1.insert(AUTH_ROUTE_NAME, new ConfigSection().with("type", "mojang")).execute().getAllAffectedRows().sum() != 1) {
                    return false;
                }

                return true;

            });

        }

        QueryResult metaRes = connection.select(META_NAME).execute();
        if(metaRes.rows() == 0 || metaRes.get(0).getInt(0) != SCHEMA_VERSION) {
            return false;
        }

        return true;
    }

    public static SerializeResult<ConnectionRequirement> createRequirement(QueryResult res) {

        if(res.rows() == 0) return SerializeResult.failure("No rows");
        return ConnectionRequirement.SERIALIZER.deserialize(ConfigContext.INSTANCE, 
                JSONCodec.loadConfig(res.get(0).getString("config")));

    }


}


