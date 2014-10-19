<%-- 
    Document   : add_signer
    Created on : Oct 17, 2014, 9:43:01 PM
    Author     : johnanilc
--%>

<%@page import="classes.DocumentSigner"%>
<%@page import="classes.DocumentSigner.ParticipantSigner"%>
<%@page import="classes.User"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Add Signer</title>
    </head>
    <body>
        <h2>Enter Signer Details</h2>
        <% int documentId = Integer.parseInt(request.getParameter("document_id")); %>
        <form method="post" action="registrationServlet">
            <input type="hidden" name="add_signer" value="1"/>
            <input type="hidden" name="document_id" value="<%=documentId%>"/>
            <table width="100%">
                <tr>
                    <td>
                        Signer Name
                    </td>
                    <td>
                        <input type="text" name="signer_name" />
                    </td>
                </tr>
                <tr>
                    <td>
                        Signer Email
                    </td>
                    <td>
                        <input type="text" name="signer_email" />
                    </td>
                </tr>
                 <tr>
                    <td/>
                    <td>
                        <input type="submit" value="Add" />
                    </td>
                </tr>
                 <tr>
                    <td colspan="2">
                        <% if (request.getAttribute("errors") != null) {%>
                            <div><%=request.getAttribute("errors")%><div>
                        <% } %>
                    </td>
                <tr>
            </table>
        </form>
        <h2>Participant Signers</h2>
        <table width="100%">
            <tr>
                <td>
                    <b>#</b>
                </td>
                <td>
                    <b>Name</b>
                </td>
                <td>
                    <b>Email</b>
                </td>
                <td>
                    <b>Signed Date</b>
                </td>
            </tr>
            <% ArrayList<ParticipantSigner> signers = DocumentSigner.getParticipantSigners(documentId); %>
            <% int index = 1; %>
            <% for (ParticipantSigner signer : signers) { %>
            <tr>
                <td><%=index++%></td>
                <td><%=signer.getSignerName()%></td>
                <td><%=signer.getSignerEmail()%></td>
                <td><%=signer.getSignedDate()%></td>
            </tr>
            <% }%>
        </table>
        <br/><br/>
        <table width="100%">
            <tr><td colspan="3" align="center"><a href="userServlet">Go Back</a></td></tr>
        </table>
    </body>
</html>
