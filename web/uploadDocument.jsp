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
        <script type="text/javascript" lang="javascript">
            function deleteDocument(documentName){
                var canDelete = confirm("Are you sure you want to delete '" + documentName + "' along with all the signatures of all users?");
                if (canDelete === true){
                    return true;
                }
                return false;
            }
        </script>
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
                        <img src="userServlet?signature_image=1" />
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
                    <b>Document #</b>
                </td>
                <td>
                    <b>Name</b>
                </td>
                <td>
                    <b>Uploaded Date</b>
                </td>
                <td>
                    <b>Last Signed Date</b>
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
                <td>
                    <%=documents.get(idx).getLastSignedDate()%>
                </td>
                <td>
                    <% if( ((UserSession)request.getSession().getAttribute("user_session")).getUser().getSignature() != null) {%>
                        <a href="eSignServlet?document_id=<%=documents.get(idx).getDocumentId()%>">Sign</a>
                    <% } %>
                </td>
                <td>
                     <% if (documents.get(idx).isSigned()) {%>
                        <a href="documentServlet?document_id=<%=documents.get(idx).getDocumentId()%>&is_download=true">Download Signed PDF</a>
                    <% } %>
                </td>
                <td>        
                    <form method="post" action="documentServlet">
                        <input type="hidden" name="document_id" value="<%=documents.get(idx).getDocumentId()%>"/>
                        <input type="submit" value="Delete" onclick="return deleteDocument(<%=documents.get(idx).getName()%>);"/></td>
                    </form>
            </tr>
            <% } %>
        </table>    
    </body>
</html>
