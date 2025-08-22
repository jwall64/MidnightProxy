package dev.jwall64.mdproxy.lastsrv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.sql.DataType;
import dev.jwall64.mdcfg.sql.DatabasePreset;
import dev.jwall64.mdcfg.sql.SQLConnection;
import dev.jwall64.mdproxy.BackendConnection;
import dev.jwall64.mdproxy.DataManager;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;
import dev.jwall64.mdproxy.sql.SQLPlugin;

public class LastServerPlugin implements Plugin {

    public static final Logger LOGGER = LoggerFactory.getLogger("LastServer");
    private static final ConfigSection DEFAULT_CONFIG =
        new ConfigSection().with("db", new ConfigSection()
                                           .with("preset", "default")
                                           .with("table_prefix", "mdp_"));

    private final ConfigSection dbConfig;

    public LastServerPlugin() {

        ConnectionCheckType.REGISTRY.tryRegister("last_server",
                                                 LastServerCheck.TYPE);
    }

    public CompletableFuture<SQLConnection> connectDatabase(Proxy proxy) {
        SQLPlugin sql = proxy.getPluginManager().getPlugin(SQLPlugin.class);
        return sql.getRegistry().connect(dbConfig);
    }

    @Override
    public void initialize(Proxy proxy) {

        Path configFolder =
            proxy.getPluginManager().configFolder().resolve("lastserver");
        try {
            Files.createDirectories(configFolder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create config directory", e);
        }

        FileWrapper<ConfigObject> config =
            proxy.fileCodecRegistry().findOrCreate(
                ConfigContext.INSTANCE, "config", configFolder, DEFAULT_CONFIG);
        this.dbConfig = config.getRoot().asSection().getSection("db");

        proxy.getPluginManager()
            .getPlugin(SQLPlugin.class)
            .getRegistry()
            .connect(dbConfig)
            .thenAccept(sql -> { Tables.init(sql); });

        proxy.clientDisconnectEvent().register(this, client -> {
            BackendConnection conn = client.getBackendConnection();
            if (conn == null)
                return;

            String id = conn.getBackendId(proxy);
            if (id == null)
                return;

            connectDatabase(proxy.thenAccept(sql -> {
                UUID uid = client.uuid();
                ByteBuffer uuidBuf = ByteBuffer.allocate(16);
                uuidBuf.putLong(0, uid.getMostSignificantBits());
                uuidBuf.putLong(8, uid.getLeastSignificantBits());
                DataValue dv = DataType.BLOB.create(uuidBuf);

                sql.delete(Tables.TABLE_NAME)
                    .where(Condition.equals("uuid", dv))
                    .execute();
                sql.insert(Tables.TABLE_NAME, Tables.TABLE_SCHEMA)
                    .addRow(List.of(dv, DataType.VARCHAR.create(id)))
                    .execute();
            });
        });
    }
}
