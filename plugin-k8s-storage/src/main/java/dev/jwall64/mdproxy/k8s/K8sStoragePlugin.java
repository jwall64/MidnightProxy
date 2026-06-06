package dev.jwall64.mdproxy.k8s;

import dev.jwall64.mdproxy.plugin.Plugin;
import dev.jwall64.mdproxy.Proxy;

public class K8sStoragePlugin implements Plugin {

    public void initialize(Proxy proxy) {
        proxy.storageProviderClasses().register("k8s", K8sStorageProvider.SERIALIZER);
    }

}
