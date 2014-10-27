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
        <table width="100%" border="0">
            <tr>
                <td />
                <td>
                    <h2>E-Sign Dashboard</h2>
                </td>
                <td align="right"><a href="login.jsp">Logout</a></td>
                <td/>
            </tr>
        </table>
        <% User user = ((UserSession)request.getSession().getAttribute("user_session")).getUser(); %>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0" width="100%">
                <tr>
                    <td width="1%"/>
                    <td width="30%">
                         <h3>Upload Signature</h3>
                    </td>
                    <td>
                        (<a href="create_signature.jsp">Create Signature</a>)
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>Signature&nbsp;&nbsp;<input type="file" name="signature" accept="image/*"/></td>
                </tr>
                <tr>
                    <td/>
                    <td colspan="2">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("signature_upload") != null) { %>
                <tr>
                    <td/>
                    <td colspan="2"><div><%=request.getAttribute("signature_upload")%><div></td>
                </tr>
                <% } %>
            </table>
        </form>
        <% if(user.getSignature() != null) {%>
            <table width="100%" border="0">
                <tr>
                    <td width="1%"/>
                    <td>
                        <h3>Uploaded Signature</h3>
                    </td>
                    <td/>
                </tr>
                <tr>
                    <td/>
                    <td>
                        <img src="userServlet?signature_image=<%=System.currentTimeMillis()%>" />
                    </td>
                    <td/>
                </tr>
            </table>
        <% } %>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table border="0" width="100%">
                <tr>
                    <td width="1%"/>
                    <td>
                        <h3>Upload Document</h3>
                    </td>
                    <td/>
                </tr>
                <tr>
                    <td/>
                    <td>Document&nbsp;&nbsp;<input type="file" name="document" accept="application/pdf"/></td>
                </tr>
                <tr>
                    <td/>
                    <td colspan="2">
                        <input type="submit" value="Save">
                    </td>
                </tr>
                <% if (request.getAttribute("document_upload") != null) { %>
                <tr>
                    <td/>
                    <td colspan="2"><div><%=request.getAttribute("document_upload")%><div></td>
                </tr>
                <% } %>
            </table>
        </form>
        <% ArrayList<DocumentDetail> documents = User.getUserDocuments(user.getUserId(), false); %>
        <table align="center" width="100%" border="0">
            <tr>
                <td width="1%"/>
                <td colspan="9">
                    <h3>Documents</h3>    
                </td>
            </tr>
            <tr>
                <td/>
                <td width="2%">
                    <b>No.</b>
                </td>
                <td width="30%">
                    <b>Name</b>
                </td>
                <td width="10%">
                    <b>Owner</b>
                </td>
                <td width="13%">
                    <b>Created</b>
                </td>
                <td width="13%">
                    <b>Last Signed</b>
                </td>
                <td width="4%">
                </td>
                <td width="13%">
                </td>
                <td width="5%">
                </td>
                <td width="10%">
                </td>
                <td/>
            </tr>
            <% for (int idx=0; idx<documents.size(); idx++) { %>
            <% DocumentDetail document = documents.get(idx); %>
            <% int documentId = document.getDocumentId(); %>
            <tr>
                <td/>
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
                    <a href="add_signer.jsp?document_id=<%=documentId%>">View/Add Signers</a>
                    <%}%>
                </td>
                <td>
                     <% if (document.getOwnerId() == user.getUserId()) {%>
                    <form method="post" action="documentServlet">
                        <input type="hidden" name="document_id" value="<%=documentId%>"/>
                        <input type="submit" value="Delete" onclick="return deleteDocument(<%=document.getName()%>);"/>
                    </form>
                     <% } %>
                </td>
                <td>
                     <% if (document.isSigned()) {%>
                        <a href="documentServlet?document_id=<%=documentId%>&is_download=true">Download</a>
                    <% } %>
                </td>
                <td/>
            </tr>
            <% } %>
        </table>
        <% ArrayList<DocumentDetail> sharedDocuments = User.getUserDocuments(user.getUserId(), true); %>
        <table align="center" width="100%" border="0">
            <tr>
                <td width="1%"/>
                <td colspan="9">
                    <h3>Shared Documents</h3>    
                </td>
            </tr>
            <tr>
                <td/>
                <td width="2%">
                    <b>No.</b>
                </td>
                <td width="30%">
                    <b>Name</b>
                </td>
                <td width="10%">
                    <b>Owner</b>
                </td>
                <td width="13%">
                    <b>Created</b>
                </td>
                <td width="13%">
                    <b>Last Signed</b>
                </td>
                <td width="4%">
                </td>
                <td width="5%">
                </td>
                <td/>
            </tr>
            <% for (int idx=0; idx<sharedDocuments.size(); idx++) { %>
            <% DocumentDetail document = sharedDocuments.get(idx); %>
            <% int documentId = document.getDocumentId(); %>
            <tr>
                <td/>
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
                     <% if (document.isSigned()) {%>
                        <a href="documentServlet?document_id=<%=documentId%>&is_download=true">Download</a>
                    <% } %>
                </td>
                <td/>
            </tr>
            <% } %>
        </table>
    </body>
</html>
