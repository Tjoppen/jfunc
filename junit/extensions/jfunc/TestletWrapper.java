package junit.extensions.jfunc;

import junit.framework.*;
import com.develop.delegator.ProxyLoader;

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
    }

    public String name() {
        return method.getName();
    }
        
    public int countTestCases() {
        return 1;
    }
        
    public void run(final TestResult result) {
        result.startTest(this);
            
        Protectable p = new Protectable() {
                public void protect() throws Throwable {
                    runBare(result);
                }
            };
        result.runProtected(this, p);
            
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
        return instance.getClass().getName() + "." + name() + "(" +
            arg + ")";
    }

    public static Class getProxy(Class[] interfaces, Class superclass) {
        Class cl = superclass;
        // Based on JCFE work (doesn't work well)
        return ProxyLoader.getProxyClass(cl.getClassLoader(), interfaces, cl);

        // Based on Sun's Proxy code (license issues)
//          return (Test) ProxyPlus.newProxyInstance(
//                   cl.getClassLoader(), new Class[0], cl, handler);

    }

}
