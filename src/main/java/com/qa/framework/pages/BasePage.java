package com.qa.framework.pages;

import java.util.List;
import java.util.stream.Collectors;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.drivers.DriverManager;
import com.qa.framework.utils.WaitUtils;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Shared behaviour for every Selenium page object.
 *
 * <p>Subclasses declare locators and business-level methods; the mechanics of waiting, clicking and
 * reading text live here exactly once. The rule the whole layer follows: a page object exposes what
 * a <em>user</em> can do ({@code loginAs}, {@code addToCart}) and never leaks a {@link WebElement}
 * to a test. Tests that only ever see domain types cannot be broken by a UI refactor.
 *
 * <p>The driver is resolved from {@link DriverManager} at construction, so a page object belongs to
 * the thread that created it — which is the same thread the test runs on.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
    }

    /**
     * Every page states how to tell it has finished loading. Concrete implementations should pick a
     * locator that only exists on that page, so navigating to the wrong screen fails immediately
     * with a clear message instead of timing out later on an unrelated element.
     */
    public abstract boolean isLoaded();

    // ------------------------------------------------------------------ navigation

    @Step("Open {path}")
    protected void open(String path) {
        String url = ConfigManager.get().webBaseUrl() + path;
        log.info("Navigating to {}", url);
        driver.get(url);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String pageTitle() {
        return driver.getTitle();
    }

    // ------------------------------------------------------------------ interactions

    @Step("Click {locator}")
    protected void click(By locator) {
        WaitUtils.waitForClickable(locator).click();
    }

    @Step("Type '{text}' into {locator}")
    protected void type(By locator, String text) {
        WebElement field = WaitUtils.waitForVisible(locator);
        field.clear();
        if (text != null && !text.isEmpty()) {
            field.sendKeys(text);
        }
    }

    protected void selectByValue(By locator, String value) {
        new Select(WaitUtils.waitForVisible(locator)).selectByValue(value);
    }

    // ------------------------------------------------------------------ reads

    protected String textOf(By locator) {
        return WaitUtils.waitForVisible(locator).getText().trim();
    }

    protected List<String> textsOf(By locator) {
        return WaitUtils.waitForAllVisible(locator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    protected int countOf(By locator) {
        return driver.findElements(locator).size();
    }

    protected boolean isDisplayed(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    /** Short-timeout presence check, for elements that are expected to be absent. */
    protected boolean isPresentQuickly(By locator) {
        return WaitUtils.isPresent(locator, 2);
    }

    protected void scrollIntoView(By locator) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", WaitUtils.waitForVisible(locator));
    }
}
