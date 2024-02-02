package dev.jwall64.mdproxy.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdproxy.Proxy;
import dev.jwall64.mdproxy.plugin.Plugin;

public class TestPlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("TestPlugin");

    @Override
    public void initialize(Proxy proxy) {

        LOGGER.info("Hello, World");
    }
}
