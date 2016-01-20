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

package org.apache.soap;

import org.apache.soap.util.xml.QName;

/**
 * <em>SOAP</em> constants.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 */
public class Constants
{
  // Namespace prefixes.
  public static String NS_PRE_XMLNS = "xmlns";
  public static String NS_PRE_SOAP = "SOAP";
  public static String NS_PRE_SOAP_ENV = NS_PRE_SOAP + "-ENV";
  public static String NS_PRE_SOAP_ENC = NS_PRE_SOAP + "-ENC";
  public static String NS_PRE_SCHEMA_XSI = "xsi";
  public static String NS_PRE_SCHEMA_XSD = "xsd";

  // Namespace URIs.
  public static String NS_URI_XMLNS =
    "http://www.w3.org/2000/xmlns/";
  public static String NS_URI_SOAP_ENV =
    "http://schemas.xmlsoap.org/soap/envelope/";
  public static String NS_URI_SOAP_ENC =
    "http://schemas.xmlsoap.org/soap/encoding/";
  public static String NS_URI_SCHEMA_XSI =
    "http://www.w3.org/1999/XMLSchema/instance/";
  public static String NS_URI_SCHEMA_XSD =
    "http://www.w3.org/1999/XMLSchema/";
  public static String NS_URI_IBM_SOAP = 
    "http://xml.apache.org/xml-soap";
  public static String NS_URI_IBM_DEPLOYMENT = 
    "http://xml.apache.org/xml-soap/deployment";
  public static String NS_URI_LITERAL_XML = 
    "http://xml.apache.org/xml-soap/literalxml";
  public static String NS_URI_XMI_ENC = 
    "http://www.ibm.com/namespaces/xmi";

  // HTTP header field names.
  public static String HEADER_POST = "POST";
  public static String HEADER_HOST = "Host";
  public static String HEADER_CONTENT_TYPE = "Content-Type";
  public static String HEADER_CONTENT_LENGTH = "Content-Length";
  public static String HEADER_SOAP_ACTION = "SOAPAction";

  // HTTP header field values.
  public static String HEADERVAL_CONTENT_TYPE = "text/xml";

  // Element names.
  public static String ELEM_ENVELOPE = "Envelope";
  public static String ELEM_BODY = "Body";
  public static String ELEM_HEADER = "Header";
  public static String ELEM_FAULT = "Fault";
  public static String ELEM_FAULT_CODE = "faultcode";
  public static String ELEM_FAULT_STRING = "faultstring";
  public static String ELEM_FAULT_ACTOR = "faultactor";
  public static String ELEM_DETAIL = "detail";

  // Qualified element names.
  public static QName  Q_ELEM_ENVELOPE =
    new QName(NS_URI_SOAP_ENV, ELEM_ENVELOPE);
  public static QName  Q_ELEM_HEADER =
    new QName(NS_URI_SOAP_ENV, ELEM_HEADER);
  public static QName  Q_ELEM_BODY =
    new QName(NS_URI_SOAP_ENV, ELEM_BODY);
  public static QName  Q_ELEM_FAULT =
    new QName(NS_URI_SOAP_ENV, ELEM_FAULT);

  // Attribute names.
  public static String ATTR_ENCODING_STYLE = "encodingStyle";
  public static String ATTR_MUST_UNDERSTAND = "mustUnderstand";
  public static String ATTR_TYPE = "type";
  public static String ATTR_NULL = "null";
  public static String ATTR_ARRAY_TYPE = "arrayType";

  // Qualified attribute names.
  public static QName  Q_ATTR_MUST_UNDERSTAND =
    new QName(NS_URI_SOAP_ENV, ATTR_MUST_UNDERSTAND);

  // Attribute values.
  public static String ATTRVAL_TRUE = "true";

  // SOAP defined fault codes.
  public static String FAULT_CODE_VERSION_MISMATCH =
    NS_PRE_SOAP_ENV + ":VersionMismatch";
  public static String FAULT_CODE_MUST_UNDERSTAND =
    NS_PRE_SOAP_ENV + ":MustUnderstand";
  public static String FAULT_CODE_CLIENT = NS_PRE_SOAP_ENV + ":Client";
  public static String FAULT_CODE_SERVER = NS_PRE_SOAP_ENV + ":Server";
  public static String FAULT_CODE_PROTOCOL = NS_PRE_SOAP_ENV + ":Protocol";

  // Error messages.
  public static String ERR_MSG_VERSION_MISMATCH = FAULT_CODE_VERSION_MISMATCH +
                                                  ": Envelope element must " +
                                                  "be associated with " +
                                                  "the '" +
                                                  Constants.NS_URI_SOAP_ENV +
                                                  "' namespace.";
}
