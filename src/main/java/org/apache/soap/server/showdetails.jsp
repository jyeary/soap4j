<%@ page import="java.util.*, org.apache.soap.server.*, org.apache.soap.util.xml.*" %>

<!-- get the serviceManager object -->
<jsp:useBean id="serviceManager" 
             class="org.apache.soap.server.ServiceManager"
             scope="application">
</jsp:useBean>

<h1>Deployed Service Information</h1>

<% 
  String id = request.getParameter ("id");
  DeploymentDescriptor dd = (id != null) 
                            ? serviceManager.query (id) 
			    : null;
  String[] scopes = {"Page", "Request", "Session", "Application"}; 

  if (id == null) {
    out.println ("<p>Huh? You hafta select a service to display ..</p>");
  } else if (dd == null) {
    out.println ("<p>Service '" + id + "' is not known.</p>");
  } else {
    out.println ("<table border='1' width='100%'>");
    out.println ("<tr>");
    out.println ("<th colspan='2'><h2>'" + id + 
		 "' Service Deployment Descriptor</h2></th>");
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<th>Property</th>");
    out.println ("<th>Details</th>");
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<td>ID</td>");
    out.println ("<td>" + dd.getID()+ "</td>");
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<td>Scope</td>");
    out.println ("<td>" + scopes[dd.getScope()]+ "</td>");
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<td>Provider Type</td>");
    byte ptb = dd.getProviderType ();
    String pt = (ptb==DeploymentDescriptor.PROVIDER_JAVA) ? "java" : "script";
    out.println ("<td>" + pt + "</td>");
    out.println ("</tr>");
    out.println ("<tr>");
    if (ptb == DeploymentDescriptor.PROVIDER_JAVA) {
      out.println ("<td>Provider Class</td>");
      out.println ("<td>" + dd.getProviderClass()+ "</td>");
      out.println ("</tr>");
      out.println ("<tr>");
      out.println ("<td>Use Static Class</td>");
      out.println ("<td>" + dd.getIsStatic()+ "</td>");
    } else {
      out.println ("<td>Scripting Language</td>");
      out.println ("<td>" + dd.getScriptLanguage () + "</td>");
      out.println ("</tr>");
      out.println ("<tr>");
      if (ptb == DeploymentDescriptor.PROVIDER_SCRIPT_FILE) {
	out.println ("<td>Filename</td>");
	out.println ("<td>" + dd.getScriptFilenameOrString () + "</td>");
      } else {
	out.println ("<td>Script</td>");
	out.println ("<td><pre>" + dd.getScriptFilenameOrString () +
		     "</pre></td>");
      }
    }
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<td>Methods</td>");
    out.print ("<td>");
    String[] m = dd.getMethods ();
    for (int i = 0; i < m.length; i++) {
      out.print (m[i]);
      if (i < m.length-1) {
	out.print (", ");
      }
    }
    out.println ("</td>");
    out.println ("</tr>");
    out.println ("<tr>");
    out.println ("<td>Type Mappings</td>");
    out.println ("<td>");
    TypeMapping[] mappings = dd.getMappings();
    if (mappings != null) {
      for (int i = 0; i < mappings.length; i++) {
	out.print (mappings[i]);
	if (i < mappings.length-1) {
	  out.print ("<br>");
	} else {
	  break;
	}
      }
    }
    out.println ("</td>");
    out.println ("</tr>");
    out.println ("</table>");
  }
%>
