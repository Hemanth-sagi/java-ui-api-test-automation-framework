package com.qa.framework.drivers;

import java.time.Duration;
import java.util.Map;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Builds a configured {@link WebDriver} for the browser named in configuration.
 *
 * <p>The factory only constructs drivers — it never stores them. Ownership passes straight to
 * {@link DriverManager}, which keeps exactly one instance per thread. Keeping creation and
 * lifetime in separate classes is what allows a new browser to be added here without touching
 * anything to do with parallelism.
 */
public final class DriverFactory {

    private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
        // static factory
    }

    public static WebDriver create(BrowserType browserType) {
        FrameworkConfig config = ConfigManager.get();
        LOG.info("Launching {} (headless={})", browserType, config.headless());

        WebDriver driver = switch (browserType) {
            case CHROME -> createChrome(config);
            case FIREFOX -> createFirefox(config);
            case EDGE -> createEdge(config);
        };

        // A hard page-load ceiling stops a hung navigation from stalling the whole suite.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeoutSeconds()));

        // No implicit wait is set anywhere, by design: mixing implicit and explicit waits makes
        // timeouts non-deterministic. All synchronisation goes through WaitUtils.
        driver.manage().window().setSize(new Dimension(config.windowWidth(), config.windowHeight()));
        return driver;
    }

    private static WebDriver createChrome(FrameworkConfig config) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (config.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--remote-allow-origins=*",
                "--window-size=" + config.windowWidth() + "," + config.windowHeight());

        // Chrome's password manager and breach warnings render on top of the app and swallow
        // clicks on any login form, which is exactly what half of these tests do first.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox(FrameworkConfig config) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        if (config.headless()) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=" + config.windowWidth(), "--height=" + config.windowHeight());
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdge(FrameworkConfig config) {
        WebDriverManager.edgedriver().setup();

        EdgeOptions options = new EdgeOptions();
        if (config.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        return new EdgeDriver(options);
    }
}
