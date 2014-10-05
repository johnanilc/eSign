<%-- 
    Document   : signup
    Created on : Oct 4, 2014, 7:16:49 PM
    Author     : johnanilc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>New User Registration</title>
    </head>
    <body>
        <h1>Register User</h1>
        <form method="post" action="userServlet" enctype="multipart/form-data">
            <table width="100%">
                <tr>
                    <td>
                        User Name
                    </td>
                    <td>
                        <input type="text" id="user_name" />
                    </td>
                </tr>
                <tr>
                    <td>
                        Email Id
                    </td>
                    <td>
                        <input type="text" id="email_id" />
                    </td>
                </tr>
                <tr>
                    <td>
                        Password
                    </td>
                    <td>
                        <input type="password" id="password" />
                    </td>
                </tr>
                <tr>
                    <td>
                        Confirm Password
                    </td>
                    <td>
                        <input type="password" id="confirm_password" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        <input type="submit" value="Save">
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
    </body>
</html>
