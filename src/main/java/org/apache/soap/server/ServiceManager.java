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
import org.apache.soap.util.xml.QName;
import org.apache.soap.*;

/**
 * A <code>ServiceManager</code> manages services and their associated
 * <code>DeploymentDescriptors</code>.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class ServiceManager {
  Hashtable dds;
  String filename = "DeployedServices.ds";
  // service management service's deployment descriptor 
  DeploymentDescriptor smsdd;
  String[] serviceNamesCache;

  public ServiceManager () {
    smsdd = new DeploymentDescriptor ();
    smsdd.setID (ServerConstants.SERVICE_MANAGER_SERVICE_NAME);
    String[] svcs = new String[] { "deploy", "undeploy", "list",  "query" };
    smsdd.setMethods (svcs);
    smsdd.setScope (DeploymentDescriptor.SCOPE_APPLICATION);
    smsdd.setProviderType (DeploymentDescriptor.PROVIDER_JAVA);
    smsdd.setProviderClass ("org.apache.soap.server.ServiceManager");
    smsdd.setIsStatic (false);

    // set up mappings to send/recv DeploymentDescriptor and TypeMapping
    // objects
    smsdd.setMappings (new TypeMapping[] {
      new TypeMapping (Constants.NS_URI_SOAP_ENC,
		       new QName (Constants.NS_URI_IBM_SOAP,
				  "DeploymentDescriptor"),
		       "org.apache.soap.server.DeploymentDescriptor",
		       "org.apache.soap.encoding.soapenc.BeanSerializer",
		       "org.apache.soap.encoding.soapenc.BeanSerializer"),
      new TypeMapping (Constants.NS_URI_SOAP_ENC,
		       new QName (Constants.NS_URI_IBM_SOAP,
				  "TypeMapping"),
		       "org.apache.soap.server.TypeMapping",
		       "org.apache.soap.server.TypeMappingSerializer",
		       "org.apache.soap.server.TypeMappingSerializer")});

    // load in a serialized thing
    try {
      FileInputStream fis = new FileInputStream (filename);
      ObjectInputStream is = new ObjectInputStream (fis);

      dds = (Hashtable) is.readObject ();
      is.close ();
    } catch(Exception e) {
      dds = new Hashtable ();
      System.err.println ("SOAP Service Manager: Unable to read '" +
                          filename +  "': assuming fresh start");
    }
  }

  private void saveRegistry () throws SOAPException {
    serviceNamesCache = null;
    try {
      FileOutputStream fos = new FileOutputStream (filename);
      ObjectOutputStream os = new ObjectOutputStream (fos);

      os.writeObject (dds);
      os.close ();
    } catch (Exception e) {
      throw new SOAPException (Constants.FAULT_CODE_SERVER,
			       "Error saving services registry: " +
			       e.getMessage ());
    };
  }

  /**
   * Deploy a service: add the descriptor to the persistent record of
   * what has been deployed. 
   */
  public void deploy (DeploymentDescriptor dd) throws SOAPException {
    String id = dd.getID ();
    if (id.equals (ServerConstants.SERVICE_MANAGER_SERVICE_NAME)) {
      throw new SOAPException (Constants.FAULT_CODE_SERVER,
			       "service management service '" +
			       ServerConstants.SERVICE_MANAGER_SERVICE_NAME +
			       "' cannot be user deployed");
    }
    dds.put (id, dd);
    saveRegistry ();
  }

  /**
   * Undeploy a service: remove the descriptor from the persistent record
   * of what has been deployed.
   *
   * @id the id of the service I'm undeploying
   * @exception SOAPException if service is not found
   */
  public DeploymentDescriptor undeploy (String id) 
       throws SOAPException {
    DeploymentDescriptor dd = (DeploymentDescriptor) dds.remove (id);
    if (dd != null) {
      saveRegistry ();
    } else {
      throw new SOAPException (Constants.FAULT_CODE_SERVER,
			       "service '" + id + "' unknown");
    }
    return dd;
  }

  /**
   * Return the deployment descriptor for a service. If the id identifies
   * the service management service, then the deployment descriptor of
   * the service management service is returned.
   *
   * @param id the id of the service I'm looking for
   * @exception SOAPException if service is not found
   */
  public DeploymentDescriptor query (String id) throws SOAPException {
    if (id == null) {
      return null;
    } else if (id.equals (ServerConstants.SERVICE_MANAGER_SERVICE_NAME)) {
      return smsdd;
    } else {
      DeploymentDescriptor dd = (DeploymentDescriptor) dds.get (id);
      if (dd != null) {
	return dd;
      } else {
	throw new SOAPException (Constants.FAULT_CODE_SERVER,
				 "service '" + id + "' unknown");
      }
    }
  }

  /**
   * Return an array of all the deployed service names. Returns an array
   * of length zero if there are no deployed services.
   *
   * @return array of all service names
   */
  public String[] list () {
    if (serviceNamesCache != null) {
      return serviceNamesCache;
    }
    Enumeration e = dds.keys ();
    int count = dds.size ();
    serviceNamesCache = new String[count];
    for (int i = 0; i < count; i++) {
      serviceNamesCache[i] = (String) e.nextElement ();
    }
    return serviceNamesCache;
  }
}
