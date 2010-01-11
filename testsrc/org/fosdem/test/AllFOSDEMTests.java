package org.fosdem.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class AllFOSDEMTests extends TestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(AllFOSDEMTests.class).includeAllPackagesUnderHere().build();
    }
}