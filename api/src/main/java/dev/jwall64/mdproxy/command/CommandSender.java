package dev.jwall64.mdproxy.command;

import dev.jwall64.mdproxy.Proxy;

public interface CommandSender {

    Proxy getProxy();

    void sendMessage(String message);

}
