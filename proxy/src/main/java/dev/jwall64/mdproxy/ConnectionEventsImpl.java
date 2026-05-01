package dev.jwall64.mdproxy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import dev.jwall64.midnightlib.event.ConcurrentHandlerList;

public class ConnectionEventsImpl implements ConnectionEvents {

    private final ConcurrentHandlerList<PreLogin> preLogin;
    private final ConcurrentHandlerList<PostLogin> postLogin;
    private final ConcurrentHandlerList<EnterConfiguration> enterConfiguration;
    private final ConcurrentHandlerList<PreConnectBackend> preConnectBackend;
    private final ConcurrentHandlerList<PostConnectBackend> postConnectBackend;
    private final ConcurrentHandlerList<Disconnect> disconnect;

    public ConnectionEventsImpl(Executor executor) {
        preLogin = new ConcurrentHandlerList<>(executor);
        postLogin = new ConcurrentHandlerList<>(executor);
        enterConfiguration = new ConcurrentHandlerList<>(executor);
        preConnectBackend = new ConcurrentHandlerList<>(executor);
        postConnectBackend = new ConcurrentHandlerList<>(executor);
        disconnect = new ConcurrentHandlerList<>(executor);

    }

    public CompletableFuture<Void> preLogin(ConnectionContext context) {
        return preLogin.invokeAsync(new PreLogin(context));
    }

    public CompletableFuture<Void> postLogin(ConnectionContext context) {
        return postLogin.invokeAsync(new PostLogin(context));
    }

    public CompletableFuture<Void> enterConfiguration(Backend backend, ConnectionContext context) {
        return enterConfiguration.invokeAsync(new EnterConfiguration(backend, context));
    }

    public CompletableFuture<Void> preConnectBackend(Backend backend, ConnectionContext context) {
        return preConnectBackend.invokeAsync(new PreConnectBackend(backend, context));
    }

    public CompletableFuture<Void> postConnectBackend(Backend backend, ConnectionContext context) {
        return postConnectBackend.invokeAsync(new PostConnectBackend(backend, context));
    }

    public CompletableFuture<Void> disconnect(Backend backend, ConnectionContext context) {
        return disconnect.invokeAsync(new Disconnect(backend, context));
    }

    @Override
    public ConcurrentHandlerList<PreLogin> preLogin() {
        return preLogin;
    }

    @Override
    public ConcurrentHandlerList<PostLogin> postLogin() {
        return postLogin;
    }

    @Override
    public ConcurrentHandlerList<EnterConfiguration> enterConfiguration() {
        return enterConfiguration;
    }

    @Override
    public ConcurrentHandlerList<PreConnectBackend> preConnectBackend() {
        return preConnectBackend;
    }

    @Override
    public ConcurrentHandlerList<PostConnectBackend> postConnectBackend() {
        return postConnectBackend;
    }

    @Override
    public ConcurrentHandlerList<Disconnect> disconnect() {
        return disconnect;
    }
}
