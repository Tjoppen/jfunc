package junit.extensions.jfunc;

import junit.framework.*;
import com.develop.delegator.ProxyLoader;
//import com.terraspring.hh.util.ProxyPlus;

import java.lang.reflect.*;

/**
 * This wrapper is used to wrap an instance of test and provide it
 * with a name and optionally arguments as well.  This was done by 
 * TestCase, but the beauty of this is now you can choose between 
 * using the same instance of the test or generating a new one.
 **/
public class TestletWrapper implements Test {

    final Test instance;
    final Method method;
    final Object[] args;
    private final boolean isJFuncTestCase;
    private final boolean isTestCase;

    TestletWrapper(Test instance, Method method) {
        this(instance, method, null);
    }

    TestletWrapper(Test instance, Method method, Object[] args) {
        if (instance == null || method == null) {
            // Fail fast
            throw new NullPointerException();
        }
        this.instance = instance;
        this.method = method;
        this.args = args;
        isJFuncTestCase = this.instance instanceof JFuncTestCase;
        isTestCase = this.instance instanceof TestCase;
    }

    public String name() {
        return method.getName();
    }
        
    public int countTestCases() {
        return 1;
    }
    
    protected void setUp(TestResult result) {
        try {
            if (isJFuncTestCase) {
                ((JFuncTestCase)instance).setUp();
            } else if (isTestCase) {
                junit.framework.Assert.fail("can't call testCase");
                //((TestCase)instance).setUp();
            }
        } catch (Exception e) {
            // mark as a setup failure
            result.addError(instance, e);
            //junit.framework.Assert.fail("setUp() exceptions");
        }
    }

    protected void tearDown(TestResult result) {
        try {
            if (isJFuncTestCase) {
                ((JFuncTestCase)instance).tearDown();
            } else if (isTestCase) {
                junit.framework.Assert.fail("can't call testCase");
                //((TestCase)instance).tearDown();
            }
        } catch (Exception e) {
            result.addError(instance, e);
            // mark as a setup failure
            //junit.framework.Assert.fail("tearDown() exceptions");
        }
    }
        
    public void run(final TestResult result) {
        boolean isJFuncTestCase = instance instanceof JFuncTestCase;
        if (instance instanceof JFuncAssert) {
            // XXX this isn't cool especially if we're running in a
            // multi-threaded environment.  Need to resolve it
            // somehow.

            // XXX might as well make our Assert class static, huh?
            ((JFuncAssert)instance).setResult(result);
            ((JFuncAssert)instance).setTest(this);
        }
        setUp(result);
        result.startTest(this);
        Protectable p = new Protectable() {
                public void protect() throws Throwable {
                    runBare(result);
                }
            };
        result.runProtected(this, p);
        tearDown(result);
        result.endTest(this);
    }
        
    public void runBare(final TestResult result) throws Throwable {
        try { 
            method.invoke(instance, (args == null) ? new Object[0] 
                          : args);
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        }
    }

    public Test getTestInstance() {
        return instance;
    }

    public String toString() {
        String arg = "";
        if (args != null && args.length > 0) {
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                buff.append(", " + args[i]);
            }
            buff.delete(0, 2);
            arg = buff.toString();
        }
        return JFuncTestCase.shortName(instance.getClass()) + "." + name() + "(" +
            arg + ")";
    }

    public static Class getProxy(Class[] interfaces, Class superclass) {
        Class cl = superclass;
        // Based on JCFE work (doesn't work as well)
        return ProxyLoader.getProxyClass(cl.getClassLoader(), interfaces, cl);

        // Based on Sun's Proxy code (license issues)
        //return ProxyPlus.getProxyClass(cl.getClassLoader(), new Class[0], cl);
    }

}
