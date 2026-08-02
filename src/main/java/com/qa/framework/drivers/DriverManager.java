package com.qa.framework.drivers;

import com.qa.framework.config.ConfigManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Owns one {@link WebDriver} per thread.
 *
 * <p>This is the piece that makes {@code parallel="methods"} safe. Each TestNG worker thread calls
 * {@link #startDriver()} and gets its own browser; page objects and utilities read the driver back
 * through {@link #getDriver()} without ever passing it around as a parameter, and no thread can
 * see another thread's session.
 *
 * <p>{@link #quitDriver()} must run for every started driver — it closes the browser <em>and</em>
 * removes the {@link ThreadLocal} entry. Skipping the removal on a pooled thread leaks both the
 * reference and, eventually, the machine's memory.
 */
public final class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
        // static holder
    }

    /** Creates the browser configured for this run and binds it to the calling thread. */
    public static WebDriver startDriver() {
        if (DRIVER.get() != null) {
            LOG.warn("A driver is already bound to thread '{}'; reusing it", Thread.currentThread().getName());
            return DRIVER.get();
        }
        WebDriver driver = DriverFactory.create(BrowserType.from(ConfigManager.get().browser()));
        DRIVER.set(driver);
        return driver;
    }

    /**
     * @return the driver bound to the calling thread
     * @throws IllegalStateException if no driver was started — a far clearer failure than the
     *         {@code NullPointerException} a bare {@code ThreadLocal.get()} would produce
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver bound to thread '" + Thread.currentThread().getName()
                            + "'. Does the test extend BaseWebTest?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (RuntimeException e) {
            // A browser that already died must not fail an otherwise passing test.
            LOG.warn("Driver quit failed on thread '{}': {}", Thread.currentThread().getName(), e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }
}
