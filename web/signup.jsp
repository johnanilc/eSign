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
        <form method="post" action="registrationServlet">
            <table width="100%">
                <tr>
                    <td width="10%" />
                    <td colspan="2">
                        <h3>Register User</h3>
                    </td>
                </tr>
                <tr>
                    <td width="10%"/>
                    <td width="10%">
                        User Name
                    </td>
                    <td>
                        <input type="text" size="30px" name="user_name" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        Email Id
                    </td>
                    <td>
                        <input type="text" size="30px" name="email_id" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        Password
                    </td>
                    <td>
                        <input type="password" size="30px" name="password" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        Confirm Password
                    </td>
                    <td>
                        <input type="password" size="30px" name="confirm_password" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td/>
                    <td>
                        <input type="submit" value="Save" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td colspan="2" style="color: red">
                        <% if (request.getAttribute("errors") != null) {%>
                            <div><%=request.getAttribute("errors")%><div>
                        <% } %>
                    </td>
                <tr>
            </table>
        </form>
    </body>
</html>
