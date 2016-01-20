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
import org.w3c.dom.*;

/**
 * A <code>XPathUtils</code> ...
 *
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class XPathUtils
{
  private static Node getPreviousTypedNode(Node node, short nodeType)
  {
    node = node.getPreviousSibling();

    while (node != null && node.getNodeType() != nodeType)
    {
      node = node.getPreviousSibling();
    }

    return node;
  }

  private static Node getNextTypedNode(Node node, short nodeType)
  {
    node = node.getNextSibling();

    while (node != null && node.getNodeType() != nodeType)
    {
      node = node.getNextSibling();
    }

    return node;
  }

  private static String getValue(Node node, short nodeType)
  {
    switch (nodeType)
    {
      case Node.ELEMENT_NODE :
        return ((Element)node).getTagName();

      case Node.TEXT_NODE :
        return ((Text)node).getData();

      case Node.PROCESSING_INSTRUCTION_NODE :
        return ((ProcessingInstruction)node).getData();

      default :
        return "";
    }
  }

  private static short getNodeType(Node node)
  {
    return (node != null ? node.getNodeType() : -1);
  }

  private static String getXPathFromVector(Vector path)
  {
    StringBuffer strBuf = new StringBuffer();
    int          length = path.size();

    for (int i = 0; i < length; i++)
    {
      Node   tempNode    = (Node)path.elementAt(i);
      short  nodeType    = getNodeType(tempNode);
      String targetValue = getValue(tempNode, nodeType);
      int    position    = 1;

      tempNode = getPreviousTypedNode(tempNode, nodeType);

      while (tempNode != null)
      {
        if (nodeType == Node.ELEMENT_NODE)
        {
          if (getValue(tempNode, nodeType).equals(targetValue))
          {
            position++;
          }
        }
        else
        {
          position++;
        }

        tempNode = getPreviousTypedNode(tempNode, nodeType);
      }

      boolean hasMatchingSiblings = (position > 1);

      if (!hasMatchingSiblings)
      {
        tempNode = (Node)path.elementAt(i);
        tempNode = getNextTypedNode(tempNode, nodeType);

        while (!hasMatchingSiblings && tempNode != null)
        {
          if (nodeType == Node.ELEMENT_NODE)
          {
            if (getValue(tempNode, nodeType).equals(targetValue))
            {
              hasMatchingSiblings = true;
            }
            else
            {
              tempNode = getNextTypedNode(tempNode, nodeType);
            }
          }
          else
          {
            hasMatchingSiblings = true;
          }
        }
      }

      String step;

      switch (nodeType)
      {
        case Node.TEXT_NODE :
          step = "text()";
          break;
        case Node.PROCESSING_INSTRUCTION_NODE :
          step = "processing-instruction()";
          break;
        default :
          step = targetValue;
          break;
      }

      if (step != null && step.length() > 0)
      {
        strBuf.append('/' + step);
      }

      if (hasMatchingSiblings)
      {
        strBuf.append("[" + position + "]");
      }
    }

    return strBuf.toString();
  }

  private static Vector getVectorPathFromNode(Node node)
  {
    Vector path = new Vector();

    while (node != null)
    {
      path.insertElementAt(node, 0);
      node = node.getParentNode();
    }

    return path;
  }

  /**
   * Generates an XPath expression that will return only the given node as its
   * result. This method only works for element, text, document and PI nodes.
   *
   * @param node the node to generate an XPath expression for. This node must
   * be an element node, a text node, a document node, or a processing
   * instruction node.
   * @return an XPath expression that will return only the given node as its
   * result.
   * @exception IllegalArgumentException if the given node is not an element,
   * text, document or PI node.
   */
  public static String getXPathExprFromNode(Node node)
                                                throws IllegalArgumentException
  {
    short nodeType = getNodeType(node);

    switch (nodeType)
    {
      case Node.ELEMENT_NODE :
      case Node.TEXT_NODE :
      case Node.PROCESSING_INSTRUCTION_NODE :
        return getXPathFromVector(getVectorPathFromNode(node));

      case Node.DOCUMENT_NODE :
        return "/";

      default :
        throw new IllegalArgumentException("Only works for element, text, " +
                                           "document, and PI nodes.");
    }
  }
}