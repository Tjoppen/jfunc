package junit.extensions.jfunc.samples;

import junit.framework.*;
import junit.extensions.jfunc.*;
import junit.extensions.jfunc.runner.UsageException;

/**
 * This sample illustrates the different means of constructing suites
 * of tests.  They will all produce at least 1 failure, 1 error, and 4
 * runs (with the exception of unsafe which will only produce 3).
 **/
public class SuiteConstruction extends JFuncTestCase {

    private boolean sharing = false;
    /**
     * For proxies only!
     **/
    public SuiteConstruction() {
        setFatal(false);
    }

    public SuiteConstruction(String name) {
        super(name);
    }

    public void testSharing() {
        assert("sharing should always should start out false", sharing == false);
        sharing = true;
    }

    public void testPassed() {
        assert(true);
    }

    public void testFailed() {
        assert(false);
    }

    public void testErrored() {
        throw new RuntimeException();
    }

    public void testWithArgs(Object arg) {
        assertNotNull(arg);
    }

    public static Test suiteTypeSafe() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SuiteConstruction("passed") {
                protected void runTest() { testPassed(); }
            });
        suite.addTest(new SuiteConstruction("failed") {
                protected void runTest() { testFailed(); }
            });
        suite.addTest(new SuiteConstruction("errored") {
                protected void runTest() { testErrored(); }
            });
        suite.addTest(new SuiteConstruction("args") {
                protected void runTest() { testWithArgs(new Object()); }
            });
        return suite;
    }

    public static Test suiteNotSafe() {
        TestSuite suite = new TestSuite();
        // this may break at runtime in the event any of these methods change
        suite.addTest(new SuiteConstruction("testPassed"));
        suite.addTest(new SuiteConstruction("testFailed"));
        suite.addTest(new SuiteConstruction("testErrored"));
        // can't give arguments to a test using this method
        //suite.addTest(new SuiteConstruction("testWithArgs"));
        return suite;
    }

    public static Test suiteUsingProxy() {
        JFuncSuite suite = new JFuncSuite();
        SuiteConstruction test = new SuiteConstruction();
        test = (SuiteConstruction) suite.getTestProxy(test);
        test.testPassed();
        test.testFailed();
        test.testErrored();
        test.testWithArgs(new Object());
        return suite;
    }

    public static Test suiteSharing() {
        JFuncSuite suite = new JFuncSuite();
        suite.oneTest(true); // default is false
        SuiteConstruction test = new SuiteConstruction();
        test = (SuiteConstruction) suite.getTestProxy(test);
        test.testSharing();
        test.testSharing();
        return suite;
    }

    public static Test suite(String[] args) throws UsageException {
        if (args.length == 1) {
            if (args[0].equals("typesafe")) {
                return suiteTypeSafe();
            } else if (args[0].equals("unsafe")) {
                return suiteNotSafe();
            } else if (args[0].equals("proxy")) {
                return suiteUsingProxy();
            } else if (args[0].equals("sharing")) {
                return suiteSharing();
            }
        }
        throw new UsageException("Invalid arguments given must be " +
                                 "(typesafe|unsafe|proxy|sharing)");
    }

}
