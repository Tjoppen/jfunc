package junit.extensions.jfunc.tests;

import junit.framework.*;
import junit.extensions.jfunc.util.*;

import java.util.*;
import java.lang.reflect.*;

public class ProxyTests extends TestCase {

    public ProxyTests(String name) {
        super(name);
    }

//      public void blahtestObjectProxy() throws Throwable {
//          Object vector = (Object)Enhancer.enhance(
//                                                   Object.class,
//                                                   new Class[]{},
                                                 
//                                                   new InvocationHandler(){
 
//           public Object invoke( Object obj,java.lang.reflect.Method method,
//                                       Object args[] )
//               throws java.lang.Throwable{
//               System.out.println(method);
//               return null;
//           }
//          });
//          vector.toString();
//      }

    public void testObjectInterceptor() throws Throwable {
        Object object = (Object)Enhancer.enhance(
                                                 Object.class,
                                                 new Class[]{},
                                                 
                                                 new MethodInterceptor(){
                                                         
        public Object invoke( Object obj,java.lang.reflect.Method method,
                                     Object args[]/*, Object retValFromBefore*/ )
             throws java.lang.Throwable{
            System.err.println("InvocationHandler.invoke()");
             return null;
         }

         public boolean invokeSuper( Object obj,java.lang.reflect.Method method,
                                     Object args[]/*, Object retValFromBefore*/ )
             throws java.lang.Throwable{
            System.err.println("MethodInterceptor.invokeSuper()");
             return false;
         }
                                                         
                                                         
         public Object afterReturn(  Object obj,     
                                     java.lang.reflect.Method method,
                                     Object args[],  
                                     /*Object retValFromBefore,*/
                                     boolean invokedSuper, 
                                     Object retValFromSuper,
                                     java.lang.Throwable e )
             throws java.lang.Throwable{
             System.err.println("MethodInterceptor.afterReturn()");
             System.out.println(method);
             return retValFromSuper;//return the same as supper
         }
                                                         
     });
        object.toString();
    }

//      public void blahtestVectorProxy() throws Throwable {
//          Vector vector = (Vector)Enhancer.enhance(
//                                                   Vector.class,
//                                                   new Class[]{List.class},
                                                 
//                                                   new InvocationHandler(){
 
//           public Object invoke( Object obj,java.lang.reflect.Method method,
//                                       Object args[] )
//               throws java.lang.Throwable{
//               System.out.println(method);
//               return null;
//           }
                                                         
//  //           public boolean invokeSuper( Object obj,java.lang.reflect.Method method,
//  //                                       Object args[]/*, Object retValFromBefore*/ )
//  //               throws java.lang.Throwable{
//  //               return true;
//  //           }
                                                         
                                                         
//  //           public Object afterReturn(  Object obj,     
//  //                                       java.lang.reflect.Method method,
//  //                                       Object args[],  
//  //                                       /*Object retValFromBefore,*/
//  //                                       boolean invokedSuper, 
//  //                                       Object retValFromSuper,
//  //                                       java.lang.Throwable e )
//  //               throws java.lang.Throwable{
//  //               System.out.println(method);
//  //               return retValFromSuper;//return the same as supper
//  //           }
                                                         
//       });
//          vector.add(new Object());
        
//      }
}
