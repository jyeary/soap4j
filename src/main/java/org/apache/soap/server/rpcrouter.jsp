<%@ page language="java" %>

<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.reflect.*" %>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="org.apache.soap.util.Bean" %>
<%@ page import="org.apache.soap.util.MethodUtils" %>
<%@ page import="org.apache.soap.util.IOUtils" %>
<%@ page import="org.apache.soap.util.StringUtils" %>
<%@ page import="org.apache.soap.util.xml.XMLParserLiaison" %>
<%@ page import="org.apache.soap.util.xml.XercesParserLiaison" %>
<%@ page import="org.apache.soap.*" %>
<%@ page import="org.apache.soap.encoding.*" %>
<%@ page import="org.apache.soap.rpc.*" %>
<%@ page import="org.apache.soap.server.*" %>

<jsp:useBean id="serviceManager"
             class="org.apache.soap.server.ServiceManager"
             scope="application">
</jsp:useBean>

<%
Response resp = null;
String targetID = null;

// Query the relevant header fields.
String requestMethod = request.getMethod ();
String contentType = request.getContentType ();
int contentLength = request.getContentLength ();

Call call = null;

try
{
  if (requestMethod == null ||
      !requestMethod.equals (Constants.HEADER_POST))
  {
    throw new SOAPException (Constants.FAULT_CODE_PROTOCOL,
                             "Can only respond to '" +
                             Constants.HEADER_POST + "' requests.");
  }
  else if (contentType == null
           || !contentType.equals (Constants.HEADERVAL_CONTENT_TYPE))
  {
    throw new SOAPException (Constants.FAULT_CODE_PROTOCOL,
                             "Content type must be: '" +
                             Constants.HEADERVAL_CONTENT_TYPE + "'.");
  }
  else if (contentLength < 0)
  {
    throw new SOAPException (Constants.FAULT_CODE_PROTOCOL,
                             "Content length must be specified.");
  }
  else
  {
    System.err.println (">>(" + new Date() +
                        ") Processing SOAP request...");

    response.setContentType (Constants.HEADERVAL_CONTENT_TYPE);

    Reader requestReader = request.getReader ();
    char[] payload       = new char[contentLength];
    int    offset        = 0;

    while (offset < contentLength) {
      offset += requestReader.read (payload, offset, contentLength - offset);
    }

    // Parse the incoming request stream.
    org.apache.soap.util.xml.XMLParserLiaison xpl = new XercesParserLiaison ();
    Document callDoc = xpl.read("- SOAP HTTP RPC Call Envelope -",
                                new CharArrayReader (payload));
    Element payloadEl = null;

    if (callDoc == null) {
      throw new SOAPException(Constants.FAULT_CODE_PROTOCOL,
                              "DOM parsing error: " + payload);
    }

    payloadEl = callDoc.getDocumentElement ();

    try {
      // Unmarshall the call envelope.
      Envelope callEnv = Envelope.unmarshall (payloadEl);

      // Extract the call from the call envelope.
      call = Call.extractFromEnvelope (callEnv, serviceManager);
    } catch (IllegalArgumentException e) {
      String msg = e.getMessage ();
      String faultCode = (msg != null
                          && msg.equals(Constants.ERR_MSG_VERSION_MISMATCH))
                         ? Constants.FAULT_CODE_VERSION_MISMATCH
                         : Constants.FAULT_CODE_CLIENT;

      throw new SOAPException (faultCode, msg, e);
    }

    // good call?
    targetID = call.getTargetObjectURI ();
    if (targetID == null) {
      throw new SOAPException (
        RPCConstants.FAULT_CODE_SERVER_BAD_TARGET_OBJECT_URI,
        "Unable to determine object id from call: is the method element " +
	"namespaced?");
    }

    DeploymentDescriptor dd = serviceManager.query (targetID);
    int scope = dd.getScope ();
    byte providerType = dd.getProviderType ();
    String className;
    Object targetObject = null;
    if (providerType == DeploymentDescriptor.PROVIDER_JAVA) {
      className = dd.getProviderClass ();
    } else {
      // for scripts, we need a new BSF manager basically
      className = "com.ibm.bsf.BSFManager";
    }

    // make sure the method that's being called is published ..
    boolean ok = false;
    String callMethodName = call.getMethodName ();
    String[] pubMethods = dd.getMethods ();
    for (int i = 0; i < pubMethods.length; i++) {
      if (callMethodName.equals (pubMethods[i])) {
        ok = true;
        break;
      }
    }
    if (!ok) {
      throw new SOAPException (Constants.FAULT_CODE_SERVER,
                               "Method '" + callMethodName + "' is not " +
                               "supported.");
    }

    // determine the scope and lock object to use to manage the lifecycle
    // of the service providing object
    int[] iScopes = {PageContext.PAGE_SCOPE, PageContext.REQUEST_SCOPE,
			    PageContext.SESSION_SCOPE,
			    PageContext.APPLICATION_SCOPE};
    int iScope = iScopes[scope];
    Object[] scopeLocks = {pageContext, request, session, application};
    Object scopeLock = scopeLocks[scope];

    boolean freshObject = false;

    // find the target object on which the requested method should
    // be invoked
    if (targetID.equals (ServerConstants.SERVICE_MANAGER_SERVICE_NAME)) {
      targetObject = serviceManager;
    } else {
      // locate (or create) the target object and invoke the method
      synchronized (scopeLock) {
        targetObject = pageContext.getAttribute (targetID, iScope);
        if (targetObject == null) {
          try {
            Class c = Class.forName (className);
            if (dd.getIsStatic ()) {
              targetObject = c;
            } else {
              targetObject = c.newInstance ();
            }
            freshObject = true;
          } catch (Exception e) {
	    String msg;
	    if (providerType == DeploymentDescriptor.PROVIDER_JAVA) {
	      msg = "Unable to resolve target object: " + e.getMessage ();
	    } else {
	      msg = "Unable to load BSF: script services not available " +
		"without BSF: " + e.getMessage ();
	    }
            throw new SOAPException (
              RPCConstants.FAULT_CODE_SERVER_BAD_TARGET_OBJECT_URI,
              msg, e);
          }
        }
        pageContext.setAttribute (targetID, targetObject, iScope);
      }
    }
      
    // build the args and determine response encoding style
    Vector params = call.getParams ();
    String respEncStyle = call.getEncodingStyleURI ();
    Object[] args = null;
    Class[] argTypes = null;
    if (params != null) {
      int paramsCount = params.size ();
      args = new Object[paramsCount];
      argTypes = new Class[paramsCount];
      for (int i = 0; i < paramsCount; i++) {
        Parameter param = (Parameter) params.elementAt (i);
        args[i] = param.getValue ();
        argTypes[i] = param.getType ();
        if (respEncStyle == null) {
          respEncStyle = param.getEncodingStyleURI ();
        }
      }
    }
    if (respEncStyle == null) {
      // need to set a default encoding to be used for the response
      respEncStyle = Constants.NS_URI_SOAP_ENC;
    }
    
    // invoke the method (directly for Java and via InvokeBSF for script
    // methods)
    Bean result = null;
    try {
      if (providerType == DeploymentDescriptor.PROVIDER_JAVA) {
	Method m = MethodUtils.getMethod (targetObject, callMethodName,
					      argTypes);
	result = new Bean (m.getReturnType (), m.invoke (targetObject, args));
      } else {
	// find the class that provides the BSF services (done
	// this way via reflection to avoid a static dependency on BSF)
	Class bc = Class.forName ("org.apache.soap.server.InvokeBSF");

        if (freshObject) {
          // get the script string to exec
          String script = dd.getScriptFilenameOrString ();
          if (providerType == DeploymentDescriptor.PROVIDER_SCRIPT_FILE) {
            String fileName = getServletContext().getRealPath (script);
            script = IOUtils.getStringFromReader (new FileReader (fileName));
          }
          
	  // exec it
          Class[] sig = {DeploymentDescriptor.class,
                         Object.class,
                         String.class};
          Method m = MethodUtils.getMethod (bc, "init", sig, true);
          m.invoke (null, new Object[] {dd, targetObject, script});
        }

        // now invoke the service
        Class[] sig = {DeploymentDescriptor.class,
                       Object.class,
                       String.class,
                       Object[].class};
        Method m = MethodUtils.getMethod (bc, "service", sig, true);
        result = (Bean) m.invoke (null, new Object[] {dd, targetObject, 
                                                      callMethodName, args});
      }
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException ();
      throw new SOAPException (Constants.FAULT_CODE_SERVER, t.getMessage(), t);
    } catch (ClassNotFoundException e) {
      throw new SOAPException (Constants.FAULT_CODE_SERVER,
			       "Unable to load BSF: script services " +
			       "unsupported with BSF", e);
    } catch (Throwable t) {
      throw new SOAPException (Constants.FAULT_CODE_SERVER, t.getMessage(), t);
    }
   
    // build the response object
    Parameter ret = null;
    if (result.type != void.class) {
      ret = new Parameter (RPCConstants.ELEM_RETURN, result.type, 
			   result.value, null);
    }
    resp = new Response (targetID, callMethodName, ret, null, null,
			 respEncStyle);
  }
}
catch (SOAPException e)
{
  Fault fault = new Fault ();
  String faultCode = e.getFaultCode ();
  String faultString = e.getMessage ();
  boolean returnSOAPResponse = true;

  if (faultCode == null || faultCode.startsWith (Constants.FAULT_CODE_SERVER))
  {
    response.setStatus (ServerConstants.SC_INTERNAL_SERVER_ERROR);
  }
  else if (faultCode.startsWith (Constants.FAULT_CODE_CLIENT))
  {
    response.setStatus (ServerConstants.SC_BAD_REQUEST);
  }
  else if (faultCode.startsWith (Constants.FAULT_CODE_PROTOCOL))
  {
    response.setStatus (ServerConstants.SC_BAD_REQUEST);
    returnSOAPResponse = false;
  }

  if (returnSOAPResponse)
  {
    fault.setFaultCode (faultCode);
    fault.setFaultString (faultString);
    fault.setFaultActorURI (request.getRequestURI ());

    resp = new Response (null,
                         null,
                         fault,
                         null,
                         null,
                         null);
  }
  else
  {
    out.println (faultString);
  }
}
catch (Throwable t)
{
  t.printStackTrace ();
  t.printStackTrace (new PrintWriter (out));
}

// Send it out.
if (resp != null)
{
  try
  {
    // Build an envelope containing the response.
    Envelope respEnvelope = resp.buildEnvelope ();
    SOAPMappingRegistry smr = (call != null
                               ? call.getSOAPMappingRegistry ()
                               : new SOAPMappingRegistry());

    respEnvelope.marshall (out, smr);

    out.println (StringUtils.lineSeparator);
  }
  catch (IllegalArgumentException e)
  {
    throw new SOAPException (Constants.FAULT_CODE_SERVER, e.getMessage(), e);
  }
  catch (IOException e)
  {
    throw new SOAPException (Constants.FAULT_CODE_SERVER, e.getMessage(), e);
  }
}
%>
