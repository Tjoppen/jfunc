package junit.extensions.jfunc.tests;

import junit.framework.*;
import junit.extensions.jfunc.util.*;
import java.util.*;

public class ProxyTests extends TestCase {

    public ProxyTests(String name) {
        super(name);
    }

    public void testVectorProxy() throws Throwable {
        Vector vector = (Vector)Enhancer.enhance(
                                                 Vector.class,
                                                 new Class[]{List.class},
                                                 
                                                 new MethodInterceptor(){
 
         public Object beforeInvoke( Object obj,java.lang.reflect.Method method,
                                     Object args[] )
             throws java.lang.Throwable{
             return null;
         }
                                                         
         public boolean invokeSuper( Object obj,java.lang.reflect.Method method,
                                     Object args[]/*, Object retValFromBefore*/ )
             throws java.lang.Throwable{
             return true;
         }
                                                         
                                                         
         public Object afterReturn(  Object obj,     
                                     java.lang.reflect.Method method,
                                     Object args[],  
                                     /*Object retValFromBefore,*/
                                     boolean invokedSuper, 
                                     Object retValFromSuper,
                                     java.lang.Throwable e )
             throws java.lang.Throwable{
             System.out.println(method);
             return retValFromSuper;//return the same as supper
         }
                                                         
     });
        vector.add(new Object());
        
    }
}
