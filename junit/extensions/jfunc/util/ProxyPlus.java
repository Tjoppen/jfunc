/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package junit.extensions.jfunc.util;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.lang.reflect.*;
import java.lang.ref.*;
import java.util.*;

/**
 * ProxyPlus provides static methods for creating dynamic proxy
 * classes and instances, but allows for more flexibility than typical
 * proxies in that it allows you to specify the superclass of the
 * proxy instance, rather than being restricing to subclasses of
 * Proxy.
 *
 * <p><b>Note:</b> parent class must provide a default constructor
 * either public or protected.  (If the interfaces aren't public there
 * may be problems with having a constructor that isn't public.)
 *
 * <p>To create a proxy for some class <code>Foo</code>:
 * <pre>
 *     InvocationHandler handler = new MyInvocationHandler(...);
 *     Class proxyClass = Proxy.getProxyClass(
 *         Foo.class.getClassLoader(), new Class[0], Foo.class);
 *     Foo f = (Foo) proxyClass.
 *         getConstructor(new Class[] { InvocationHandler.class }).
 *         newInstance(new Object[] { handler });
 * </pre>
 * or more simply:
 * <pre>
 *     Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
 *                                          new Class[0],
 *                                          Foo.class,
 *                                          handler);
 * </pre>
 * @author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 *<!-- if there is anyone I'm neglecting to mention for credit here, contact me-->
 * @author Shane Celis 
 *  <a href="mailto:shane@terraspring.com">shane@terraspring.com</a>
 * @version $Id: ProxyPlus.java,v 1.4 2002/07/01 20:33:12 semios Exp $
 **/
public class ProxyPlus extends Proxy {
    static final String INVOCATION_CLASS = InvocationHandler.class.getName();
    static final ObjectType BOOLEAN_OBJECT =
        new ObjectType(Boolean.class.getName());
    static final ObjectType INTEGER_OBJECT = 
        new ObjectType(Integer.class.getName());
    static final ObjectType CHARACTER_OBJECT = 
        new ObjectType(Character.class.getName());
    static final ObjectType BYTE_OBJECT = new ObjectType(Byte.class.getName());
    static final ObjectType SHORT_OBJECT = new ObjectType(Short.class.getName());
    static final ObjectType LONG_OBJECT = new ObjectType(Long.class.getName());
    static final ObjectType DOUBLE_OBJECT = 
        new ObjectType(Double.class.getName());
    static final ObjectType FLOAT_OBJECT = new ObjectType(Float.class.getName());
    static final ObjectType METHOD_OBJECT =
        new ObjectType(java.lang.reflect.Method.class.getName());
    static final ObjectType NUMBER_OBJECT = 
        new ObjectType(Number.class.getName());
    static final String CONSTRUCTOR_NAME = "<init>";
    static final String FIELD_NAME = "h";
    static final String SOURCE_FILE = "<generated>";
    static final String CLASS_SUFIX = "$ProxyPlus";
    // XXX should I keep this?
    static final String CLASS_PREFIX = "org.apache.";
    /**
     * Call the super() constructor (this can be turned off if the
     * -noverify option is given to the JVM)
     **/
    static boolean callSuper = true;
    static int index = 0;

    /** 
     * Just a little syntactic sugar, so that I don't have to
     * reference everything by Constants.ACC_PUBLIC, I can instead
     * just say c.ACC_PUBLIC
     **/
    private static final TerseConstants c = new TerseConstants();
    private static java.util.List costructionHandlers = new java.util.Vector();
    private static java.util.Map cache = new java.util.WeakHashMap();
    

    private static int addNewInstanceRef(ConstantPoolGen cp,String name) {
        return cp.addMethodref(
                               name,
                               "<init>",
                               "(L"+ INVOCATION_CLASS.replace('.','/') +";)V");
    }
   
    private static int addInvokeRef(ConstantPoolGen cp) {
        // http://jakarta.apache.org/bcel/manual.html
        // should use the Type.getMethodSignature() here
        return cp.addInterfaceMethodref(INVOCATION_CLASS,
               "invoke",
               // proxy, method, args[]
        // this "better" way doesn't seem to work                                       
//                 Type.getMethodSignature(Type.OBJECT,
//                                         new Type[] {
//                                         Type.OBJECT,
//                                         new ObjectType(java.lang.reflect.Method.class.getName()),
//                                         new ObjectType(new Object[0].getClass().getName())}));
                                        
     "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
      }

    protected ProxyPlus(InvocationHandler h) {
        super(h);
    }

    // XXX should I really allow this?  maybe the Field should be final ?
//      public static void setInvocationHandler(Object enhanced, 
//                                              InvocationHandler ih) {
//          enhanced.getClass().getField(FIELD_NAME).set(enhanced, ih);
//      }

    public static InvocationHandler getInvocationHandler(Object proxy){
        Class cl = proxy.getClass();
        if (!isProxyPlusClass(cl)) {
            throw new IllegalArgumentException("not a proxy plus instance");
        }
        if (isProxyClass(cl)) {
            return Proxy.getInvocationHandler(proxy);
        }
        try{      
            return (InvocationHandler) cl.getField(FIELD_NAME).get(proxy);
        }catch(NoSuchFieldException nsfe){
            throw new NoSuchFieldError(cl.getName() + ":" + nsfe.getMessage());
        }catch(java.lang.IllegalAccessException iae ){
            throw new IllegalAccessError(cl.getName() + ":" + iae.getMessage()); 
        }
    }

    // I thought it poor to change this behavior after all
//      public static boolean isProxyClass(Class cl) {
//          if (Proxy.isProxyClass(cl)) {
//              return true;
//          } else {
//              return isProxyPlusClass(cl);
//          }
//      }

    /**
     * @return true if it's a proxy Plus class, false otherwise
     **/
    public static boolean isProxyPlusClass(Class cl) {
        Map map = (Map) cache.get(cl.getClassLoader());
        return map.containsValue(cl);
    }

    
    public static Class getProxyClass(Class[] interfaces,
                                      Class superclass) {
        return getProxyClass(Thread.currentThread().getContextClassLoader(),
                             interfaces,
                             superclass);
    }

    public static synchronized Class getProxyClass(ClassLoader loader,
                                                   Class[] interfaces,
                                                   Class superclass) {
        Class cls = superclass;
        if (loader == null) {
            throw new NullPointerException("loader must not be null " + 
           "(objects.getClassLoader() will return null, if loaded by " + 
           "the bootstrap class loader)");
        }
        if (cls == null || interfaces == null)
            throw new NullPointerException();
        
        if (callSuper) {
            // check to ensure that there is a default constructor
            try {
                superclass.getDeclaredConstructor(new Class[0]);
            } catch (NoSuchMethodException nsme) {
                throw new IllegalArgumentException(superclass.getName() +
                                                   " has no default constructor");
            }
        }
        
        StringBuffer keyBuff = new StringBuffer(cls.getName() + ";");
        if(interfaces != null){
            for(int i = 0; i< interfaces.length; i++ ){
                keyBuff.append(interfaces[i].getName() + ";");
            }
        }
        String key = keyBuff.toString(); 
        
        java.util.Map map = (java.util.Map) cache.get(loader);
        if ( map == null ) {
            map = new java.util.Hashtable();
            cache.put(loader, map);
        }
        Class result = (Class) map.get(key);
        if (result == null) {
            String class_name = cls.getName() + CLASS_SUFIX;
            if (class_name.startsWith("java")) {
                class_name = CLASS_PREFIX + class_name;
            }
            class_name += index++;
            java.util.HashMap methods = new java.util.HashMap();
            JavaClass clazz = enhance(cls, class_name, interfaces, methods);
            byte b[] = clazz.getBytes();
            java.lang.reflect.Method m = null;
            try {
                m = ClassLoader.class.getDeclaredMethod("defineClass",
                new Class[] { String.class, byte[].class, int.class, int.class });
            } catch (NoSuchMethodException nsme) {
                throw new NoSuchMethodError("ProxyPlus internal error: " + nsme);
            }
            boolean flag = m.isAccessible();
            try {
                // protected method invocaton
                m.setAccessible(true);
                result = (Class) m.invoke(loader,
                                          new Object[] { clazz.getClassName(), b, 
                                                         new Integer(0), 
                                                         new Integer(b.length)});
            } catch (IllegalAccessException iae) {
                throw new IllegalAccessError("ProxyPlus internal error: " 
                                             + iae.getMessage());
            } catch (InvocationTargetException ite) {
                throw new InternalError("ProxyPlus internal error: " 
                                        + ite.getTargetException());
            } finally {
                m.setAccessible(flag);
            }
            for (Iterator i = methods.keySet().iterator(); i.hasNext();){
                String name = (String) i.next();
                // XXX don't tell me the methods are set up *after* 
                // creation?
                try {
                    result.getField(name).set(null, methods.get(name));
                }catch(NoSuchFieldException nsfe){
                    throw new NoSuchFieldError(result.getName() + ":" 
                                               + nsfe.getMessage());
                }catch(java.lang.IllegalAccessException iae ){
                    throw new IllegalAccessError(result.getName() + ":" 
                                                 + iae.getMessage()); 
                }
            }
            map.put(key, result);
        }
        return result;
    }
    
    public static Object newProxyInstance(Class interfaces[],
                                          Class superclass,
                                          InvocationHandler ih)
        throws NoSuchMethodException, 
               IllegalAccessException, 
               InstantiationException, 
               ClassNotFoundException,
               InvocationTargetException {
        return newProxyInstance(
                       Thread.currentThread().getContextClassLoader(),
                       interfaces,
                       superclass,
                       ih);

    }

    public static Object newProxyInstance(ClassLoader loader,
                                          Class interfaces[],
                                          Class superclass,
                                          InvocationHandler ih)
        throws NoSuchMethodException, 
               IllegalAccessException, 
               InstantiationException, 
               ClassNotFoundException,
               InvocationTargetException {
        Class cl = getProxyClass(loader, interfaces, superclass);
        try {
            return cl.getConstructor(new Class[] { 
                Class.forName(InvocationHandler.class.getName(), 
                              true, 
                              loader)}).newInstance(new Object[] { ih });
            /* These were the catches for the proxy code, but
               since we're doing objects, they can't be considered
               internal errors any longer */
        } catch (ClassNotFoundException e) {
            // this actually means something different than CNFE,
            // it means the loader can't see that interface
            throw e;
        } catch (NoSuchMethodException e) {
            throw e;
            //throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw e;
            //throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw e;
            //throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw e;
            //throw new InternalError(e.toString());
        }
    }

    private static void addConstructor(ClassGen cg) {
        
        String parentClass = cg.getSuperclassName();
        InstructionFactory factory = new InstructionFactory(cg);
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        InstructionList il = new InstructionList();
        MethodGen costructor = new MethodGen(c.ACC_PUBLIC, // access flags
                                             Type.VOID, // return type
                                             new Type[] { // argument types
            new ObjectType(INVOCATION_CLASS)}, null, CONSTRUCTOR_NAME, 
            cg.getClassName(), il, cp);
        
        if (callSuper) {
        // <super>
        // this piece of code can actually be excluded... if your super class
        // doesn't include a default constructor, this may be necessary.  
        // Unfortunately it requires that verification be turned off (-noverify).
            il.append(new ALOAD(0));
            il.append(factory.createInvoke(//"java.lang.Object",
                                           parentClass,
                                           CONSTRUCTOR_NAME,
                                           Type.VOID,
                                           new Type[] {},
                                           c.INVOKESPECIAL));
        // </super>
        }
        il.append(new ALOAD(0));
        il.append(new ALOAD(1));
        il.append(factory.createFieldAccess(
                                            cg.getClassName(),
                                            FIELD_NAME,
                                            new ObjectType(INVOCATION_CLASS),
                                            c.PUTFIELD));

        il.append(new RETURN());
        cg.addMethod(getMethod(costructor));
    }
        
    private static void addHandlerField(ClassGen cg) {
        ConstantPoolGen cp = cg.getConstantPool();
        FieldGen fg = new FieldGen(c.ACC_PUBLIC, new ObjectType(INVOCATION_CLASS), 
                                   FIELD_NAME, cp);
        cg.addField(fg.getField());
    }
    
    private static ClassGen getClassGen(String class_name,
                                        Class parentClass,
                                        Class[] interfaces) {
        ClassGen gen = new ClassGen(class_name, parentClass.getName(), 
                                    SOURCE_FILE, c.ACC_PUBLIC, null);
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                gen.addInterface(interfaces[i].getName());
            }
        }
        return gen;
    }
    
    private static JavaClass enhance(Class parentClass,
                                    String class_name,
                                    Class interfaces[],
                                    java.util.HashMap methodTable) {
        ClassGen cg = getClassGen(class_name, parentClass, interfaces);
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        addHandlerField(cg);
        addConstructor(cg);
        int after = addInvokeRef(cp);
        java.util.Set methodSet = new java.util.HashSet();
        
        for (int j = 0;  j <= interfaces.length; j++ ) {
            java.lang.reflect.Method methods[];
            if (j == 0) {
                methods = parentClass.getMethods();
            } else {
                methods = interfaces[j - 1].getMethods();
            }
            for (int i = 0; i < methods.length; i++) {
                int mod = methods[i].getModifiers();
                if (!java.lang.reflect.Modifier.isStatic(mod)
                    && !java.lang.reflect.Modifier.isFinal(mod)
                    && (java.lang.reflect.Modifier.isPublic(mod)
                        || java.lang.reflect.Modifier.isProtected(mod))) {
                    
                    methodSet.add(new MethodWrapper(methods[i]));
                  
                }
            }
        }
        int cntr = 0;
        for (java.util.Iterator i = methodSet.iterator(); i.hasNext();) {
            // XXX need to get rid of these, they should be simply embeded 
            // in the generated code
            java.lang.reflect.Method method = ((MethodWrapper) i.next()).method;
            String fieldName = "METHOD_" + (cntr++);
            cg.addMethod(generateMethod(method, fieldName, cg,  after));
            methodTable.put(fieldName, method);
        }
        JavaClass jcl = cg.getJavaClass();
        return jcl;
    }
        
    private static void addMethodField(String fieldName, ClassGen cg) {
        ConstantPoolGen cp = cg.getConstantPool();
        FieldGen fg =
            new FieldGen(c.ACC_PUBLIC | c.ACC_STATIC, METHOD_OBJECT, 
                         fieldName, cp);
        cg.addField(fg.getField());
    }
    
    private static int createArgArray(InstructionList il,
                                      InstructionFactory factory,
                                      ConstantPoolGen cp,
                                      Type[] args) {
            
        int argCount = args.length;
        if (argCount > 5)
            il.append(new BIPUSH((byte) argCount));
        else
            il.append(new ICONST((byte) argCount));
        il.append(new ANEWARRAY(cp.addClass(Type.OBJECT)));
        int load = 1;
        for (int i = 0; i < argCount; i++) {
            il.append(new DUP());
            if (i > 5)
                il.append(new BIPUSH((byte) i));
            else
                il.append(new ICONST((byte) i));
            if (args[i] instanceof BasicType) {
                if (args[i].equals(Type.BOOLEAN)) {
                    il.append(new NEW(cp.addClass(BOOLEAN_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(new INVOKESPECIAL(
                                                cp.addMethodref(Boolean.class.getName(), CONSTRUCTOR_NAME, "(Z)V")));
                } else if (args[i].equals(Type.INT)) {
                    il.append(new NEW(cp.addClass(INTEGER_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Integer.class.getName(), CONSTRUCTOR_NAME, "(I)V")));
                } else if (args[i].equals(Type.CHAR)) {
                    il.append(new NEW(cp.addClass(CHARACTER_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Character.class.getName(), CONSTRUCTOR_NAME, "(C)V")));
                } else if (args[i].equals(Type.BYTE)) {
                    il.append(new NEW(cp.addClass(BYTE_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Byte.class.getName(), CONSTRUCTOR_NAME, "(B)V")));
                } else if (args[i].equals(Type.SHORT)) {
                    il.append(new NEW(cp.addClass(SHORT_OBJECT)));
                    il.append(new DUP());
                    il.append(new ILOAD(load++));
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Short.class.getName(), CONSTRUCTOR_NAME, "(S)V")));
                } else if (args[i].equals(Type.LONG)) {
                    il.append(new NEW(cp.addClass(LONG_OBJECT)));
                    il.append(new DUP());
                    il.append(new LLOAD(load));
                    load += 2;
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Long.class.getName(), CONSTRUCTOR_NAME, "(J)V")));
                } else if (args[i].equals(Type.DOUBLE)) {
                    il.append(new NEW(cp.addClass(DOUBLE_OBJECT)));
                    il.append(new DUP());
                    il.append(new DLOAD(load));
                    load += 2;
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Double.class.getName(), CONSTRUCTOR_NAME, "(D)V")));
                } else if (args[i].equals(Type.FLOAT)) {
                    il.append(new NEW(cp.addClass(FLOAT_OBJECT)));
                    il.append(new DUP());
                    il.append(new FLOAD(load++));
                    il.append(
                              new INVOKESPECIAL(
                                                cp.addMethodref(Float.class.getName(), CONSTRUCTOR_NAME, "(F)V")));
                }
                il.append(new AASTORE());
            } else {
                il.append(new ALOAD(load++));
                il.append(new AASTORE());
            }
        }
        return load;
    }
    
    private static Method getMethod(MethodGen mg) {
        mg.stripAttributes(true);
        mg.setMaxLocals();
        mg.setMaxStack();
        return mg.getMethod();
    }
    
    private static InstructionHandle generateReturnValue(InstructionList il,
                                                       InstructionFactory factory,
                                                         ConstantPoolGen cp,
                                                         Type returnType,
                                                         int stack) {
        if (returnType.equals(Type.VOID)) {
            return il.append(new RETURN());
        }
        il.append(new ASTORE(stack));
        il.append(new ALOAD(stack));
        if ((returnType instanceof ObjectType) 
            || ( returnType instanceof ArrayType) ) {
            if (returnType instanceof ArrayType){
                il.append(new CHECKCAST(cp.addArrayClass((ArrayType)returnType)));
                return il.append(new ARETURN());
            }
            if (!returnType.equals(Type.OBJECT)){
                il.append(new CHECKCAST(cp.addClass((ObjectType) returnType)));
                return il.append(new ARETURN());
            }else {
                return il.append(new ARETURN());
            }
            
        }
        if (returnType instanceof BasicType) {
            if (returnType.equals(Type.BOOLEAN)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(BOOLEAN_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               Boolean.class.getName(),
                                               "booleanValue",
                                               Type.BOOLEAN,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new IRETURN());
            } else if (returnType.equals(Type.CHAR)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(CHARACTER_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               Character.class.getName(),
                                               "charValue",
                                               Type.CHAR,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new IRETURN());
            } else if (returnType.equals(Type.LONG)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new LCONST(0) );
                il.append(new LRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               Number.class.getName(),
                                               "longValue",
                                               Type.LONG,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new LRETURN());
            } else if (returnType.equals(Type.DOUBLE)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new DCONST(0) );
                il.append(new DRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               Number.class.getName(),
                                               "doubleValue",
                                               Type.DOUBLE,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new DRETURN());
            } else if (returnType.equals(Type.FLOAT)) {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new FCONST(0) );
                il.append(new FRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               java.lang.Number.class.getName(),
                                               "floatValue",
                                               Type.FLOAT,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new FRETURN());
            } else {
                IFNONNULL ifNNull = new IFNONNULL(null);
                il.append(ifNNull);
                il.append(new ICONST(0) );
                il.append(new IRETURN());
                ifNNull.setTarget(il.append(new ALOAD(stack)));
                il.append(new CHECKCAST(cp.addClass(NUMBER_OBJECT)));
                il.append(
                          factory.createInvoke(
                                               Number.class.getName(),
                                               "intValue",
                                               Type.INT,
                                               new Type[] {},
                                               c.INVOKEVIRTUAL));
                return il.append(new IRETURN());
            }
        }
        throw new java.lang.InternalError();
    }
    
    
    private static Instruction newWrapper(Type type, ConstantPoolGen cp) {
        
        if (type instanceof BasicType) {
            if (type.equals(Type.BOOLEAN)) {
                return new NEW(cp.addClass(BOOLEAN_OBJECT));
            } else if (type.equals(Type.INT)) {
                return new NEW(cp.addClass(INTEGER_OBJECT));
            } else if (type.equals(Type.CHAR)) {
                return new NEW(cp.addClass(CHARACTER_OBJECT));
            } else if (type.equals(Type.BYTE)) {
                return new NEW(cp.addClass(BYTE_OBJECT));
            } else if (type.equals(Type.SHORT)) {
                return new NEW(cp.addClass(SHORT_OBJECT));
            } else if (type.equals(Type.LONG)) {
                return new NEW(cp.addClass(LONG_OBJECT));
            } else if (type.equals(Type.DOUBLE)) {
                return new NEW(cp.addClass(DOUBLE_OBJECT));
            } else if (type.equals(Type.FLOAT)) {
                return new NEW(cp.addClass(FLOAT_OBJECT));
            }
        }
        return null;
    }
    
    private static Instruction initWrapper(Type type, ConstantPoolGen cp) {
        
        if (type instanceof BasicType) {
            if (type.equals(Type.BOOLEAN)) {
                return new INVOKESPECIAL(cp.addMethodref(Boolean.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(Z)V"));
            } else if (type.equals(Type.INT)) {
                return new INVOKESPECIAL(cp.addMethodref(Integer.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(I)V"));
            } else if (type.equals(Type.CHAR)) {
               return new INVOKESPECIAL(cp.addMethodref(Character.class.getName(),
                                                 CONSTRUCTOR_NAME, "(C)V"));
            } else if (type.equals(Type.BYTE)) {
                return new INVOKESPECIAL(cp.addMethodref(Byte.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(B)V"));
            } else if (type.equals(Type.SHORT)) {
                return new INVOKESPECIAL(cp.addMethodref(Short.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(S)V"));
            } else if (type.equals(Type.LONG)) {
                return new INVOKESPECIAL(cp.addMethodref(Long.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(J)V"));
            } else if (type.equals(Type.DOUBLE)) {
                return new INVOKESPECIAL(cp.addMethodref(Double.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(D)V"));
            } else if (type.equals(Type.FLOAT)) {
                return new INVOKESPECIAL(cp.addMethodref(Float.class.getName(), 
                                                 CONSTRUCTOR_NAME, "(F)V"));
            }
        }
        return null;
    }
    
    private static int loadArg(InstructionList il, Type t, int index, int pos) {
        
        if (t instanceof BasicType) {
            if (t.equals(Type.LONG)) {
                il.append(new LLOAD(pos));
                pos += 2;
                return pos;
            } else if (t.equals(Type.DOUBLE)) {
                il.append(new DLOAD(pos));
                pos += 2;
                return pos;
            } else if (t.equals(Type.FLOAT)) {
                il.append(new FLOAD(pos));
                return ++pos;
            } else {
                il.append(new ILOAD(pos));
                return ++pos;
            }
        } else {
            il.append(new ALOAD(pos));
            return ++pos;
        }
    }
    
    private static Type[] toType(Class cls[]) {
        
        Type tp[] = new Type[cls.length];
        for (int i = 0; i < cls.length; i++) {
            tp[i] = toType(cls[i]);
        }
        return tp;
    }
    
    private static Type toType(Class cls) {
        
        if (cls.equals(void.class)) {
            return Type.VOID;
        }
        if (cls.isPrimitive()) {
            if (int.class.equals(cls)) {
                return Type.INT;
            } else if (char.class.equals(cls)) {
                return Type.CHAR;
            } else if (short.class.equals(cls)) {
                return Type.SHORT;
            } else if (byte.class.equals(cls)) {
                return Type.BYTE;
            } else if (long.class.equals(cls)) {
                return Type.LONG;
            } else if (float.class.equals(cls)) {
                return Type.FLOAT;
            } else if (double.class.equals(cls)) {
                return Type.DOUBLE;
            } else if (boolean.class.equals(cls)) {
                return Type.BOOLEAN;
            }
        } else if (cls.isArray()) {
            return new ArrayType( toType(cls.getComponentType()),
                                  cls.getName().lastIndexOf('[') + 1);
        } else
            return new ObjectType(cls.getName());
        throw new java.lang.InternalError(cls.getName());
    }
    
    private static void invokeSuper(ClassGen cg, MethodGen mg, Type args[]) {
        
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionList il = mg.getInstructionList();
        int pos = 1;
        il.append(new ALOAD(0)); //this
        for (int i = 0; i < args.length; i++) { //load args to stack
            pos = loadArg(il, args[i], i, pos);
        }
        il.append(
                  new INVOKESPECIAL(
                                    cp.addMethodref(cg.getSuperclassName(), 
                                                    mg.getName(), 
                                                    mg.getSignature())));
    }
    
    private static MethodGen toMethodGen(java.lang.reflect.Method mtd,
                                         String className,
                                         InstructionList il,
                                         ConstantPoolGen cp) {
            
        return new MethodGen(c.ACC_PUBLIC,
                             toType(mtd.getReturnType()),
                             toType(mtd.getParameterTypes()),
                             null,
                             mtd.getName(),
                             className,
                             il,
                             cp);
    }
    
    private static MethodGen toMethodGen(java.lang.reflect.Constructor mtd,
                                         String className,
                                         InstructionList il,
                                         ConstantPoolGen cp) {
            
        return new MethodGen(c.ACC_PUBLIC,
                             Type.VOID,
                             toType(mtd.getParameterTypes()),
                             null,
                             CONSTRUCTOR_NAME,
                             className,
                             il,
                             cp);
    }
    
    private static Method generateMethod(java.lang.reflect.Method method,
                                         String fieldName,
                                         ClassGen cg,
                                         int after
                                         ) {
        
        InstructionList il = new InstructionList();
        InstructionFactory factory = new InstructionFactory(cg);
        ConstantPoolGen cp = cg.getConstantPool();
        MethodGen mg = toMethodGen(method, cg.getClassName(), il, cp);
        
        Type types[] = mg.getArgumentTypes();
        int argCount = types.length;
        addMethodField(fieldName, cg);
        //GENERATE ARG ARRAY
        int loaded = createArgArray(il, factory, cp, mg.getArgumentTypes());
        int argArray = loaded;
        il.append(new ASTORE(argArray));

        // ADD METHOD LOCAL VARIABLE
        //          LocalVariableGen lg = mg.addLocalVariable("meth",
        //                                                    METHOD_OBJECT, null, null);
        //          int meth = lg.getIndex();
        //      il.append(
        //          lg.setStart(il.append(new ASTORE(meth))); // "meth" valid from here
        
        
        il.append(new ALOAD(0)); //this.handler
    
        il.append(factory.createFieldAccess(cg.getClassName(),
                                            FIELD_NAME,
                                            new ObjectType(INVOCATION_CLASS),
                                            c.GETFIELD));
        
        // INVOKE Handler
        il.append(new ALOAD(0)); //this
        il.append(factory.createGetStatic(cg.getClassName(), fieldName, 
                                          METHOD_OBJECT));
        il.append(new ALOAD(argArray));
        il.append(new INVOKEINTERFACE(after, 4 ));
        
        //GENERATE RETURN VALUE 
        InstructionHandle exitMethod =
            generateReturnValue(il, factory, cp, mg.getReturnType(), ++loaded);

        mg.setMaxStack();
        mg.setMaxLocals();
        Method result = getMethod(mg);
        
        return result;
    }
    
    static class MethodWrapper {
        java.lang.reflect.Method method;
        MethodWrapper(java.lang.reflect.Method method) {
            if (method == null) {
                throw new NullPointerException();
            }
            this.method = method;
        }
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof MethodWrapper)) {
                return false;
            }
            java.lang.reflect.Method m1 = this.method;
            java.lang.reflect.Method m2 = ((MethodWrapper)obj).method;
            if (m1 == m2) {
                return true;
            }
            if (m1.getName().equals(m2.getName())) {
                Class[] params1 = m1.getParameterTypes();
                Class[] params2 = m2.getParameterTypes();
                if (params1.length == params2.length) {
                    for (int i = 0; i < params1.length; i++) {
                        if (!params1[i].getName().equals(params2[i].getName())) {
                            return false;
                        }
                    }
                    if(!m1.getReturnType().getName().
                       equals(m2.getReturnType().getName()) ){
                        throw new java.lang.IllegalStateException(
                  "Can't implement:\n" + m1.getDeclaringClass().getName() +
                  "\n      and\n" + m2.getDeclaringClass().getName() + "\n"+
                  m1.toString() + "\n" + m2.toString());
                    }
                    return true;
                }
            }
            return false;
        }
        public int hashCode() {
            return method.getName().hashCode();
        }
    }

    private static class TerseConstants implements Constants {
    }
    
    private static boolean equals(java.lang.reflect.Method m1,
                                  java.lang.reflect.Method m2) {
        return new MethodWrapper(m1).equals(new MethodWrapper(m2));
    }

}
