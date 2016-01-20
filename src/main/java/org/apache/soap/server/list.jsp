<%@ page import="java.util.*, org.apache.soap.server.*" %>

<!-- get the serviceManager object -->
<jsp:useBean id="serviceManager" 
             class="org.apache.soap.server.ServiceManager"
             scope="application">
</jsp:useBean>

<h1>Service Listing</h1>

<% 
  String[] serviceNames = serviceManager.list ();
  if (serviceNames.length == 0) {
    out.println ("<p>Sorry, there are no services currently deployed.</p>");
  } else {
    out.println ("<p>Here are the deployed services (select one to see");
    out.println ("details)</p>");
    %>
    <ul>
    <%
    for (int i = 0; i < serviceNames.length; i++) {
      String id = serviceNames[i];
    %>
      <li><a href="showdetails.jsp?id=<%=id%>"><%= id%></li>
    <%
    }
    %>
    </ul>
    <%
  }
%>
