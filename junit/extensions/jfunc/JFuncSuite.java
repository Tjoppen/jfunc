package junit.extensions.jfunc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.Protectable;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
/**
 * <p>The JFuncSuite uses dynamic proxies for classes to make
 * suite construction in JUnit easier and emplore more compile time
 * checks.  This suite will only be useful to you, if you have to
 * construct your suites by hand.  If you simply allow your suites to
 * be dynamically generated, just keep doing what you've been doing.
 *
 * <p>Instead of calling something like this:
 *
 * <blockquote><code><pre>
 * suite.add(new SimpleTest("testPassed"));
 * suite.add(new SimpleTest("testFailed"));
 * suite.add(new SimpleTest("testErrored"));
 * // can't do this with the current TestCase in Junit
 * // suite.add(new SimpleTest("testWithArgs", new Object[] { "ugly" }));
 * return suite;
 * </pre></code></blockquote>
 *
 * <p>The new code style will look much more javaish:
 *
 * <blockquote><code><pre>
 * SimpleTest tc = (SimpleTest) suite.getTestProxy(new SimpleTest());
 * tc.testPassed();
 * tc.testFailed();
 * tc.testErrored();
 * // TestProxies can accept arguments
 * // tc.testWithArgs("pretty");
 * return suite;
 * </pre></code></blockquote>
 *
 * <p>These two pieces of code are effectively the same, except one is
 * cleaner, easier to read in my opinion and has more checks that
 * happen at compile time.
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
     * This kind of functionality should be used sparingly as the
     * state of the test object may be important and tests can
     * interfere with one anothers data.  (<b>Read:</b> Don't use it
     * if you can avoid it.)  Many functional testers will have a hard
     * time avoiding it, so if you're using static member variables,
     * this is a better solution.
     *
     * @param justOne true will use just one instance of the test
     * (added JFunc behavior).  false will have the suite use a new
     * test for every test (default JUnit behavior).
     **/
    public void oneTest(boolean justOne) {
        manyTests(!justOne);
        //oneInstancePerTest = !justOne;
    }

    /**
     * @param many true will have the suite use many tests (default JUnit
     * behavior).  false will use just one instance of the test (added JFunc
     * behavior).
     **/
    public void manyTests(boolean many) {
        oneInstancePerTest = many;
    }

    /**
     * Returns an object that looks like the given test object, and
     * can be cast to that object.  This facilitates a better means of
     * constructing testcases.  
     **/
    public Test getTestProxy(Test test) {
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
              throw new RuntimeException(ie.toString());
          } catch (InvocationTargetException ite) {
              throw new RuntimeException(ite.getTargetException().toString());
          } catch (Exception e) {
              throw new RuntimeException(e.toString());
          }
    }

    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        setUpOnce(result);
        super.run(result);
        tearDownOnce(result);
    }

    private Set getTests() {
        Set tests = new HashSet();
        for (Enumeration e= tests(); e.hasMoreElements(); ) {
            Test test = (Test)e.nextElement();
            if (test instanceof TestletWrapper) {
                test = ((TestletWrapper)test).getTestInstance();
            }
            tests.add(test);
        }
        return tests;
    }

    /**
     * This doesn't really work the way I want it to just yet.  It
     * only works the way I intended it too when you're using
     * <code>suite.oneTest(true)</code>
     **/
    protected void setUpOnce(TestResult result) {
        for(Iterator i = getTests().iterator(); i.hasNext();) {
            Test test = (Test) i.next();
            if (test instanceof JFuncTestCase) {
                try {
                    ((JFuncTestCase)test).setUpOnce();
                } catch (Exception e) {
                    result.addError(test, e);
                }
            }
        }
    }

    protected void tearDownOnce(TestResult result) {
        for(Iterator i = getTests().iterator(); i.hasNext();) {
            Test test = (Test) i.next();
            if (test instanceof JFuncTestCase) {
                try {
                    ((JFuncTestCase)test).tearDownOnce();
                } catch (Exception e) {
                    result.addError(test, e);
                }
            }
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
                    // oh, now I remember.  
                    // cloning is bad anyhow... but this doesn't take into
                    // account that the constructor might actually set up
                    // some state information.  I could unprotected clone()
                    // reflectively and then use it?
//                      if (test instanceof Cloneable) {
//                          t = (Test) test.clone(); //getClass().newInstance();
//                      } else {
                        t = (Test) test.getClass().newInstance();
//                      }
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
