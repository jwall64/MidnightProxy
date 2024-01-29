package dev.jwall64.mdproxy.command;

import dev.jwall64.mdproxy.Proxy;

public interface CommandExecutor {

    void execute(Proxy proxy, String[] args);

}
