package com.toolshop.core;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.Arrays;

/** Applies @KnownBug: inverts the outcome of the test method. */
public class KnownBugListener implements IInvokedMethodListener {
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        KnownBug bug = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(KnownBug.class);
        if (bug == null || !applies(bug, result)) {
            return;
        }
        if (result.getStatus() == ITestResult.FAILURE) {
            result.setAttribute("knownBug", bug.value());
            result.setThrowable(null);
            result.setStatus(ITestResult.SUCCESS);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            result.setStatus(ITestResult.FAILURE);
            result.setThrowable(new AssertionError("Passed, so this may be fixed: remove @KnownBug. " + bug.value()));
        }
    }

    private boolean applies(KnownBug bug, ITestResult result) {
        if (bug.onlyFor().length == 0) {
            return true;
        }
        Object[] parameters = result.getParameters();
        return parameters.length > 0 && Arrays.asList(bug.onlyFor()).contains(String.valueOf(parameters[0]));
    }
}
