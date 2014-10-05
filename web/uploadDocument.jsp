<%-- 
    Document   : uploadDocument
    Created on : Sep 25, 2014, 8:59:43 PM
    Author     : johnanilc
--%>

<%@page import="classes.UserSession"%>
<%@page import="classes.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>My Documents</title>
    </head>
    <body>
        <h1>E-Sign Dashboard</h1>
        <h2>Upload Signature</h2>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0">
                <tr>
                    <td>Signature </td>
                    <td><input type="file" name="signature" accept="image/*"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("signature_upload") != null) { %>
                <tr>
                    <td colspan="2"><div><%=request.getAttribute("signature_upload")%><div></td>
                </tr>
                <% } %>
            </table>
        </form>
        <% if( ((UserSession)request.getSession().getAttribute("user_session")).getUser().getSignature() != null) {%>
            <h2>Uploaded Signature</h2>
            <table width="100%">
                <tr>
                    <td colspan="2">
                        <img src="/userServlet?signature_image=1" />
                    </td>
                </tr>
            </table>
        <% } %>
        <h2>Upload Document</h2>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0">
                <tr>
                    <td>Document: </td>
                    <td><input type="file" name="document" accept="application/pdf"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("document_upload") != null) { %>
                <tr>
                    <td colspan="2"><div><%=request.getAttribute("document_upload")%><div></td>
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
                    <a href="/documentServlet?document_id=<%=documents.get(idx).getDocumentId()%>"><%=documents.get(idx).getName()%></a>
                </td>
                <td>
                     <%=documents.get(idx).getUpdatedDate()%>
                </td>
                <% if( ((UserSession)request.getSession().getAttribute("user_session")).getUser().getSignature() != null) {%>
                <td>
                    <a href="/eSignServlet?document_id=<%=documents.get(idx).getDocumentId()%>">Sign Now</a>
                </td>
                <% } %>
            </tr>
            <% } %>
        </table>    
    </body>
</html>
