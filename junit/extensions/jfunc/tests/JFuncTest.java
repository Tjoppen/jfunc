package junit.extensions.jfunc.tests;

import junit.framework.*;
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
        suite.oneInstancePerTest(true); // default already
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
        suite.oneInstancePerTest(false);
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
        //suite.oneInstancePerTest(false);
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

    public static class InnerTest extends JFuncTestCase {

        public InnerTest() {
            //this(false);
        }

        public InnerTest(boolean fatal) {
            //setFatal(fatal);
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
