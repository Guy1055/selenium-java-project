package com.toolshop.core;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/** Retries failed tests in CI only, because the shared demo resets itself now and then. */
public class RetryTransformer implements IAnnotationTransformer {
    private static final int MAX_RETRIES = System.getenv("CI") != null ? 2 : 0;

    public static class Retry implements IRetryAnalyzer {
        private final AtomicInteger attempts = new AtomicInteger();

        @Override
        public boolean retry(ITestResult result) {
            return attempts.incrementAndGet() <= MAX_RETRIES;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor constructor, Method method) {
        annotation.setRetryAnalyzer(Retry.class);
    }
}
