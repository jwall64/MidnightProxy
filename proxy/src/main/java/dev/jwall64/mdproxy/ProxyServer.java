package dev.jwall64.mdproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.jwt.Algorithm;
import dev.jwall64.jwt.CryptAlgorithm;
import dev.jwall64.jwt.JsonWebKey;
import dev.jwall64.jwt.Jwt;
import dev.jwall64.jwt.KeyVerifier;
import dev.jwall64.jwt.TokenValidator;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigPrimitive;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.FileCodecRegistry;
import dev.jwall64.mdcfg.codec.FileWrapper;
import dev.jwall64.mdcfg.serializer.BackSerializer;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.command.CommandExecutor;
import dev.jwall64.mdproxy.command.ListCommand;
import dev.jwall64.mdproxy.command.ReloadCommand;
import dev.jwall64.mdproxy.command.StopCommand;
import dev.jwall64.mdproxy.command.SyncCommand;
import dev.jwall64.mdproxy.netty.ConnectionManager;
import dev.jwall64.mdproxy.plugin.PluginManager;
import dev.jwall64.mdproxy.plugin.PluginManagerImpl;
import dev.jwall64.mdproxy.util.CryptUtil;
import dev.jwall64.midnightlib.event.HandlerList;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.pseudonym.PartialMessage;
import dev.jwall64.pseudonym.lang.LangManager;
import dev.jwall64.pseudonym.text.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class ProxyServer implements Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger("ProxyServer");

    private final KeyPair keyPair;
    private final JsonWebKey reconnectKey;
    private final FileWrapper<ConfigObject> config;
    private final Registry<String, CommandExecutor> commands;
    private final Map<String, Authenticator> authenticators = new HashMap<>();
    private final ConnectionManager listener;
    private final ConsoleHandler console;
    private final LangManager<PartialMessage<String>, Component> langManager;
    private final PluginManagerImpl pluginLoader;
    private final TokenValidator reconnectTokenValidator;
    private final IconCacheImpl iconCache;
    private final int port;
    private final int clientTimeout;
    private final PlayerListImpl playerList;
    private final FileCodecRegistry fileCodecRegistry;

    private Registry<String, BackSerializer<? extends StorageProvider>> storageProviderClasses;
    private PlayerCountProvider playerCount;

    private final ThreadPoolExecutor authExecutor;

    private int reconnectTimeout;
    private int backendTimeout;
    private int playerLimit;
    private boolean useAuthentication;
    private boolean requireAuth;
    private boolean haproxy;
    private boolean preventProxy;
    private boolean logStatus;
    private boolean legacyPing;
    private StorageProvider storageProvider;

    // Events
    private final HandlerList<ClientConnection> connected = new HandlerList<>();
    private final HandlerList<ClientConnection> disconnected = new HandlerList<>();
    private final HandlerList<ClientConnection> joined = new HandlerList<>();
    private final HandlerList<Proxy> shutdown = new HandlerList<>();
    private final HandlerList<Proxy> started = new HandlerList<>();

    public ProxyServer(FileWrapper<ConfigObject> config, FileCodecRegistry registry, LangManager<PartialMessage<String>, Component> langManager, PluginManagerImpl pluginLoader) {

        this.config = config;
        this.langManager = langManager;
        this.pluginLoader = pluginLoader;
        this.reconnectTokenValidator = 
            new TokenValidator.AllOf(List.of(
                new TokenValidator.ExpectClaims(Map.of(Jwt.CLAIM_ISSUER, new ConfigPrimitive("midnightproxy"))),
                new TokenValidator.ExpectPresent(List.of("hostname", "port", "username", "uuid", "protocol")),
                new TokenValidator.AnyOf(List.of(
                    new TokenValidator.ExpectPresent(List.of("backend")),
                    new TokenValidator.ExpectPresent(List.of("backend_host", "backend_port", "backend_redirect", "backend_haproxy"))
                )),
                new TokenValidator.SingleUse("rcid", 60 * 1000)
            ));
        this.fileCodecRegistry = registry;

        this.playerList = new PlayerListImpl();
        this.playerCount = playerList;

        Path iconCacheDir = Paths.get(getConfig().getString("icon_cache_dir"));
        this.iconCache = new IconCacheImpl(iconCacheDir, getConfig().getInt("icon_cache_size"));

        try { Files.createDirectories(iconCacheDir); } catch (IOException e) {
            throw new RuntimeException("Could not create icon cache directory", e);
        }

        ConfigSection conf = getConfig();

        this.port = conf.getInt("port");
        this.clientTimeout = conf.getInt("client_timeout_ms");

        this.keyPair = CryptUtil.generateKeyPair();
        this.reconnectKey = JsonWebKey.builder()
            .withAlgorithm(Algorithm.ECDH_ES_A256KW)
            .withUse(Algorithm.ECDH_ES_A256KW.use())
            .generate(Algorithm.ECDH_ES_A256KW.type());

        this.commands = Registry.createStringRegistry();

        this.commands.register("stop", new StopCommand());
        this.commands.register("reload", new ReloadCommand());
        this.commands.register("list", new ListCommand());
        this.commands.register("sync", new SyncCommand());

        this.listener = new ConnectionManager(this, conf.getInt("boss_threads"), conf.getInt("worker_threads"));
        this.console = new ConsoleHandler(this);
        this.authExecutor = new ThreadPoolExecutor(1, conf.getInt("auth_threads"), conf.getInt("auth_timeout_ms"), TimeUnit.MILLISECONDS, new SynchronousQueue<>());

        this.storageProviderClasses = Registry.createStringRegistry();
        this.storageProviderClasses.register("config", new BackSerializer<ConfigStorageProvider>() {
            @Override
            public <O> SerializeResult<ConfigStorageProvider> deserialize(SerializeContext<O> ctx, O obj) {
                return SerializeResult.success(new ConfigStorageProvider());
            }
        });
        this.storageProviderClasses.register("folder", FolderStorageProvider.SERIALIZER);

        this.pluginLoader.loadAll(this);
        this.storageProviderClasses = storageProviderClasses.freeze();

        reload();
    }

    public void start() {
        console.start();
        this.listener.startListener();

        started.invoke(this);
    }

    @Override
    public void shutdown() {

        if(listener.getBossGroup().isTerminated()) {
            return;
        }

        shutdownEvent().invoke(this);

        LOGGER.info("Shutting down...");

        authExecutor.shutdown();
        console.stop();
        listener.stop();

    }

    @Override
    public ConfigSection getConfig() {
        ConfigObject out = config.getRoot();
        if(out == null || !out.isSection()) {
            return new ConfigSection();
        }
        return out.asSection();
    }

    @Override
    public void reload() {
        config.load();
        langManager.clearCache();

        ConfigSection conf = getConfig();

        this.useAuthentication = conf.getBoolean("use_authentication");
        this.requireAuth = conf.getBoolean("force_authentication");
        this.backendTimeout = conf.getInt("backend_timeout_ms");
        this.playerLimit = conf.getInt("player_limit");
        this.reconnectTimeout = conf.getInt("reconnect_timeout_sec");
        this.haproxy = conf.getBoolean("haproxy_protocol");
        this.preventProxy = conf.getBoolean("prevent_proxy_connections");
        this.logStatus = conf.getBoolean("log_status_messages");
        this.legacyPing = conf.getBoolean("reply_to_legacy_ping");

        ConfigSection storageProviderConfig = conf.getSection("storage_provider");
        String storageProviderClass = storageProviderConfig.getString("type");

        BackSerializer<? extends StorageProvider> ser = storageProviderClasses.get(storageProviderClass);

        this.storageProvider = ser.deserialize(ConfigContext.INSTANCE, storageProviderConfig).getOrThrow();
        this.storageProvider.reload(this);

    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Registry<String, Backend> getBackends() {
        return storageProvider.backends();
    }

    @Override
    public List<StatusEntry> getStatusEntries() {
        return storageProvider.statusEntries();
    }

    @Override
    public List<Route> getRoutes() {
        return storageProvider.routes();
    }

    @Override
    public IconCache getIconCache() {
        return iconCache;
    }

    @Override
    public boolean usesAuthentication() {
        return useAuthentication;
    }

    @Override
    public boolean requiresAuth() {
        return requireAuth;
    }

    @Override
    public Registry<String, CommandExecutor> getCommands() {
        return commands;
    }

    @Override
    public Stream<UUID> getClientIds() {
        return playerList.getPlayerIds();
    }

    @Override
    public ClientConnection getConnection(UUID uuid) {
        return playerList.getPlayer(uuid);
    }

    @Override
    public PlayerList getPlayerList() {
        return playerList;
    }

    @Override
    public int getOnlinePlayers() {
        return playerCount.getOnlinePlayers(this);
    }

    @Override
    public int getPlayerLimit() {
        return playerLimit;
    }

    @Override
    public boolean bypassesPlayerLimit(PlayerInfo info) {
        return false;
    }

    @Override
    public boolean bypassesPlayerLimit(PlayerProfile info) {
        return false;
    }

    @Override
    public void setPlayerCountProvider(PlayerCountProvider provider) {
        this.playerCount = provider;
    }

    @Override
    public Authenticator getAuthenticator(String type) {
        return authenticators.computeIfAbsent(type, k -> {
            Authenticator.Type t = Authenticator.REGISTRY.get(k);
            if(t == null) return null;
            return t.create(this);
        });
    }

    public List<AuthRoute> getAuthRoutes() {
        return storageProvider.authRoutes();
    }

    public Executor getAuthExecutor() {
        return authExecutor;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginLoader;
    }

    @Override
    public HandlerList<ClientConnection> clientConnectEvent() {
        return connected;
    }

    @Override
    public HandlerList<ClientConnection> clientDisconnectEvent() {
        return disconnected;
    }

    @Override
    public HandlerList<ClientConnection> clientJoinBackendEvent() {
        return joined;
    }

    @Override
    public HandlerList<Proxy> startupEvent() {
        return started;
    }

    @Override
    public HandlerList<Proxy> shutdownEvent() {
        return shutdown;
    }

    @Override
    public FileCodecRegistry fileCodecRegistry() {
        return fileCodecRegistry;
    }

    @Override
    public StorageProvider storageProvider() {
        return storageProvider;
    }

    @Override
    public Registry<String, BackSerializer<? extends StorageProvider>> storageProviderClasses() {
        return storageProviderClasses;
    }

    public int getReconnectTimeout() {
        return reconnectTimeout;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }


    public String generateReconnectToken(ConnectionContext context, Backend backend) {

        Jwt.Builder builder = Jwt.builder()
            .withClaim("hostname", backend.redirect() ? backend.hostname() : context.getConnection().hostname())
            .withClaim("port", backend.redirect() ? backend.port() : context.getConnection().port())
            .withClaim("protocol", context.getConnection().protocolVersion())
            .withClaim("username", context.getConnection().username())
            .withClaim("uuid", Objects.toString(context.getConnection().uuid()))
            .withClaim("rcid", UUID.randomUUID(), Serializer.UUID)
            .expiresIn(getReconnectTimeout())
            .withIssuer("midnightproxy");

        String backendId = getBackends().getId(backend);
        if(backendId == null) {
            builder.withClaim("backend_host", backend.hostname())
                    .withClaim("backend_port", backend.port())
                    .withClaim("backend_redirect", backend.redirect())
                    .withClaim("backend_haproxy", backend.haproxy());
        } else {
            builder.withClaim("backend", backendId);
        }

        return builder.buildEncrypted(reconnectKey, reconnectKey.algorithm(), CryptAlgorithm.A256GCM);
    }

    @Nullable
    public Jwt validateReconnect(String tokenStr) {

        Jwt token;
        try {
            token = Jwt.parseAndVerify(tokenStr, reconnectKey, KeyVerifier.DEFAULT);
        } catch(Exception ex) { return null; }

        if(!reconnectTokenValidator.validate(token)) return null;
        return token;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public int getBackendTimeout() {
        return backendTimeout;
    }

    public LangManager<PartialMessage<String>, Component> getLangManager() {
        return langManager;
    }

    public boolean useHAProxyProtocol() {
        return haproxy;
    }

    public boolean preventProxyConnections() {
        return preventProxy;
    }

    public boolean logStatusMessages() {
        return logStatus;
    }

    public boolean replyToLegacyPing() {
        return legacyPing;
    }

    public ConnectionManager getConnectionManager() {
        return listener;
    }

}
