package com.qa.framework.utils;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.drivers.DriverManager;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Every wait in the Selenium layer goes through here.
 *
 * <p>Two deliberate choices:
 * <ul>
 *   <li>No implicit wait is ever configured. Implicit and explicit waits compound unpredictably in
 *       the same session, producing timeouts nobody can explain; one explicit strategy is easier to
 *       reason about and to tune per environment.</li>
 *   <li>{@link StaleElementReferenceException} is ignored while polling. On a page that re-renders
 *       — which is most of them — a reference can go stale between the lookup and the assertion,
 *       and retrying is the correct response, not failing.</li>
 * </ul>
 */
public final class WaitUtils {

    private WaitUtils() {
        // static utility
    }

    private static WebDriverWait waiter() {
        return waiter(ConfigManager.get().explicitWaitSeconds());
    }

    private static WebDriverWait waiter(int seconds) {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        webDriverWait.ignoring(StaleElementReferenceException.class);
        return webDriverWait;
    }

    public static WebElement waitForVisible(By locator) {
        return waiter().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(By locator, int seconds) {
        return waiter(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return waiter().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static List<WebElement> waitForAllVisible(By locator) {
        return waiter().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static boolean waitForInvisibility(By locator) {
        return waiter().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static boolean waitForUrlContains(String fragment) {
        return waiter().until(ExpectedConditions.urlContains(fragment));
    }

    public static boolean waitForTextIn(By locator, String text) {
        return waiter().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Waits for an arbitrary condition — the escape hatch for app-specific synchronisation that
     * {@link ExpectedConditions} does not cover, without page objects reaching for {@code Thread.sleep}.
     */
    public static <T> T waitUntil(Function<WebDriver, T> condition) {
        return waiter().until(condition::apply);
    }

    /**
     * Non-blocking existence check.
     *
     * <p>Uses a deliberately short timeout: this answers "is it there right now?", typically for a
     * validation banner that should <em>not</em> appear, so waiting the full explicit timeout would
     * just make the happy path slow.
     */
    public static boolean isPresent(By locator, int seconds) {
        try {
            waiter(seconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException | NoSuchElementException e) {
            return false;
        }
    }
}
