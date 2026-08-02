package com.qa.framework.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import com.qa.framework.config.ConfigManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@code retry.count} times.
 *
 * <p>Retries are a controlled concession to shared public demo environments and real networks, not
 * a way to paper over defects: the count is configurable and setting {@code -Dretry.count=0} gives
 * an honest, unfiltered result. Every retry is logged at WARN so a test that only ever passes on
 * the second attempt is visible rather than silently green.
 *
 * <p>TestNG creates one analyzer per test method, so the counter needs no cross-thread coordination;
 * it is atomic purely to stay correct if the same method is invoked concurrently by a data provider.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);

    private final AtomicInteger attempts = new AtomicInteger();
    private final int maxRetries = ConfigManager.get().retryCount();

    @Override
    public boolean retry(ITestResult result) {
        if (attempts.get() >= maxRetries) {
            return false;
        }
        int attempt = attempts.incrementAndGet();
        LOG.warn("Retrying {}.{} (attempt {} of {}) after: {}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getName(),
                attempt,
                maxRetries,
                result.getThrowable() == null ? "unknown failure" : result.getThrowable().getMessage());
        return true;
    }
}
