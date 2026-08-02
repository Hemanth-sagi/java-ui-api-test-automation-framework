package com.qa.framework.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single entry point to configuration.
 *
 * <p>Thread safety: the {@link FrameworkConfig} proxy is built once during class initialisation —
 * the JVM guarantees that happens exactly once, under a lock, before any thread sees the field —
 * and is then only ever read. No synchronisation is needed on the hot path, so parallel test
 * threads never contend on config access.
 */
public final class ConfigManager {

    private static final Logger LOG = LogManager.getLogger(ConfigManager.class);

    private static final String ENV_KEY = "env";
    private static final String DEFAULT_ENV = "dev";

    private static final FrameworkConfig CONFIG;

    static {
        String env = System.getProperty(ENV_KEY, DEFAULT_ENV);

        // Makes ${env} in @Config.Sources resolve to the environment under test.
        ConfigFactory.setProperty(ENV_KEY, env);

        CONFIG = ConfigCache.getOrCreate(FrameworkConfig.class, overrides(env));

        LOG.info("Configuration loaded | env={} | browser={} | headless={} | web={} | api={}",
                CONFIG.env(), CONFIG.browser(), CONFIG.headless(), CONFIG.webBaseUrl(), CONFIG.apiBaseUri());
    }

    private ConfigManager() {
        // static utility
    }

    public static FrameworkConfig get() {
        return CONFIG;
    }

    /**
     * Builds the highest-priority override layer.
     *
     * <p>Environment variables are folded in first and command-line {@code -D} properties second,
     * so a {@code -D} flag always wins over an inherited shell variable. Doing the merge here —
     * rather than relying on the ordering of several Owner imports — keeps the precedence rule
     * explicit and testable.
     *
     * <p>Env var names are normalised {@code API_TOKEN -> api.token}, which is what lets a CI
     * secret populate a config key without that key ever appearing in a committed file.
     */
    private static Map<String, String> overrides(String env) {
        Map<String, String> overrides = new HashMap<>();
        System.getenv().forEach((key, value) -> overrides.put(normalise(key), value));
        System.getProperties().forEach((key, value) -> overrides.put(String.valueOf(key), String.valueOf(value)));
        overrides.put(ENV_KEY, env);
        return overrides;
    }

    private static String normalise(String envVarName) {
        return envVarName.toLowerCase(Locale.ROOT).replace('_', '.');
    }
}
