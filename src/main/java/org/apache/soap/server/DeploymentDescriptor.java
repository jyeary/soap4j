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

package org.apache.soap.server;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.apache.soap.util.xml.*;
import org.apache.soap.util.DOMUtils;
import org.apache.soap.Constants;
import org.apache.soap.encoding.*;

/**
 * This class represents the deployment information about a SOAP service.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class DeploymentDescriptor implements Serializable {
  // scopes for the lifecycles of the provider class
  public static final int SCOPE_PAGE = 0;
  public static final int SCOPE_REQUEST = 1;
  public static final int SCOPE_SESSION = 2;
  public static final int SCOPE_APPLICATION = 3;

  // types of providers
  public static final byte PROVIDER_JAVA = (byte) 0;
  public static final byte PROVIDER_SCRIPT_FILE = (byte) 1;
  public static final byte PROVIDER_SCRIPT_STRING = (byte) 2;

  String id;
  int scope;
  byte providerType = -1;
  String providerClass;
  boolean isStatic;
  String scriptFilenameOrString;
  String scriptLanguage;
  String[] methods;
  TypeMapping[] mappings;
  transient SOAPMappingRegistry cachedSMR;
 
  /**
   * Constructor.
   *
   * @param id the name of the service. Should be of valid URI syntax.
   */
  public DeploymentDescriptor () {
  }

  /**
   * ID of this service.
   */
  
  public void setID (String id) {
    this.id = id;
  }

  public String getID () {
    return id;
  }

  /**
   * Lifecyle of the object providing the service. 
   */
  public void setScope (int scope) {
    this.scope = scope;
  }

  public int getScope () {
    return scope;
  }

  /**
   * Methods provided by the service.
   */
  public void setMethods (String[] methods) {
    this.methods = methods;
  }

  public String[] getMethods () {
    return methods;
  }

  /**
   * Type of provider. 
   */
  public void setProviderType (byte providerType) {
    this.providerType = providerType;
  }

  public byte getProviderType () {
    return providerType;
  }

  /**
   * For Java providers, the class providing the service.
   */
  public void setProviderClass (String providerClass) {
    this.providerClass = providerClass;
  }

  public String getProviderClass () {
    return providerClass;
  }

  /**
   * For Java providers, is it static or not.
   */
  public void setIsStatic (boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean getIsStatic () {
    return isStatic;
  }

  public void setScriptLanguage (String scriptLanguage) {
    this.scriptLanguage = scriptLanguage;
  }

  public String getScriptLanguage () {
    return scriptLanguage;
  }

  public void setScriptFilenameOrString (String scriptFilenameOrString) {
    this.scriptFilenameOrString = scriptFilenameOrString;
  }

  public String getScriptFilenameOrString () {
    return scriptFilenameOrString;
  }

  /**
   * Type mappings between XML types of certain encodings and
   * Java types. 
   *
   * @param map the structure containing the mapping info
   */
  public void setMappings (TypeMapping[] mappings) {
    this.mappings = mappings;
  }

  /**
   * Return the registered mappings.
   */
  public TypeMapping[] getMappings () {
    return mappings;
  }

  /**
   * Cache the SOAP serialization registry for this descriptor; used only
   * by me.
   */
  private void setCachedSMR (SOAPMappingRegistry cachedSMR) {
    this.cachedSMR = cachedSMR;
  }

  private SOAPMappingRegistry getCachedSMR () {
    return cachedSMR;
  }

  /**
   * Write out the deployment descriptor according to the 
   * the deployment descriptor DTD.
   */
  public void toXML (Writer pr) {
    PrintWriter pw = new PrintWriter (pr);
    
    pw.println ("<isd:service xmlns:isd=\"" +
		Constants.NS_URI_IBM_DEPLOYMENT + "\" id=\"" + id + "\">");

    byte pt = providerType;
    String[] scopes = {"Page", "Request", "Session", "Application"};
    pw.print ("  <isd:provider type=\"" + 
	      (pt == DeploymentDescriptor.PROVIDER_JAVA ? "java" : "script") +
	      "\" scope=\"" + scopes[scope] + "\" methods=\"");
    for (int i = 0; i < methods.length; i++) {
      pw.print (methods[i]);
      if (i < methods.length-1) {
	pw.print (" ");
      }
    }
    pw.println ("\">");
    if (pt == DeploymentDescriptor.PROVIDER_JAVA) {
      pw.println ("    <isd:java class=\"" + providerClass +
		  "\" static=\"" + (isStatic ? "true" : "false") + "\"/>");
    } else {
      pw.print ("    <isd:script language=\"" + scriptLanguage);
      if (pt == DeploymentDescriptor.PROVIDER_SCRIPT_FILE) {
	pw.println (" source=\"" + scriptFilenameOrString + "\"/>");
      } else {
	pw.println ("\">");
	pw.println ("      <![CDATA[");
	pw.println (scriptFilenameOrString);
	pw.println ("      ]]>");
	pw.println ("    </isd:script>");
      }
    }
    pw.println ("  </isd:provider>");

    if (mappings != null) {
      pw.println ("  <isd:mappings>");
      for (int i = 0; i < mappings.length; i++) {
	TypeMapping tm = mappings[i];
	pw.print ("    <isd:map encodingStyle=\"" + tm.encodingStyle +
		  "\" xmlns:x=\"" + tm.elementType.getNamespaceURI () +
		  "\" qname=\"x:" + tm.elementType.getLocalPart () +
		  "\" javaType=\"" + tm.javaType + "\"");
	if (tm.xml2JavaClassName != null) {
	  pw.print (" xml2JavaClassName=\"" + tm.xml2JavaClassName + "\"");
	}
	if (tm.java2XMLClassName != null) {
	  pw.print (" java2XMLClassName=\"" + tm.java2XMLClassName + "\"");
	}
	pw.println ("/>");
      }
      pw.println ("  </isd:mappings>");
    }

    pw.println ("</isd:service>");
    pw.flush ();
  }

  /**
   * Build a deployment descriptor from a document corresponding to
   * the deployment descriptor DTD.
   */
  public static DeploymentDescriptor fromXML (Element root) {
    if ((root == null) ||
	!root.getNamespaceURI().equals (Constants.NS_URI_IBM_DEPLOYMENT) ||
	!root.getLocalName().equals ("service")) {
      throw new IllegalArgumentException ("root is null or document element " +
					  "is not {" +
					  Constants.NS_URI_IBM_DEPLOYMENT +
					  "}service");
    }

    DeploymentDescriptor dd = new DeploymentDescriptor ();
    NodeList nl;
    Element e;
    
    String id = DOMUtils.getAttribute (root, "id");
    if (id == null) {
      throw new IllegalArgumentException ("required 'id' attribute " +
					  "missing in deployment descriptor");
    }
    dd.setID (id);

    nl = root.getElementsByTagNameNS (Constants.NS_URI_IBM_DEPLOYMENT,
				      "provider");
    if ((nl == null) || nl.getLength () != 1) {
      throw new IllegalArgumentException ("exactly one 'provider' element " +
					  "missing in deployment descriptor");
    }
    e = (Element) nl.item (0);
    String typeStr = DOMUtils.getAttribute (e, "type");
    String scopeStr = DOMUtils.getAttribute (e, "scope");
    String methodsStr = DOMUtils.getAttribute (e, "methods");
    if ((typeStr == null) ||
	(!typeStr.equals ("java") && !typeStr.equals ("script")) ||
	(scopeStr == null) ||
	(!scopeStr.equals ("Page") && !scopeStr.equals ("Request") &&
	 !scopeStr.equals ("Session") && !scopeStr.equals ("Application")) ||
	(methodsStr == null) || methodsStr.equals ("")) {
      throw new IllegalArgumentException ("invalid value for type or scope " +
					  "or methods attribute in provider " +
					  "element of deployment descriptor");
    }
    
    int scope = -1;
    String[] methods;

    if (typeStr.equals ("java")) {
      dd.setProviderType (DeploymentDescriptor.PROVIDER_JAVA);
      nl = e.getElementsByTagNameNS (Constants.NS_URI_IBM_DEPLOYMENT, "java");
      if ((nl == null) || nl.getLength () != 1) {
	throw new IllegalArgumentException ("exactly one 'java' element " +
					    "missing in deployment " +
					    "descriptor");
      }
      e = (Element) nl.item (0);
      String className = DOMUtils.getAttribute (e, "class");
      if (className == null) {
	throw new IllegalArgumentException ("<java> element requires " +
					    "'class' attribute");
      }
      dd.setProviderClass (className);
      String isStatic = DOMUtils.getAttribute (e, "static");
      boolean isStaticBool = false;
      if (isStatic != null) {
	if (isStatic.equals ("false")) {
	  isStaticBool = false;
	} else if (isStatic.equals ("true")) {
	  isStaticBool = true;
	} else {
	  throw new IllegalArgumentException ("'static' attribute of " +
					      "<java> element must be " +
					      "true or false");
	}
      }
      dd.setIsStatic (isStaticBool);

    } else {
      nl = e.getElementsByTagNameNS (Constants.NS_URI_IBM_DEPLOYMENT,
				     "script");
      if ((nl == null) || nl.getLength () != 1) {
	throw new IllegalArgumentException ("exactly one 'script' element " +
					    "missing in deployment " +
					    "descriptor");
      }
      e = (Element) nl.item (0);
      dd.setScriptLanguage (DOMUtils.getAttribute (e, "language"));
      String source = DOMUtils.getAttribute (e, "source");
      if (source != null) {
	dd.setProviderType (DeploymentDescriptor.PROVIDER_SCRIPT_FILE);
	dd.setScriptFilenameOrString (source);
      } else {
	dd.setProviderType (DeploymentDescriptor.PROVIDER_SCRIPT_STRING);
	dd.setScriptFilenameOrString (DOMUtils.getChildCharacterData (e));
      }
    }

    if (scopeStr.equals ("Page")) {
      scope = DeploymentDescriptor.SCOPE_PAGE;
    } else if (scopeStr.equals ("Request")) {
      scope = DeploymentDescriptor.SCOPE_REQUEST;
    } else if (scopeStr.equals ("Session")) {
      scope = DeploymentDescriptor.SCOPE_SESSION;
    } else { // scopeStr.equals ("Application")
      scope = DeploymentDescriptor.SCOPE_APPLICATION;
    }
    dd.setScope (scope);

    StringTokenizer st = new StringTokenizer (methodsStr);
    int nTokens = st.countTokens ();
    methods = new String[nTokens];
    for (int i = 0; i < nTokens; i++) {
      methods[i] = st.nextToken ();
    }
    dd.setMethods (methods);
    
    // read the type mappings
    nl = root.getElementsByTagNameNS (Constants.NS_URI_IBM_DEPLOYMENT,
				      "mappings");
    if ((nl == null) || nl.getLength () > 1) {
      throw new IllegalArgumentException ("at most one 'mappings' element " +
					  "allowed in deployment descriptor");
    }
    if (nl.getLength () == 1) {
      e = (Element) nl.item (0);
      nl = e.getElementsByTagNameNS (Constants.NS_URI_IBM_DEPLOYMENT, "map");
      int nmaps = nl.getLength ();
      if (nmaps > 0) {
	TypeMapping[] tms = new TypeMapping[nmaps];
	dd.setMappings (tms);
	for (int i = 0; i < nmaps; i++) {
	  e = (Element) nl.item (i); 
	  String qnameQname = DOMUtils.getAttribute (e, "qname");
	  int pos = qnameQname.indexOf (':');
	  String prefix = qnameQname.substring (0, pos);
	  String localPart = qnameQname.substring (pos+1);
	  String nsURI = DOMUtils.getNamespaceURIFromPrefix (e, prefix);
	  tms[i] = 
	    new TypeMapping (DOMUtils.getAttribute (e, "encodingStyle"),
			     new QName (nsURI, localPart),
			     DOMUtils.getAttribute (e, "javaType"),
			     DOMUtils.getAttribute (e, "java2XMLClassName"),
			     DOMUtils.getAttribute (e, "java2XMLClassName"));
	}
      }
    }

    return dd;
  }

  /**
   * What do u think this does?
   */
  public String toString () {
    StringBuffer methodsStrbuf = new StringBuffer ("[");
    for (int i = 0; i < methods.length; i++) {
      methodsStrbuf.append (methods[i]);
      if (i < methods.length-1) {
        methodsStrbuf.append (",");
      }
    }
    methodsStrbuf.append ("]");
    String header = "[DeploymentDescriptor id='" + id + "', " +
      "scope='" + scope + "', ";
    String body = null;
    if (providerType == PROVIDER_JAVA) {
      body = "class='" + providerClass + "', static='" + isStatic + "', ";
    } else if (providerType == PROVIDER_SCRIPT_FILE) {
      body = "source='" + scriptFilenameOrString + "', ";
      body += "language='" + scriptLanguage + "', ";
    } else {
      body = "script='" + scriptFilenameOrString + "', ";
      body += "language='" + scriptLanguage + "', ";
    }
    return header + body + "methods='" + methodsStrbuf + "', " +
      "mappings='" + mappings + "]";
  }

  /**
   * Utility to generate an XML serialization registry from all the
   * type mappings registered into a deployment descriptor.
   * 
   * @param dd the deployment descriptor
   * @return the xml serialization registry
   */
  public static SOAPMappingRegistry
  buildSOAPMappingRegistry (DeploymentDescriptor dd) {
    TypeMapping[] maps = dd.getMappings ();
    SOAPMappingRegistry smr = dd.getCachedSMR ();
    if (smr != null) {
      return smr;
    } else {
      smr = new SOAPMappingRegistry ();
    }
    if (maps != null) {
      for (int i = 0; i < maps.length; i++) {
	TypeMapping tm = maps[i];
	int step = 0;
	try {
	  step = 0;
	  Class javaType = null;
	  if (tm.javaType != null) {
	    javaType = Class.forName (tm.javaType);
	  }
	  step = 1;
	  Serializer s = null;
	  if (tm.java2XMLClassName != null) {
	    Class c = Class.forName (tm.java2XMLClassName);
	    s = (Serializer) c.newInstance ();
	  }
	  step = 2;
	  Deserializer d = null;
	  if (tm.xml2JavaClassName != null) {
	    Class c = Class.forName (tm.xml2JavaClassName);
	    d = (Deserializer) c.newInstance ();
	  }
	  smr.mapTypes (tm.encodingStyle, tm.elementType, javaType, s, d);
	} catch (Exception e2) {
	  String m = "deployment error in SOAP service '" + dd.getID () + 
	    "': ";
	  if (step == 0) {
	    m += "class name '" + tm.javaType + "' could not be resolved: ";
	  } else if (step == 1) {
	    m += "class name '" + tm.java2XMLClassName + "' could not be " +
	      "resolved as a serializer: ";
	  } else {
	    m += "class name '" + tm.xml2JavaClassName + "' could not be " +
	      "resolved as a deserializer: ";
	  }
	  throw new IllegalArgumentException (m + e2.getMessage ());
	}
      }
    }
    dd.setCachedSMR (smr);
    return smr;
  }
}
