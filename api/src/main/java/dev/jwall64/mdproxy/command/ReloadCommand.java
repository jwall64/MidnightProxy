package dev.jwall64.mdproxy.command;

import dev.jwall64.mdproxy.Proxy;

public class ReloadCommand implements CommandExecutor {
    @Override
    public void execute(Proxy proxy, String[] args) {
        proxy.reload();
        System.out.println("Reloaded proxy configuration");
    }
}
