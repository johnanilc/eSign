<%-- 
    Document   : upload_document
    Created on : Sep 25, 2014, 8:59:43 PM
    Author     : johnanilc
--%>

<%@page import="classes.Document.DocumentDetail"%>
<%@page import="classes.User"%>
<%@page import="classes.UserSession"%>
<%@page import="classes.Document"%>
<%@page import="classes.DocumentSigner"%>
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
        <table width="100%">
            <tr>
                <td align="right"><a href="login.jsp">Logout</a></td>
            </tr>
        </table>
        <h1>E-Sign Dashboard</h1>
        <h2>Upload Signature</h2>
        <% User user = ((UserSession)request.getSession().getAttribute("user_session")).getUser(); %>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0">
                <tr>
                    <td>Signature </td>
                    <td><input type="file" name="signature" accept="image/*"/></td>
                    <td><a href="create_signature.jsp">Create Signature</a></td>
                </tr>
                <tr>
                    <td colspan="3">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("signature_upload") != null) { %>
                <tr>
                    <td colspan="3"><div><%=request.getAttribute("signature_upload")%><div></td>
                </tr>
                <% } %>
            </table>
        </form>
        <% if(user.getSignature() != null) {%>
            <h2>Uploaded Signature</h2>
            <table width="100%">
                <tr>
                    <td colspan="2">
                        <img src="userServlet?signature_image=<%=System.currentTimeMillis()%>" />
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
        <h2>Documents</h2>    
        <% ArrayList<DocumentDetail> documents = User.getUserDocuments(user.getUserId()); %>
        <table align="center" width="100%">
            <tr>
                <td>
                    <b>Document #</b>
                </td>
                <td>
                    <b>Name</b>
                </td>
                <td>
                    <b>Owner</b>
                </td>
                <td>
                    <b>Uploaded Date</b>
                </td>
                <td>
                    <b>Last Signed Date</b>
                </td>
            </tr>
            <% for (int idx=0; idx<documents.size(); idx++) { %>
            <% DocumentDetail document = documents.get(idx); %>
            <% int documentId = document.getDocumentId(); %>
            <tr>
                <td>
                    <%=idx+1%>
                </td>
                <td>
                    <a href="documentServlet?document_id=<%=documentId%>"><%=document.getName()%></a>
                </td>
                <td>
                    <%=document.getOwnerName()%>
                </td>
                <td>
                     <%=document.getUpdatedDate()%>
                </td>
                <td>
                    <%=document.getLastSignedDate()%>
                </td>
                <td>
                    <% if(user.getSignature() != null) {%>
                        <a href="eSignServlet?document_id=<%=documentId%>">Sign</a>
                    <% } %>
                </td>
                <td>
                    <% if (document.getOwnerId() == user.getUserId()) {%>
                    <a href="add_signer.jsp?document_id=<%=documentId%>">Add Signer</a>
                    <%}%>
                </td>
                <td>
                     <% if (document.isSigned()) {%>
                        <a href="documentServlet?document_id=<%=documentId%>&is_download=true">Download Signed PDF</a>
                    <% } %>
                </td>
                <td>
                     <% if (document.getOwnerId() == user.getUserId()) {%>
                    <form method="post" action="documentServlet">
                        <input type="hidden" name="document_id" value="<%=documentId%>"/>
                        <input type="submit" value="Delete" onclick="return deleteDocument(<%=document.getName()%>);"/></td>
                    </form>
                     <%}%>
            </tr>
            <% } %>
        </table>
    </body>
</html>
