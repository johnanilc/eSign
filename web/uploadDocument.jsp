<%-- 
    Document   : uploadDocument
    Created on : Sep 25, 2014, 8:59:43 PM
    Author     : johnanilc
--%>

<%@page import="classes.userServlet.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>My Documents</title>
    </head>
    <body>
        <h1>My Documents</h1>
        <h2>Upload Document</h2>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0">
                <tr>
                    <td>Document: </td>
                    <td><input type="file" name="document" size="50" accept="application/pdf"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("message") != null) { %>
                <tr>
                    <td colspan="2"><div><%=request.getAttribute("message")%><div></td>
                </tr>
                <% } %>
            </table>
        </form>
        <h2>Uploaded Documents</h2>    
        <% ArrayList<Document> documents = (ArrayList<Document>)request.getAttribute("documents"); %>
        <table align="center" width="100%">
            <tr>
                <td>
                    Document #
                </td>
                <td>
                    Name
                </td>
                <td>
                    Updated Date
                </td>
            </tr>
            <% for (int idx=0; idx<documents.size(); idx++) {%>
            <tr>
                <td>
                    <%=idx+1%>
                </td>
                <td>
                    <a href="documentServlet?document_id=<%=documents.get(idx).getDocumentId()%>"><%=documents.get(idx).getName()%></a>
                </td>
                <td>
                     <%=documents.get(idx).getUpdatedDate()%>
                </td>
            </tr>
            <% } %>
        </table>    
    </body>
</html>
