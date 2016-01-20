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

package org.apache.soap.encoding;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.soap.util.*;
import org.apache.soap.util.xml.*;
import org.apache.soap.*;
import org.apache.soap.rpc.*;
import org.apache.soap.encoding.literalxml.*;
import org.apache.soap.encoding.soapenc.*;
import org.apache.soap.encoding.xmi.*;

/**
 * A <code>SOAPMappingRegistry</code> object is an
 * <code>XMLJavaMappingRegistry</code> with pre-registered
 * serializers and deserializers to support <em>SOAP</em>.
 *
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 * @author Francisco Curbera (curbera@us.ibm.com)
 * @author Sam Ruby (rubys@us.ibm.com)
 */
public class SOAPMappingRegistry extends XMLJavaMappingRegistry
{
  private static final String soapEncURI = Constants.NS_URI_SOAP_ENC;
  private static final String schemaURI = Constants.NS_URI_SCHEMA_XSD;
  private static final QName stringQName = new QName(schemaURI, "string");
  private static final QName booleanQName = new QName(schemaURI, "boolean");
  private static final QName doubleQName = new QName(schemaURI, "double");
  private static final QName floatQName = new QName(schemaURI, "float");
  private static final QName longQName = new QName(schemaURI, "long");
  private static final QName intQName = new QName(schemaURI, "int");
  private static final QName shortQName = new QName(schemaURI, "short");
  private static final QName byteQName = new QName(schemaURI, "byte");
  private static final QName arrayQName = new QName(soapEncURI, "Array");

  private final ParameterSerializer paramSer = new ParameterSerializer();
  private final ArraySerializer arraySer = new ArraySerializer();
  private final XMLParameterSerializer xmlParamSer =
    new XMLParameterSerializer();

  public SOAPMappingRegistry()
  {
    // Register parameter serializer for SOAP-ENC encoding style.
    mapTypes(soapEncURI, RPCConstants.Q_ELEM_PARAMETER, Parameter.class,
             paramSer, paramSer);

    // Register array deserializer for SOAP-ENC encoding style.
    mapTypes(soapEncURI, arrayQName, null, null, arraySer);

    /*
      Map xsd:ur-type to java.lang.Object (no serializer/deserializer,
      just an association).
    */
    mapTypes(Constants.NS_URI_SOAP_ENC,
             new QName(Constants.NS_URI_SCHEMA_XSD, "ur-type"),
             Object.class, null, null);

    // Register parameter serializer for literal xml encoding style.
    mapTypes(Constants.NS_URI_LITERAL_XML, RPCConstants.Q_ELEM_PARAMETER,
             Parameter.class, xmlParamSer, xmlParamSer);

    try {

      Class XMISerializer = Class.forName("org.apache.soap.util.xml.XMISerializer");
      Class XMIParameterSerializer = 
        Class.forName("org.apache.soap.encoding.xmi.XMIParameterSerializer");

      // Register default serializers for XMI encoding style.
      mapTypes(Constants.NS_URI_XMI_ENC, null, null,
               (Serializer)XMISerializer.newInstance(), 
               (Deserializer)XMIParameterSerializer.newInstance());

      // Register serializer for Parameter class - not deserializer!
      mapTypes(Constants.NS_URI_XMI_ENC, null, Parameter.class,
               (Serializer)XMIParameterSerializer.newInstance(), null);

    } catch (IllegalAccessException iae) {
    } catch (InstantiationException ie) {
    } catch (ClassNotFoundException cnfe) {

      // If the class can't be loaded, continue without it...

    }

    Serializer ser = new Serializer()
    {
      public void marshall(String inScopeEncStyle, Class javaType, Object src,
                           Object context, Writer sink, NSStack nsStack,
                           XMLJavaMappingRegistry xjmr)
        throws IllegalArgumentException, IOException
      {
        nsStack.pushScope();

        SoapEncUtils.generateStructureHeader(inScopeEncStyle,
                                             javaType,
                                             context,
                                             sink,
                                             nsStack,
                                             xjmr);

        sink.write(src.toString() + "</" + context + '>');

        nsStack.popScope();
      }
    };

    Deserializer deser = new Deserializer()
    {
      public Bean unmarshall(String inScopeEncStyle, QName elementType,
                             Node src, XMLJavaMappingRegistry xjmr)
        throws IllegalArgumentException
      {
        Element root = (Element)src;
        String value = DOMUtils.getChildCharacterData(root);

        if (elementType.equals(stringQName))
        {
          return new Bean(String.class, value);
        }
        else if (elementType.equals(booleanQName))
        {
          return new Bean(boolean.class, new Boolean(value));
        }
        else if (elementType.equals(doubleQName))
        {
          return new Bean(double.class, new Double(value));
        }
        else if (elementType.equals(floatQName))
        {
          return new Bean(float.class, new Float(value));
        }
        else if (elementType.equals(longQName))
        {
          return new Bean(long.class, new Long(value));
        }
        else if (elementType.equals(intQName))
        {
          return new Bean(int.class, new Integer(value));
        }
        else if (elementType.equals(shortQName))
        {
          return new Bean(short.class, new Short(value));
        }
        else if (elementType.equals(byteQName))
        {
          return new Bean(byte.class, new Byte(value));
        }

        throw new IllegalArgumentException("I don't know how to " +
                                           "deserialize a '" + elementType +
                                           "' using encoding style '" +
                                           inScopeEncStyle + "'.");
      }
    };

    /*
      Register mapping of element type to/from Java type along with
      the serializer and deserializer for it. Note that we register
      the same serializer for primitives and their object wrapper
      counterparts. What this means is that once encoded into 
      SOAP_ENC, a primitive int and an Integer, for example, are
      indistinguishable. We believe (at least for now :-)) that this
      is the right thing to do .. that is, upon seeing an element
      like <foo xsi:type="xsd:int">53343</foo> we should always get
      a primitive int out of it.
    */
    mapTypes(soapEncURI, stringQName, String.class, ser, deser);
    mapTypes(soapEncURI, booleanQName, Boolean.class, ser, null);
    mapTypes(soapEncURI, booleanQName, boolean.class, ser, deser);
    mapTypes(soapEncURI, doubleQName, Double.class, ser, null);
    mapTypes(soapEncURI, doubleQName, double.class, ser, deser);
    mapTypes(soapEncURI, floatQName, Float.class, ser, null);
    mapTypes(soapEncURI, floatQName, float.class, ser, deser);
    mapTypes(soapEncURI, longQName, Long.class, ser, null);
    mapTypes(soapEncURI, longQName, long.class, ser, deser);
    mapTypes(soapEncURI, intQName, Integer.class, ser, null);
    mapTypes(soapEncURI, intQName, int.class, ser, deser);
    mapTypes(soapEncURI, shortQName, Short.class, ser, null);
    mapTypes(soapEncURI, shortQName, short.class, ser, deser);
    mapTypes(soapEncURI, byteQName, Byte.class, ser, null);
    mapTypes(soapEncURI, byteQName, byte.class, ser, deser);
  }

  /**
   * This function overrides the one in XMLJavaMappingRegistry for the sole
   * purpose of returning SOAP-ENC:Array when javaType represents an array.
   * The XMLJavaMappingRegistry will be consulted first, and if no mapping
   * is found, SOAP-ENC:Array is returned. Obviously, this only applies when
   * the encoding style is soap encoding.
   */
  public QName queryElementType(Class javaType, String encodingStyleURI)
    throws IllegalArgumentException
  {
    try
    {
      return super.queryElementType(javaType, encodingStyleURI);
    }
    catch (IllegalArgumentException e)
    {
      if (javaType != null
          && javaType.isArray()
          && encodingStyleURI != null
          && encodingStyleURI.equals(soapEncURI))
      {
        return arrayQName;
      }
      else
      {
        throw e;
      }
    }
  }

  /**
   * This function overrides the one in XMLJavaMappingRegistry for the sole
   * purpose of returning an ArraySerializer when javaType represents an
   * array. The XMLJavaMappingRegistry will be consulted first, and if no
   * serializer is found for javaType, ArraySerailizer is returned.
   * Obviously, this only applies when the encoding style is soap encoding.
   */
  public Serializer querySerializer(Class javaType, String encodingStyleURI)
    throws IllegalArgumentException
  {
    try
    {
      return super.querySerializer(javaType, encodingStyleURI);
    }
    catch (IllegalArgumentException e)
    {
      if (javaType != null
          && javaType.isArray()
          && encodingStyleURI != null
          && encodingStyleURI.equals(soapEncURI))
      {
        return arraySer;
      }
      else
      {
        throw e;
      }
    }
  }
}
