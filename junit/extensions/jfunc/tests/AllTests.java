package junit.extensions.jfunc.tests;

import junit.framework.*;

public class AllTests {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(JFuncTest.class);
        suite.addTestSuite(ProxyTests.class);
        //suite.addTestSuite(SweetTest.class);
        return suite;
    }
}
