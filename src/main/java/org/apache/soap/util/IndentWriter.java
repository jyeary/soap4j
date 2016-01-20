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

/* $Id: IndentWriter.java,v 4b031f0e6aa5 2016/01/20 14:23:03 jyeary $ */

package org.apache.soap.util;

import java.io.*;

/**
 * An <code>IndentWriter</code> object behaves the same as a
 * <code>PrintWriter</code> object, with the additional capability
 * of being able to print strings that are prepended with a specified
 * amount of spaces.
 *
 * @version  $Revision: 4b031f0e6aa5 $
 * @author Matthew J. Duftler
 */
public class IndentWriter extends PrintWriter
{
  /**
   * Forwards its arguments to the <code>PrintWriter</code> constructor
   * with the same signature.
   */
  public IndentWriter(OutputStream out)
  {
    super(out);
  }

  /**
   * Forwards its arguments to the <code>PrintWriter</code> constructor
   * with the same signature.
   */
  public IndentWriter(OutputStream out, boolean autoFlush)
  {
    super(out, autoFlush);
  }

  /**
   * Forwards its arguments to the <code>PrintWriter</code> constructor
   * with the same signature.
   */
  public IndentWriter(Writer out)
  {
    super(out);
  }

  /**
   * Forwards its arguments to the <code>PrintWriter</code> constructor
   * with the same signature.
   */
  public IndentWriter(Writer out, boolean autoFlush)
  {
    super(out, autoFlush);
  }

  /**
   * Print the text (indented the specified amount) without inserting a linefeed.
   *
   * @param numberOfSpaces the number of spaces to indent the text.
   * @param text the text to print.
   */
  public void print(int numberOfSpaces, String text)
  {
    super.print(StringUtils.getChars(numberOfSpaces, ' ') + text);
  }

  /**
   * Print the text (indented the specified amount) and insert a linefeed.
   *
   * @param numberOfSpaces the number of spaces to indent the text.
   * @param text the text to print.
   */
  public void println(int numberOfSpaces, String text)
  {
    super.println(StringUtils.getChars(numberOfSpaces, ' ') + text);
  }
}