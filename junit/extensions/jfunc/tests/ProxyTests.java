package junit.extensions.jfunc.tests;

import junit.framework.*;
import junit.extensions.jfunc.util.*;

import java.util.*;
import java.lang.reflect.*;

public class ProxyTests extends TestCase {

    public ProxyTests(String name) {
        super(name);
    }

    public void testUnproxiable() throws Exception {
        try {
            Method method = Object.class.getMethod("toString",
                                                   new Class[] {});
            Object object = (Object)ProxyPlus.newProxyInstance(new Class[] {},
                                                             Unproxiable.class,
                                                             new Handler(method));
            object.toString();
            fail();
        } catch (Exception e) {
        }
    }

    public void testObjectProxy() throws Exception {
        Method method = Object.class.getMethod("toString",
                                               new Class[] {});
        Object object = (Object)ProxyPlus.newProxyInstance(
                                                 new Class[]{},
                                                 Object.class,
                                                 new Handler(method));
        object.toString();
    }

    public void testVectorProxy() throws Exception {
        Object added = new Object();
        Method method = Vector.class.getMethod("add",
                                               new Class[] {
                                                   Object.class
                                               });
        Vector object = (Vector)ProxyPlus.newProxyInstance(
                                                 new Class[] {},
                                                 Vector.class,
                                                 new Handler(method, 
                                                             new Object[] {
                                                                 added }));
        object.add(added);
        //object.add(new Object()); // should fail
        //object.remove(added); // should fail
    }

    public void testIsProxy() throws Exception {
        testIsProxy(Vector.class);
    }

    public void testIsProxy(Class cl) throws Exception {
        Class proxy = ProxyPlus.getProxyClass(new Class[] {},
                                              cl);
        assertNotNull("proxy is null", proxy);
        assertTrue("proxy isn't recognized", 
                   ProxyPlus.isProxyPlusClass(proxy));
        assertTrue("proxy shouldn't be recognized", 
                   !ProxyPlus.isProxyClass(proxy));
    }

    // proxies must have default constructor, unless you do a -noverify
    class Unproxiable {
        private Unproxiable() { }
    }
    
    // static asserts are pretty cool
    class Handler extends Assert implements InvocationHandler {
        public Method expectedMethod;
        public Object[] expectedArgs;

        public Handler(Method method) {
            this.expectedMethod = method;
        }

        public Handler(Method method, Object[] args) {
            this.expectedMethod = method;
            this.expectedArgs = args;
        }

        public Handler() { }

        public Object invoke(Object obj,
                             Method method,
                             Object args[])
            throws java.lang.Throwable{
            // you can get yourself into trouble here... 
            // calling obj.toString() will cause a bus error or stack overflow
            // (it's recursive after all)
            assertNotNull(obj);
            assertNotNull(method);
            assertNotNull(args);
            if (expectedMethod != null) 
                assertEquals("method incorrect", expectedMethod, method);
            if (expectedArgs != null) {
                for(int i = 0; i < expectedArgs.length; i++) {
                    assertTrue("args incorrect", expectedArgs[i] == args[i]);
                }
            }
//              System.err.println("InvocationHandler.invoke("  +obj.getClass() + 
//                                 ", " + method + "," + args + ")");
            
            //throw new Exception("hah");
            return null;
        }
    }
}
