package junit.extensions.jfunc.samples;

import junit.framework.*;
import junit.extensions.jfunc.*;

/**
 * Some simple tests.
 *
 */
public class SimpleTest extends JFuncTestCase {

    private boolean sharing = false;

    /**
     * This is used by the proxy and no one else.
     **/
    public SimpleTest() {
    }

    public SimpleTest(String name) {
        super(name);
        setFatal(false);
    }

    public SimpleTest(String name, boolean failsAreFatal) {
        super(name);
        setFatal(failsAreFatal);
    }
           
    protected void setUp() {
    }

    public void testMultipleFailures() {
        fail("failing once");
        fail("failing twice");
        fail("failing once again");
    }

    public void testVerboseAssertions() {
        vassert("good old numbers", 1, 1);
        vassert("good old numbers", 3, 1);
        vassert("good old strings", "hi", "hi");
        vassert("good old strings", "hi", "bye");
        vassert("Found some similarities", "No similarities found", true);
        vassert("Found some similarities", "No similarities found", false);
    }

    public int testInstantResult(boolean result) {
        assert(result);
        return JFuncResult.UNDEF;
    }

    public int testInstantError() {
        throw new RuntimeException("blah");
    }

    /**
     * This test illustrates the differences when using the
     * oneTestInstancePerTest().
     **/
    public void testSharing() {
        assert("sharing should always start out false", sharing == false);
        sharing = true;
    }

    public void testThrows() {
        throw new RuntimeException("exception message");
    }

    public static Test suite(String[] args) {
        if (args.length == 0) {
            return suite();
        }
        //System.err.println("using args suite generator");
        if (args[0].equals("testInstantResult")) {
            JFuncSuite suite = new JFuncSuite();
            SimpleTest test = new SimpleTest();
            suite.oneTest(true);
            test = (SimpleTest) suite.getTestProxy(test);
            boolean pass = false;
            if (args.length > 1 && args[1].equals("true")) {
                pass = true;
            }
            test.testInstantResult(pass);
            return suite;
        }
        return new SimpleTest(args[0]);

    }

    public static Test suite() {
        //System.err.println("using arg-less suite constructor");
        /*
         * the type safe way
         *
        // more like type ugly :)
         TestSuite suite= new TestSuite();
         suite.addTest(
         new SimpleTest("add") {
         protected void runTest() { testAdd(); }
         }
         );

         suite.addTest(
         new SimpleTest("testDivideByZero") {
         protected void runTest() { testDivideByZero(); }
         }
         );
         return suite;
        */

        /*
         * the dynamic way
         */
        return new TestSuite(SimpleTest.class);
    }
}
