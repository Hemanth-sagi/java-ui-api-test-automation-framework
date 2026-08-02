package com.qa.framework.config;

import org.aeonbits.owner.Config;

/**
 * Typed view over the framework's configuration.
 *
 * <p>Backed by Owner, so every key below is resolved at call time from the first source that
 * provides it. Resolution order is defined in {@link ConfigManager}:
 * {@code -D system property} &rarr; {@code environment variable} &rarr;
 * {@code config/<env>.properties} &rarr; {@code config/config.properties}.
 *
 * <p>Adding a setting means adding a method here — the compiler then guarantees every call site
 * uses a key that actually exists, which raw {@code Properties.getProperty(String)} never can.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:config/${env}.properties",
        "classpath:config/config.properties"
})
public interface FrameworkConfig extends Config {

    // ------------------------------------------------------------------ environment

    @Key("env")
    @DefaultValue("dev")
    String env();

    // ------------------------------------------------------------------ web / UI

    @Key("web.base.url")
    String webBaseUrl();

    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("headless")
    @DefaultValue("true")
    boolean headless();

    @Key("window.width")
    @DefaultValue("1920")
    int windowWidth();

    @Key("window.height")
    @DefaultValue("1080")
    int windowHeight();

    /** Seconds an explicit wait will poll before giving up. */
    @Key("explicit.wait.seconds")
    @DefaultValue("15")
    int explicitWaitSeconds();

    @Key("page.load.timeout.seconds")
    @DefaultValue("30")
    int pageLoadTimeoutSeconds();

    /** Playwright's per-action timeout, in milliseconds. */
    @Key("playwright.action.timeout.ms")
    @DefaultValue("15000")
    double playwrightActionTimeoutMs();

    /** Slows Playwright down by N ms per action — useful when recording a demo GIF. */
    @Key("playwright.slowmo.ms")
    @DefaultValue("0")
    double playwrightSlowMoMs();

    /** Records a Playwright trace per test; the trace is only kept when the test fails. */
    @Key("playwright.trace")
    @DefaultValue("true")
    boolean playwrightTrace();

    // ------------------------------------------------------------------ API

    @Key("api.base.uri")
    String apiBaseUri();

    @Key("api.base.path")
    @DefaultValue("")
    String apiBasePath();

    @Key("api.username")
    String apiUsername();

    @Key("api.password")
    String apiPassword();

    /**
     * Pre-issued bearer token. Left blank in the committed properties files: when it is blank the
     * framework logs in for a token instead. CI injects a real one via the {@code API_TOKEN}
     * environment variable, so no credential material ever needs to be committed.
     */
    @Key("api.token")
    @DefaultValue("")
    String apiToken();

    // ------------------------------------------------------------------ execution

    /** Extra attempts a failed test is given before it is reported as failed. 0 disables retries. */
    @Key("retry.count")
    @DefaultValue("1")
    int retryCount();

    @Key("screenshot.on.failure")
    @DefaultValue("true")
    boolean screenshotOnFailure();
}
