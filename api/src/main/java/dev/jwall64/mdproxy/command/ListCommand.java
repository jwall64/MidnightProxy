package dev.jwall64.mdproxy.command;

import dev.jwall64.mdproxy.ClientConnection;

import java.util.UUID;

public class ListCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender sender, String[] args) {

        sender.sendMessage("Clients connected:");
        sender.getProxy().getClientIds().forEach(uuid -> {
            ClientConnection conn = sender.getProxy().getConnection(uuid);
            sender.sendMessage(" - " + conn.username() + ": " + sender.getProxy().getBackends().getId(conn.getBackendConnection().getBackend()));
        });
    }
}
