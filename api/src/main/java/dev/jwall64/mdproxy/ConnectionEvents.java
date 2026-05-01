package dev.jwall64.mdproxy;

import dev.jwall64.midnightlib.event.HandlerList;

public interface ConnectionEvents {

    HandlerList<PreLogin> preLogin();
    HandlerList<PostLogin> postLogin();
    HandlerList<EnterConfiguration> enterConfiguration();
    HandlerList<PreConnectBackend> preConnectBackend();
    HandlerList<PostConnectBackend> postConnectBackend();
    HandlerList<Disconnect> disconnect();

    public record PreLogin(ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }

    public record PostLogin(ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }

    public record EnterConfiguration(Backend backend, ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }

    public record PreConnectBackend(Backend backend, ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }

    public record PostConnectBackend(Backend backend, ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }

    public record Disconnect(Backend backend, ConnectionContext context) {
        ClientConnection connection() {
            return context.getConnection();
        }
    }


}
