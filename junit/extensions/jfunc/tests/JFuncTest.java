package junit.extensions.jfunc.tests;

import junit.framework.*;
import junit.extensions.*;
import junit.extensions.jfunc.*;
import junit.extensions.jfunc.samples.*;

/**
 * Tests the JFunc framework itself.  This isn't meant to demonstrate
 * how to use the JFunc framework.  It only tests the framework.
 * Check out the samples directory for examples of use.
 **/
public class JFuncTest extends TestCase {

    /**
     * For proxy construction only.
     **/
    public JFuncTest() {
        super("");
    }       

    public JFuncTest(String name) {
        super(name);
    }

    public void testNoMultipleFailures() {
        TestResult result = new TestResult();
        Test test = new SimpleTest("testMultipleFailures", true);
        test.run(result);
        assert("should have only had one failure", result.failureCount() == 1);
    }

    public void testMultipleFailures() {
        TestResult result = new TestResult();
        Test test = new SimpleTest("testMultipleFailures", false);
        test.run(result);
        assert("should have only had three failures", result.failureCount() == 3);
    }

    public void testRunningMultipleFailures() throws Exception {
        JFuncResult result = new JFuncResult();
        SimpleTest test = new SimpleTest("", false);
        test = (SimpleTest) result.getTestProxy(test);
        // This errored because it's calling the super-super classes
        // run method, which expects the name to be good
        //test.run(result);
        test.testMultipleFailures();
        assert("should have had three failures instead: "+ result.failureCount(), 
               result.failureCount() == 3);
    }

    public void testTestletWrapper() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        SimpleTest test = new SimpleTest();
        test = (SimpleTest) suite.getTestProxy(test);
        test.testMultipleFailures();
        TestletWrapper wrapper = (TestletWrapper) suite.testAt(0);
        TestFailure failure = new TestFailure(wrapper, new Exception("testing"));
        //System.err.println(wrapper.toString());
        assert(wrapper.toString().equals("SimpleTest.testMultipleFailures()"));
        assert(wrapper.name().equals("testMultipleFailures"));
        assert(failure.toString().equals("SimpleTest.testMultipleFailures(): testing"));
        //System.err.println(failure);
    }

    public void testJFuncTestCase() throws Exception {
        JFuncTestCase test = new SimpleTest("testMultipleFailures");
        TestFailure failure = new TestFailure(test, new Exception("testing"));
        assert(test.toString().equals("SimpleTest.testMultipleFailures()"));
        assert(test.name().equals("testMultipleFailures"));
        assert(failure.toString().equals("SimpleTest.testMultipleFailures(): testing"));
    }

    public void testReturnValues() throws Exception {
        JFuncResult result = new JFuncResult();
        SimpleTest test = new SimpleTest();
        test = (SimpleTest) result.getTestProxy(test);
        // XXX this won't work
        // test.run(result);
        assert ("didn't return expect result", 
                test.testInstantResult(true) == JFuncResult.PASSED);
        assert ("didn't return expect result", 
                test.testInstantResult(false) == JFuncResult.FAILED);
        assert ("didn't return expect result", 
                test.testInstantError() == JFuncResult.ERRORED);
    }

    public void testNoMemberSharing() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        TestResult result = new TestResult();
        suite.oneTest(false); // default already
        SimpleTest test = (SimpleTest) suite.getTestProxy(new SimpleTest());
        // create a proxy of SimpleTest to build the suite
        test.testSharing();
        test.testSharing();
        // run the suite which will execute two tests, which should both fail
        suite.run(result);
        int failures = result.failureCount();
        assert("should have zero failures in result not " + failures, 
               failures == 0);
    }

    public void testMemberSharing() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        TestResult result = new TestResult();
        suite.oneTest(true);
        SimpleTest test = (SimpleTest) suite.getTestProxy(new SimpleTest());
        // create a proxy of SimpleTest to build the suite
        test.testSharing();
        test.testSharing();
        // run the suite which will execute two tests, which should both fail
        suite.run(result);
        assert("should have one failure in result", result.failureCount() == 1);
    }

    public void testRunningProxies() throws Exception {
        //JFuncSuite suite = new JFuncSuite();
        JFuncResult result = new JFuncResult();
        SimpleTest test = (SimpleTest) result.getTestProxy(new SimpleTest());
        // create a proxy of SimpleTest to build the suite
        assert(test.testInstantResult(true) == 0);
        assert(test.testInstantResult(false) == 1);
        assert(test.testInstantError() == 2);
    }


    public void testVerboseAsserts() {
        boolean gotAssert = false;
        JFuncResult result = new JFuncResult();
        Test test = new SimpleTest("testVerboseAssertions");
        Listener listen = new Listener();
        result.addListener(listen);
        test.run(result);
        assert("failed to pass verbose assertion to listener", listen.gotAssert);
    }

    public void testInnerTest() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        InnerTest test = new InnerTest();
        test = (InnerTest) suite.getTestProxy(test);
        test.testPassed();
        test.testFailed();
        //junit.extensions.jfunc.textui.JFuncRunner.run(suite);
    }

    public void testSetUp() throws Exception {
        TestResult result = new TestResult();
        InnerTest test = new InnerTest("testPassed");
        test.run(result);
        assert("setUp() wasn't called", test.calledSetUp == 1);
        assert("tearDown() wasn't called", test.calledTearDown == 1);
        
    }

    public void testProxySetUpOneTest() throws Exception {
        TestResult result = new TestResult();
        JFuncSuite suite = new JFuncSuite();
        InnerTest thetest = new InnerTest();
        suite.oneTest(true);
        InnerTest test = (InnerTest) suite.getTestProxy(thetest);
        test.testPassed();
        suite.run(result);
        assert("setUp() wasn't called", thetest.calledSetUp == 1);
        assert("tearDown() wasn't called", thetest.calledTearDown == 1);
    }

    public void testProxySetUp() throws Exception {
        TestResult result = new TestResult();
        JFuncSuite suite = new JFuncSuite();
        InnerTest thetest = new InnerTest();
        //suite.oneTest(true);
        InnerTest test = (InnerTest) suite.getTestProxy(thetest);
        test.testPassed();
        suite.run(result);
        /**
         * The actual test is a little difficult to get at, because
         * it's wrapped by the test wrapper object and it's
         * cloned/newInstance so you can't simply keep a reference to
         * it as you would in testProxySetUpOneTest()
         **/
        thetest = (InnerTest) ((TestletWrapper)suite.testAt(0)).getTestInstance();
        assert("setUp() wasn't called", thetest.calledSetUp == 1);
        assert("tearDown() wasn't called", thetest.calledTearDown == 1);
    }

    public void testStandardSetUpOnce() throws Exception {
        TestSuite suite = new TestSuite();
        TestResult result = new TestResult();
        InnerSetup setup = new InnerSetup(suite);
        suite.addTest(new InnerTest("testPassed"));
        setup.run(result);
        assert("setup incorrect ", setup.calledSetUp == 1);
        assert("teardown incorrect ", setup.calledTearDown == 1);
    }

    public void testSetUpOnce() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        TestResult result = new TestResult();
        InnerTest tests[] = new InnerTest[] { new InnerTest(), new InnerTest() };
        suite.oneTest(true);
        InnerTest proxy = (InnerTest) suite.getTestProxy(tests[0]);
        InnerTest proxy2 = (InnerTest) suite.getTestProxy(tests[1]);
        //suite.addTest(test);
        proxy.testPassed();
        proxy.testFailed();
        proxy2.testPassed();
        proxy2.testFailed();
        suite.run(result);
        for (int i = 0; i < tests.length; i++) {
            InnerTest test = tests[i];
            assert("setup incorrect ", test.calledSetUp == 2);
            assert("teardown incorrect ", test.calledTearDown == 2);
            assert("setup once incorrect ", test.calledSetUpOnce == 1);
            assert("teardown once incorrect ", test.calledTearDownOnce == 1);
        }
    }

    public void testProxyOneTestSetUpOnce() throws Exception {
        TestResult result = new TestResult();
        JFuncSuite suite = new JFuncSuite();
        InnerTest thetest = new InnerTest();
        //suite.oneTest(true);
        InnerTest test = (InnerTest) suite.getTestProxy(thetest);
        test.testPassed();
        test.testFailed();
        suite.run(result);
        /**
         * The actual test is a little difficult to get at, because
         * it's wrapped by the test wrapper object and it's
         * cloned/newInstance so you can't simply keep a reference to
         * it as you would in testProxySetUpOneTest()
         **/
        thetest = (InnerTest) ((TestletWrapper)suite.testAt(0)).getTestInstance();
        InnerTest othertest = (InnerTest) 
            ((TestletWrapper)suite.testAt(1)).getTestInstance();
        assert(thetest != othertest);
        assert("setup incorrect", thetest.calledSetUp == 1);
        assert("teardown incorrect", thetest.calledTearDown == 1);
        assert("setup once incorrect ", thetest.calledSetUpOnce == 1);
        assert("teardown once incorrect ", thetest.calledTearDownOnce == 1);
        assert("setup incorrect", othertest.calledSetUp == 1);
        assert("teardown incorrect", othertest.calledTearDown == 1);
        assert("setup once incorrect ", othertest.calledSetUpOnce == 1);
        assert("teardown once incorrect ", othertest.calledTearDownOnce == 1);
    }



    public static class InnerSetup extends TestSetup {
        public int calledSetUp;
        public int calledTearDown;

        public InnerSetup(Test test) {
            super(test);
        }

        protected void setUp() {                
            calledSetUp++;
        }
        protected void tearDown() {
            calledTearDown++;
        }
    }
                    

    public static class InnerTest extends JFuncTestCase {
        public int calledSetUp;
        public int calledTearDown;
        public int calledSetUpOnce;
        public int calledTearDownOnce;

        public InnerTest() {
            //this(false);
        }

        public InnerTest(boolean fatal) {
            //setFatal(fatal);
        }

        public InnerTest(String test) {
            super(test);
        }

        protected void setUp() {
            calledSetUp++;
        }

        protected void tearDown() {
            calledTearDown++;
        }

        protected void setUpOnce() {
            calledSetUpOnce++;
        }

        protected void tearDownOnce() {
            calledTearDownOnce++;
        }

        public void testPassed() {
            assert(true);
        }

        public void testFailed() {
            assert(false);
        }

    }

    class Listener implements AssertListener {
        boolean gotAssert = false;

        /**
         * An assert happened.
         **/
        public void addAssert(Test test, String msg, boolean condition) {
            gotAssert = true;
        }

        public void addError(Test test, Throwable t) {}
        public void addFailure(Test test, AssertionFailedError t) {}
        public void endTest(Test test) {}
        public void startTest(Test test) {}
    }   
}
