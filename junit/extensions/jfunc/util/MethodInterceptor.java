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

//package org.apache.commons.simplestore.tools;
package junit.extensions.jfunc.util;

/**
* Callback interface for code generated by Enhancer
*
* Decompiled code for java.util.Vector:
*<pre>
*
*<b> package </b> org.apache.java.util;
*
*<b> public class </b> Vector$$EnhancedBySimplestore$$ <b> extends </b>java.util.Vector{
*
* <b>org.apache.commons.simplestore.tools.MethodInterceptor</b> h;
* //...
* <b>static java.lang.reflect.Method </b> METHOD_23 =
*                Vector.class.getMethod(<span style='color:red'>"removeElement"</span>,
*                                           new Class[]{Object.class}
*                                   ); 
* //...
*
* <b>public boolean</b> removeElement(Object arg1){
*
*   Object args[] = { arg1 };
*   Object retValFromBefore = h.<b>beforeInvoke</b>(this,METHOD_23,args);
*   boolean invokedSuper = false;
*   Throwable t = null;
*   Object retValFromSuper = null;
*
*   if( h.<b>invokeSuper</b>(this,METHOD_23,args,retValFromBefore) ){
*     invokedSuper = true;
*   try{
*
*      retValFromSuper = new Boolean( <b>super</b>.removeElement(arg1) );
*
*    }catch(Throwable tl){
*        t = tl
*    }
*
*   }
*
*  return ((Boolean) h.<b>afterReturn</b>(this, METHOD_23, args,
*                       retValFromBefore, invokedSuper, retValFromSuper,t )
*            ).booleanValue();
*
*}
*</pre>


 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: MethodInterceptor.java,v 1.1 2002/06/27 00:54:52 semios Exp $
 */
public interface MethodInterceptor {
    
    /** Generated code calls this method first
     * @param obj this
     * @param method Intercepted method
     * @param args Arg array
     * @throws Throwable  any exeption to stop execution
     * @return returned value used as parameter for all
     * interceptor methods
     */    
  /*  public Object beforeInvoke( Object obj,
                                java.lang.reflect.Method method,
                                Object args[] )throws java.lang.Throwable;
   */
    
    /** Generated code calls this method before invoking super
     * @param obj this
     * @param method Method
     * @param args Arg array
     * @param retValFromBefore value returned from beforeInvoke
     * @throws Throwable any exeption to stop execution
     * @return true if need to invoke super
     */    
    public boolean invokeSuper( Object obj,
                                java.lang.reflect.Method method,
                                Object args[]
                                /*,Object retValFromBefore*/ )
                                             throws java.lang.Throwable;    
    
    /** this method is invoked after execution
     * @param obj this
     * @param method Method
     * @param args Arg array
     * @param retValFromBefore value returned from beforeInvoke
     * @param invokedSuper value returned from invoke super
     * @param retValFromSuper value returner from super
     * @param e Exception thrown by super
     * @throws Throwable any exeption
     * @return value to return from generated method
     */    
    public Object afterReturn(  Object obj, 
                                java.lang.reflect.Method method,
                                Object args[],
                                /*Object retValFromBefore,*/
                                boolean invokedSuper,
                                Object retValFromSuper,
                                java.lang.Throwable e )
                                             throws java.lang.Throwable;
    
    
}