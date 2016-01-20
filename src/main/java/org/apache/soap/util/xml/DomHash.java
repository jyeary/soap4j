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

package org.apache.soap.util.xml;

import java.util.Vector;
import java.io.*;
import org.w3c.dom.*;
import java.util.zip.*;


public class DomHash
{

  public static CRC32 crc = new CRC32();
  
  //
  // Hash computation code
  //
  public static long getElementHash(Element element)
  {
    Attr atts[]=null;    
    long hash = 0;

    try 
      {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream dos = new DataOutputStream(baos);
	dos.writeInt(Node.ELEMENT_NODE); // Node type
	dos.write(element.getTagName().getBytes() ); 
	
	dos.write((byte)0);
	dos.write((byte)0);
	

	//
	// sort the attributes now
	//
	int atlen=0;
	NamedNodeMap attributes = element.getAttributes();
	if (null != attributes && (atlen = attributes.getLength())!=0 ) 
	  {    
	    Attr at;
	    atts = new Attr[atlen];
	    Vector as = new Vector(); 
	    for (int i = 0;  i < atlen;  i ++)
	      as.addElement(attributes.item(i).getNodeName());
	    sortStringVector(as);
	    for (int i = 0;  i < atlen;  i ++) 
	      {
		at = (Attr)element.getAttributeNode((String)as.elementAt(i));		 
		atts[i] = at;		
	      }
	  }

	if (null == atts || (atlen = atts.length)==0 ) 
	  { // Number of attributes
	    dos.writeInt(0);
	  } 
	else
	  {	    
	    dos.writeInt(atlen);
	    	       
	    Attr at;
	    
	    for (int i = 0;  i < atlen;  i ++) 
	      {
		at = atts[i];
		dos.writeInt((int)Node.ATTRIBUTE_NODE);
		dos.writeBytes(at.getName());
		dos.writeBytes(at.getValue()); 		
	      }
	  }

	//
	// Done with attributes - may simplify this code
	//

	dos.close();	
	crc.reset();
	crc.update(baos.toByteArray());
	hash = crc.getValue();
      }
    catch(Exception exception) 
      {	
      	exception.printStackTrace();
      }
	
    return hash; 
  }
	
  public static void updateLong(Checksum crc, long add)
  {
    crc.update((byte)((add>>56) & 0xff));
    crc.update((byte)((add>>48) & 0xff));
    crc.update((byte)((add>>40) & 0xff));
    crc.update((byte)((add>>32) & 0xff));
    crc.update((byte)((add>>24) & 0xff));
    crc.update((byte)((add>>16) & 0xff));
    crc.update((byte)((add>>8) & 0xff));	
    crc.update((byte)(add & 0xff));
  }
  private static Vector sortStringVector(Vector vector) 
  {
    String[] as = new String[vector.size()];
    vector.copyInto(as);
    heapSort(as);
    vector.removeAllElements();
    vector.ensureCapacity(as.length);
    for (int i = 0;  i < as.length;  i ++)
        vector.addElement(as[i]);
    return vector;
  }

  private static void heapSort(String[] pd) {
    int i;
    for (i = pd.length/2;  i >= 0;  i--) {  // Make heap
      fall(pd, pd.length, i);
    }
    for (i = pd.length-1;  i > 0;  i--) {
      String t = pd[0];
      pd[0] = pd[i];
      pd[i] = t;
      fall(pd, i, 0);
    }
  }
  
  private static void fall(String[] pd, int n, int i) {
    int j = 2*i+1;
    if (j < n) {                            // left exists
      if (j+1 < n) {                      // right exists too
	// j: bigger
	if (0 > pd[j].compareTo(pd[j+1]))
	  j = 2*i+2;
      } else {                            // only left
      }
      if (0 > pd[i].compareTo(pd[j])) {
	// the child is bigger
	String t = pd[i];
	pd[i] = pd[j];
	pd[j] = t;
	fall(pd, n, j);
      }
    }
  }

  public static String getXMLDisplayString(String in)
  {
    char[] work = new char[in.length()*2];
    String ltS = "&lt;";
    String gtS = "&gt;";
    boolean done = false;
    int lt = 0, gt = 0, start = 0, length = 0;
    while( !done )
      {
	lt = in.indexOf('<', start);	
	gt = in.indexOf('>', lt); 
	if( lt>=start )
	  {                    
	    in.getChars(start, lt, work, length);
	    length += lt-start;
	    ltS.getChars(0, ltS.length(), work, length);	           
	    length += ltS.length();
	  }
	else
	  break;
	if( gt>lt ) 
	  {
	    in.getChars(lt+1, gt, work, length);
	    length += gt-lt-1;
	    gtS.getChars(0, gtS.length(), work, length);
	    length += gtS.length();
	    start = gt+1;
	  }
	else
	  break;
	if( lt>in.length() || gt > in.length() ) done=true;       
      }
    return new String(work);
    
  }
  

}
