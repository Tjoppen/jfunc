package junit.extensions.jfunc;

import com.develop.delegator.ProxyLoader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.Protectable;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/**
 * <p>The ProxyTestSuite uses dynamic proxies for classes to make
 * suite construction in Junit easier and emplore more compile time
 * checks.  This suite will only be useful to you, if you have to
 * construct your suites by hand.  If you simply allow your suites to
 * be dynamically generated, just keep doing what you've been doing.
 *
 * <p>Instead of calling something like this:
 *
 * <blockquote><code><pre>
 * suite.add(new TestTest("testPassed"));
 * suite.add(new TestTest("testFailed"));
 * suite.add(new TestTest("testErrored"));
 * // can't do this with the current TestCase in Junit
 * // suite.add(new TestTest("testArgs", new Object[] { "ugly" }));
 * return suite;
 * </pre></code></blockquote>
 *
 * <p>The new code style will look much more javaish:
 *
 * <blockquote><code><pre>
 * TestTest tc = (TestTest) suite.getTestProxy(new TestTest());
 * tc.testPassed();
 * tc.testFailed();
 * tc.testErrored();
 * // TestProxies can accept arguments
 * // tc.testArgs("pretty");
 * return suite;
 * </pre></code></blockquote>
 *
 * <p>These two pieces of code are effectively the same (except for
 * the testlet that accepts arguments), except one is cleaner, easier
 * to read in my opinion and has more checks that happen at compile
 * time.
 *
 * @author Shane Celis <shane@terraspring.com>
 **/
public class JFuncSuite extends TestSuite {

    /**
     * Controls whether or not a new instance of the test is 
     * created every time a test is added to the suite.
     **/
    private boolean oneInstancePerTest = true;

    /**
     * Constructs a test suite object.
     **/
    public JFuncSuite() {
        super();
    }

    /**
     * Defaults to regular Junit behavior of one instance of the class
     * per test (true).  If given a false, however, there will be only
     * one instance of the test from which various method calls are
     * made.  This kind of functionality should be used sparingly as
     * the state of the test object may be important and tests can
     * interfere with one anothers data.  (<b>Read:</b> Don't use it
     * if you can avoid it.)
     **/
    public void oneInstancePerTest(boolean yes) {
        oneInstancePerTest = yes;
    }

    /**
     * Returns an object that looks like the given test object, and
     * can be cast to that object.  This facilitates a better means of
     * constructing testcases.  
     **/
    public Test getTestProxy(Test test) throws InstantiationException {
        // Note that this proxy/invocation handler is now tied to "this" suite.
          InvocationHandler handler = new TestProxy(this, test);
          Class cl = test.getClass();

          //throw new InstantiationException("Need to sort out proxy issues");
          // May need to do a complete rewrite using BCEL library

          // Based on Sun's Proxy code (license issues)
//          return (Test) ProxyPlus.newProxyInstance(
//                   cl.getClassLoader(), new Class[0], cl, handler);


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
    }
    

    /**
     * A proxy that looks like the test object.
     **/
    class TestProxy implements InvocationHandler {

        Test test;
        TestSuite suite;
        TestProxy(TestSuite s, Test t) {
            suite = s;
            test = t;
        }

        /**
         * Since we're actually receiving arguments now, it would be
         * quite easy for us to make the test methods accept
         * arguments.
         **/
        public Object invoke(Object proxy, Method method, Object[] args) {
            Test t = null;
            if (oneInstancePerTest) {
                /**
                 * Construct a new test object.  The standard way JUnit
                 * tries to maintain this with it's TestCase object.
                 **/
                try {
                    // why aren't I cloning these instead?
                    t = (Test) test.getClass().newInstance();
                } catch (Exception e) {
                    suite.addTest(warning(e.toString()));
                    return null;
                } 
            } else {
                /**
                 * Doesn't construct a new test object.  Instead
                 * provides a wrapper to reference the particular test
                 * method (I call them testlets).
                 **/
                t = test;
            }
            suite.addTest(new TestletWrapper(t, method, args));
            return null;
        }
    }

    /**
     * Returns a test which will fail and log a warning message.
     */
    private static Test warning(final String message) {
        return new TestCase("warning") {
                protected void runTest() {
                    fail(message);
                }
            };              
    }

}
