package com.toolshop.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test that describes correct behaviour the site does not have yet.
 * The test counts as passed while it fails, and fails the day the site is fixed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KnownBug {
    String value();

    /** For data-driven tests: only rows whose first argument is listed are expected to fail. */
    String[] onlyFor() default {};
}
