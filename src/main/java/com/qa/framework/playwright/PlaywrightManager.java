package com.qa.framework.playwright;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.config.FrameworkConfig;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Playwright counterpart to {@code DriverManager} — one browser stack per thread.
 *
 * <p>A {@link Playwright} instance owns a Node process and is explicitly <em>not</em> thread-safe,
 * so unlike Selenium it is not enough to isolate the page: the entire chain
 * {@code Playwright -> Browser -> BrowserContext -> Page} is thread-local.
 *
 * <p>The extra {@link BrowserContext} layer is what Playwright gives us over WebDriver. It is an
 * isolated cookie/storage partition that costs milliseconds to create, so every test starts from a
 * genuinely clean session without paying for a browser launch.
 */
public final class PlaywrightManager {

    private static final Logger LOG = LogManager.getLogger(PlaywrightManager.class);
    private static final Path TRACE_DIR = Paths.get("target", "playwright-traces");

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightManager() {
        // static holder
    }

    /** Creates a Playwright browser, context and page, and binds them to the calling thread. */
    public static Page startPage() {
        FrameworkConfig config = ConfigManager.get();
        LOG.info("Launching Playwright {} (headless={})", config.browser(), config.headless());

        Playwright playwright = Playwright.create();

        // The application marks its automation hooks with data-test, not Playwright's default
        // data-testid. Retargeting the attribute once here lets every page object use the
        // first-class getByTestId() locator instead of falling back to raw CSS.
        playwright.selectors().setTestIdAttribute("data-test");

        Browser browser = launchBrowser(playwright, config);

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(config.windowWidth(), config.windowHeight()));
        context.setDefaultTimeout(config.playwrightActionTimeoutMs());

        if (config.playwrightTrace()) {
            // Tracing runs for every test but is only written to disk when one fails
            // (see stopTracing) — full diagnostics on red, no artefact sprawl on green.
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(false));
        }

        Page page = context.newPage();

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);
        return page;
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException(
                    "No Playwright page bound to thread '" + Thread.currentThread().getName()
                            + "'. Does the test extend BasePlaywrightTest?");
        }
        return page;
    }

    public static boolean hasPage() {
        return PAGE.get() != null;
    }

    /**
     * Writes the current context's trace to {@code target/playwright-traces}.
     *
     * @return the trace file, or {@code null} when tracing is off or no context is bound
     */
    public static Path stopTracing(String testName) {
        BrowserContext context = CONTEXT.get();
        if (context == null || !ConfigManager.get().playwrightTrace()) {
            return null;
        }
        try {
            Path trace = TRACE_DIR.resolve(sanitise(testName) + "-" + Thread.currentThread().getId() + ".zip");
            context.tracing().stop(new Tracing.StopOptions().setPath(trace));
            LOG.info("Playwright trace written to {} (view with: npx playwright show-trace {})", trace, trace);
            return trace;
        } catch (RuntimeException e) {
            LOG.warn("Could not write Playwright trace: {}", e.getMessage());
            return null;
        }
    }

    /** Closes the whole per-thread stack, innermost first, and clears every thread-local. */
    public static void closePage() {
        closeQuietly("page", PAGE.get() == null ? null : () -> PAGE.get().close());
        closeQuietly("context", CONTEXT.get() == null ? null : () -> CONTEXT.get().close());
        closeQuietly("browser", BROWSER.get() == null ? null : () -> BROWSER.get().close());
        closeQuietly("playwright", PLAYWRIGHT.get() == null ? null : () -> PLAYWRIGHT.get().close());

        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
    }

    private static Browser launchBrowser(Playwright playwright, FrameworkConfig config) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.headless())
                .setSlowMo(config.playwrightSlowMoMs());

        return switch (config.browser().toLowerCase(Locale.ROOT)) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit", "safari" -> playwright.webkit().launch(options);
            case "edge" -> playwright.chromium().launch(options.setChannel("msedge"));
            default -> playwright.chromium().launch(options);
        };
    }

    private static void closeQuietly(String what, Runnable close) {
        if (close == null) {
            return;
        }
        try {
            close.run();
        } catch (RuntimeException e) {
            LOG.warn("Failed to close Playwright {}: {}", what, e.getMessage());
        }
    }

    private static String sanitise(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
