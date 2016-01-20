/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
 * 4. The names "SOAP" and "Apache Software Foundation" must
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2000, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/* $Id: JavaUtils.java,v 4b031f0e6aa5 2016/01/20 14:23:03 jyeary $ */

package org.apache.soap.util;

import java.io.IOException;

public class JavaUtils
{
  // Debug flag - generates debug stuff if true.
  private static boolean debug = false;

  // Temporarily copied from JavaEngine...
  private static boolean cantLoadCompiler=false; // One-time flag for following

  // ADDED BY JKESS; callers want control over the -g option.
  public static void setDebug(boolean newDebug)
  {
    debug=newDebug;
  }

  public static boolean JDKcompile(String fileName, String classPath)
  {
    if (debug)
    {
      System.err.println ("JavaEngine: Compiling " + fileName);
      System.err.println ("JavaEngine: Classpath is " + classPath);
    }
    
    String option = debug ? "-g" : "-O";

    if(!cantLoadCompiler)
      {
  String args[] = {
    option,
    "-classpath",
    classPath,
    fileName
  };
  try
    {
      return new sun.tools.javac.Main(System.err, "javac").compile(args);
    }
  catch (Throwable th)
    {
      System.err.println("WARNING: Unable to load Java 1.1 compiler.");
      System.err.println("\tSwitching to command-line invocation.");
      cantLoadCompiler=true;
    }
      }
    
    // Can't load javac; try exec'ing it.
    String args[] = {
      "javac",
      option,
      "-classpath",
      classPath,
      fileName
    };
    try
      {
  Process p=java.lang.Runtime.getRuntime().exec(args);
  p.waitFor();
  return(p.exitValue()!=0);
      }
    catch(IOException e)
      {
  System.err.println("ERROR: IO exception during exec(javac).");
      }
    catch(SecurityException e)
      {
  System.err.println("ERROR: Unable to create subprocess to exec(javac).");
      }
    catch(InterruptedException e)
      {
  System.err.println("ERROR: Wait for exec(javac) was interrupted.");
      }
    return false;
  }
}
