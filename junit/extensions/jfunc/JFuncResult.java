package junit.extensions.jfunc;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestListener;
import junit.framework.AssertionFailedError;

import java.util.Vector;
import java.util.Enumeration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

/*
 * XXX The second is a proxy of a test object, that unlike the
 * JFuncTestSuite that creates a suite, the JFuncTestResult.
 */
/**
 * JFuncTestResult passes verbose assertions to the
 * <code>AssertListener</code>.  And also allows for tests to run
 * without a suite through a proxy mechanism.  Since these tests are
 * running "live", the result of the test can be passed back
 * immediately, which is useful in cases where you might only run test
 * B if test A passes.
 * 
 * @author Shane Celis <shane@terraspring.com>
 **/
public class JFuncResult extends TestResult implements AssertListener {

    public static final int UNDEF = -1;
    public static final int PASSED = 0;
    public static final int FAILURE = 1;
    public static final int ERROR = 2;

    public JFuncResult() {
        super();
    }

    public synchronized void addAssert(Test test, 
                                       String message, 
                                       boolean condition) {
        for (Enumeration e= cloneListeners().elements(); e.hasMoreElements(); ) {
            Object listen = e.nextElement();
            if (listen instanceof AssertListener) 
                ((AssertListener)listen).addAssert(test, message, condition);
        }
    }

    private synchronized Vector cloneListeners() {
        return (Vector) fListeners.clone();
    }

    public Test getTestProxy(Test test) throws InstantiationException {
        InvocationHandler handler = new RunningTestProxy(this, test);
        Class cl = test.getClass();
        if (test instanceof JFuncAssert) {
            ((JFuncAssert)test).setResult(this);
        }
        //throw new InstantiationException("no proxy generator yet");
        try {
            Class proxy = TestletWrapper.getProxy(new Class[0], cl);
            Constructor cons = proxy.getConstructor(
                                     new Class[] { InvocationHandler.class });
            return (Test) cons.newInstance(new Object[] { handler });
        } catch (InstantiationException ie) {
            throw ie;
        } catch (Exception e) {
            throw new InstantiationException(e.toString());
        }
        
//      return (Test) ProxyPlus.newProxyInstance(
//                         cl.getClassLoader(), new Class[0], cl, handler);
    }

    class RunningTestProxy implements InvocationHandler {
        Test test;
        TestResult result;

        public RunningTestProxy(TestResult r, Test t) {
            result = r;
            test = t;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) {
            // figure out how to make this thing return a value
            // might want to have just one listener who's value is flushable
            Listener listener = new Listener();
            result.addListener(listener);
            new TestletWrapper(test, method, args).run(result);
            result.removeListener(listener);
            return new Integer(listener.status());
            //return null;
        }
    }

    class Listener implements AssertListener {
        boolean gotAssert = false;
        boolean gotFailure = false;
        boolean gotError = false;

        /**
         * An assert happened.
         **/
        public void addAssert(Test test, String msg, boolean condition) {
            gotAssert = true;
        }

        public void addError(Test test, Throwable t) {
            gotError = true;
        }
        public void addFailure(Test test, AssertionFailedError t) {
            gotFailure = true;
        }
        public void endTest(Test test) {}
        public void startTest(Test test) {}

        public int status() {
            if (gotError) {
                return ERROR;
            } else if (gotFailure) {
                return FAILURE;
            } else {
                return PASSED;
            }
        }
                  
    }   

}
