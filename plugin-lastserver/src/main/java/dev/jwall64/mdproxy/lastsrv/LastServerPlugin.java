package dev.jwall64.mdproxy.lastsrv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mcore.MidnightCoreAPI;
import dev.jwall64.mcore.data.DataManager;
import dev.jwall64.mdproxy.BackendConnection;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.requirement.ConnectionCheckType;

import java.io.File;

public class LastServerPlugin implements Plugin {

    public static final Logger LOGGER = LoggerFactory.getLogger("LastServer");
    private final DataManager dataManager;

    public LastServerPlugin() {
        File configFolder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("lastserver").toFile();
        if(!configFolder.isDirectory() && !configFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }
        dataManager = new DataManager(configFolder);

        ConnectionCheckType.REGISTRY.register("last_server", LastServerCheck.TYPE);
    }

    @Override
    public void initialize(Proxy proxy) {
        proxy.clientDisconnectEvent().register(this, client -> {

            BackendConnection conn = client.getBackendConnection();
            if(conn == null) return;

            String id = conn.getBackendId(proxy);
            if(id == null) return;

            dataManager.getData(client.uuid().toString()).set("last_server", id);
            dataManager.save(client.uuid().toString());
        });

    }

    public DataManager getDataManager() {
        return dataManager;
    }
}