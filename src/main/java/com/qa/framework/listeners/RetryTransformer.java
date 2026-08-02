package com.qa.framework.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Applies {@link RetryAnalyzer} to every {@code @Test} in the suite.
 *
 * <p>The alternative — {@code @Test(retryAnalyzer = RetryAnalyzer.class)} on each method — is one
 * more thing to forget on every new test, and forgetting it is invisible until a flake escapes.
 * Registering the transformer once in the suite file makes the policy uniform by construction.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        // Never override an analyzer a test set for itself.
        if (annotation.getRetryAnalyzerClass() == null
                || annotation.getRetryAnalyzerClass() == org.testng.internal.annotations.DisabledRetryAnalyzer.class) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
