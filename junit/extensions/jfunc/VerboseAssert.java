package junit.extensions.jfunc;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

/**
 * VerboseAssert allows for assertions to be displayed even if they
 * failed.  If you've ever tried printing your assertions that have
 * passed, you'll quickly realize that all the messages are written to
 * express a failure.  In order to present the tests outcome more
 * naturally, regardless of the outcome of an assert, you need at the
 * heart of the problem a message to express success and another for
 * failure.  A good place inbetween is a description of what you're
 * comparing, and showing the expected vs. actual.  It is this classes
 * aim to provide both of those things.
 *
 * @author Shane Celis <shane@terraspring.com>
 **/
public abstract class VerboseAssert extends JFuncAssert {

    public VerboseAssert() {
    }

    public VerboseAssert(Test test, TestResult result) {
        super(test, result);
    }


    /*----- Fails ------------------------------------------------------------*/

    // XXX this causes recursion currently
//      public void fail(String msg) {
//          assert(msg, false);
//      }

    /*----- Asserts ----------------------------------------------------------*/

    public void vassert(String successMessage,
                       String failureMessage,
                       boolean condition) {
        vassert((condition ? successMessage : failureMessage), condition);
    }

    public void vassert(String description,
                       boolean expected,
                       boolean actual) {
        vassert(description, new Boolean(expected), new Boolean(actual));
    }

    public void vassert(String description,
                       int expected,
                       int actual) {
        vassert(description, new Integer(expected), new Integer(actual));
    }
                       
    public void vassert(String description,
                       Object expected,
                       Object actual,
                       boolean condition) {
        String msg = (description != null) ? description + ": " : "" ;
        if (expected != null && actual != null) {
            msg += "EXPECTED(" + expected.toString() 
                + ") ACTUAL(" + actual.toString() + ")";
        }
        vassert(msg, condition);
    }

    public void vassert(String description,
                       Object expected,
                       Object actual) {
        vassert(description, expected, actual, expected.equals(actual));
    }

    // XXX following method seems to suggest that an assert is tightly tied
    // to a test...
    /** 
     * The root assert method grounded in JUnit.
     **/
    public void vassert(String msg, boolean condition) {
        //
        // XXX this way (use of getTestResult() and getTest()) is awkward and 
        // should but fixed somehow...
        //
        TestResult result = getResult();
        Test test = getTest();
        if (result != null && result instanceof AssertListener) {
            ((AssertListener)result).addAssert(test, msg, condition);
        }
        // debug
//          System.out.println("test " + test);
//          System.out.println("msg " + msg);
//          System.out.println("condition " + condition);
        
        super.assert(msg, condition);
    }
}
