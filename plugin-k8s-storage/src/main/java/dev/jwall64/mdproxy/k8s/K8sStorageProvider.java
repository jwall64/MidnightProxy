package dev.jwall64.mdproxy.k8s;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.mdcfg.serializer.BackSerializer;
import dev.jwall64.mdcfg.serializer.GsonContext;
import dev.jwall64.mdcfg.serializer.SerializeContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.StorageProvider;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Watchable;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;

import dev.jwall64.mdproxy.Backend;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.Route;
import dev.jwall64.mdproxy.StatusEntry;
import dev.jwall64.mdproxy.AuthRoute;

public class K8sStorageProvider implements StorageProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sStorageProvider.class);

    private final String namespace;
    private Proxy proxy;

    private final DynamicKubernetesApi backendsApi;
    private final DynamicKubernetesApi routesApi;
    private final DynamicKubernetesApi statusEntriesApi;
    private final DynamicKubernetesApi authRoutesApi;
    
    private final Registry<String, Backend> backends;
    private final Registry<String, Route> routes;
    private final Registry<String, StatusEntry> statusEntries;
    private final Registry<String, AuthRoute> authRoutes;

    private final ExecutorService exe;

    public K8sStorageProvider(String namespace, ApiClient client) {
        this.namespace = namespace;
        this.exe = Executors.newFixedThreadPool(4);

        this.backendsApi = new DynamicKubernetesApi("mdproxy.jwall64.dev", "v1", "mcbackends", client);
        this.routesApi = new DynamicKubernetesApi("mdproxy.jwall64.dev", "v1", "mcroutes", client);
        this.statusEntriesApi = new DynamicKubernetesApi("mdproxy.jwall64.dev", "v1", "mcstatusentries", client);
        this.authRoutesApi = new DynamicKubernetesApi("mdproxy.jwall64.dev", "v1", "mcauthroutes", client);

        this.backends = Registry.createStringRegistry();
        this.routes = Registry.createStringRegistry();
        this.statusEntries = Registry.createStringRegistry();
        this.authRoutes = Registry.createStringRegistry();

    }

    public Registry<String, Backend> backends() { 
        return backends;
    }

    public List<Route> routes() { 
        return List.copyOf(routes.values());
    }

    public List<StatusEntry> statusEntries() {
        return List.copyOf(statusEntries.values());
    }

    public List<AuthRoute> authRoutes() { 
        return List.copyOf(authRoutes.values());
    }

    public void reload(Proxy proxy) { 

        if(this.proxy == null) {
            this.proxy = proxy;

            exe.submit(() -> watch(backendsApi, "backend", backends, () -> Backend.SERIALIZER));
            exe.submit(() -> watch(routesApi, "route", routes, () -> Route.SERIALIZER));
            exe.submit(() -> watch(statusEntriesApi, "status entry", statusEntries, () -> StatusEntry.SERIALIZER));
            exe.submit(() -> watch(authRoutesApi, "auth route", authRoutes, () -> AuthRoute.serializer(proxy)));

        }
    } 

    private <T> void watch(DynamicKubernetesApi api, String registryName, Registry<String, T> registry, Supplier<Serializer<T>> serializer) {
        try {
            Watchable<DynamicKubernetesObject> obj = api.watch(namespace);
            proxy.shutdownEvent().register(this, proxy -> {
                try { 
                    obj.close();
                } catch(IOException ex) {
                    LOGGER.error("An error occurred while closing a watch!", ex);
                }
            });

            obj.forEach(res -> {
                if(res == null || res.object == null || res.object.getMetadata() == null) return;
                String name = res.object.getMetadata().getName();
                switch(res.type) {
                    case "MODIFIED": 
                        registry.remove(name);
                        // Fall-through
                    case "ADDED": {
                        SerializeResult<T> result = serializer.get().deserialize(GsonContext.INSTANCE, res.object.getRaw().getAsJsonObject("spec"));
                        if(!result.isComplete()) {
                            LOGGER.error("An error occurred while decoding a " + registryName + "!", result.getError());
                        } else {
                            registry.register(name, result.getOrThrow());
                        }
                        break;
                    }
                    case "DELETED":
                        registry.remove(name);
                        break;
                }
            });
        } catch(ApiException ex) {
            LOGGER.error("An exception occurred while watching for a custom resource!", ex);
        }
    }

    public static BackSerializer<K8sStorageProvider> SERIALIZER = new BackSerializer<K8sStorageProvider>() {
        
        @Override
        public <O> SerializeResult<K8sStorageProvider> deserialize(SerializeContext<O> ctx, O value) {

            return ctx.asString(ctx.get("namespace", value)).map(namespace -> {
                ApiClient client;
                try {
                    client = ClientBuilder.cluster().build();
                } catch (IOException | NumberFormatException ex) {
                    return SerializeResult.failure("The Kubernetes storage class can only be used within a Kubernetes cluster!", ex);
                }
                return SerializeResult.success(new K8sStorageProvider(namespace, client));
            });
        }

    };
}
