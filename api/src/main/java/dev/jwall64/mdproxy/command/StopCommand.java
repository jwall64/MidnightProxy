package dev.jwall64.mdproxy.command;

import dev.jwall64.mdproxy.Proxy;

public class StopCommand implements CommandExecutor {
    @Override
    public void execute(Proxy proxy, String[] args) {
        proxy.shutdown();
    }
}
