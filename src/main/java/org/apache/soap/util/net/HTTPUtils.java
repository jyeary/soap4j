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

package org.apache.soap.util.net;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * A bunch of utility stuff for doing HTTP things.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 */
public class HTTPUtils {
  private static final String HTTP_VERSION = "1.0";
  private static final String HTTP_POST = "POST";
  private static final String HEADER_HOST = "Host";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_CONTENT_LENGTH = "Content-Length";

  /**
   * An instance of this class is returned by post.
   */
  static public class Response {
    int statusCode;
    String statusString;
    public Hashtable headers;
    public int contentLength;
    public String contentType;
    public BufferedReader content;

    Response (int statusCode, String statusString, Hashtable headers,
	      int contentLength, String contentType, BufferedReader content) {
      this.statusCode = statusCode;
      this.statusString = statusString;
      this.headers = headers;
      this.contentLength = contentLength;
      this.contentType = contentType;
      this.content = content;
    }
  }

  /** 
   * POST something to the given URL. The headers are put in as 
   * HTTP headers, the content length is calculated and the content
   * is sent as content. Duh.
   *
   * @param url the url to post to
   * @param headers additional headers to send as HTTP headers
   * @param contentType type of the content
   * @param content the body of the post
   */
  public static Response post (URL url, Hashtable headers,
			       String contentType, String content)
               throws IllegalArgumentException {
    PrintWriter out = null;
    BufferedReader in = null;
    try {
      Socket s = new Socket (url.getHost (), url.getPort ());
      out = new PrintWriter (s.getOutputStream ());
      in = new BufferedReader (new InputStreamReader (s.getInputStream ()));
    } catch (Exception e) {
      throw new IllegalArgumentException ("error opening socket: " +
					  e.getMessage ());
    }

    /* send it out */
    out.println (HTTP_POST + " " + url.getFile() + " HTTP/" + HTTP_VERSION);
    out.println (HEADER_HOST + ": " + url.getHost () + ':' + url.getPort ());
    out.println (HEADER_CONTENT_TYPE + ": " + contentType);
    out.println (HEADER_CONTENT_LENGTH + ": " + content.length ());
    for (Enumeration e = headers.keys (); e.hasMoreElements (); ) {
      Object key = e.nextElement ();
      out.println (key + ": " + headers.get (key));
    }
    out.println ();
    out.println (content);
    out.flush ();
    //    out.close ();

    /* read the status line */
    int statusCode = 0;
    String statusString = null;
    try {
      StringTokenizer st = new StringTokenizer (in.readLine ());
      st.nextToken (); // ignore version part
      statusCode = Integer.parseInt (st.nextToken ());
      StringBuffer sb = new StringBuffer ();
      while (st.hasMoreTokens ()) {
	sb.append (st.nextToken ());
	if (st.hasMoreTokens ()) {
	  sb.append (" ");
	}
      }
      statusString = sb.toString ();
    } catch (Exception e) {
      throw new IllegalArgumentException ("error parsing HTTP status line: " +
					  e.getMessage ());
    }

    /* get the headers */
    Hashtable respHeaders = new Hashtable ();
    int respContentLength = -1;
    String respContentType = null;
    try {
      String line = null;
      while ((line = in.readLine ()) != null) {
	if (line.length () == 0) {
	  break;
	}
	int colonIndex = line.indexOf (':');
	String fieldName = line.substring (0, colonIndex);
	String fieldValue = line.substring (colonIndex + 1).trim ();
	if (fieldName.equals (HEADER_CONTENT_LENGTH)) {
	  respContentLength = Integer.parseInt (fieldValue);
	} else if (fieldName.equals (HEADER_CONTENT_TYPE)) {
	  respContentType = fieldValue;
	} else {
	  respHeaders.put (fieldName, fieldValue);
	}
      }
    } catch (Exception e) {
      throw new IllegalArgumentException ("error reading HTTP headers: " +
					  e.getMessage ());
    }

      
    /* all done */
    return new Response (statusCode, statusString, respHeaders, 
			 respContentLength, respContentType, in);
  }
}