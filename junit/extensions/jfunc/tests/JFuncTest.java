package junit.extensions.jfunc.tests;

import junit.framework.*;
import junit.extensions.jfunc.*;

/**
 * Tests the JFunc framework itself
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

    public void testRunningNoMultipleFailures() throws Exception {
        JFuncResult result = new JFuncResult();
        SimpleTest test = new SimpleTest("testMultipleFailures", true);
        test = (SimpleTest) result.getTestProxy(test);
        // XXX this won't work
        // test.run(result);
        test.testMultipleFailures();
//          for(java.util.Enumeration e = result.failures(); e.hasMoreElements();) {
//              Object o = e.nextElement();
//              System.err.println(o.toString());
//          }
        assert("should have only had one failure", result.failureCount() == 1);
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
