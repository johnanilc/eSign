<%-- 
    Document   : login
    Created on : Oct 5, 2014, 2:07:29 PM
    Author     : johnanilc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>E-SIGN DOCUMENT SERVICE</title>
    </head>
    <body>
        <h1>Login</h1>
        <form method="post" action="loginServlet">
            <table width="100%">
                <tr>
                    <td>
                        Email
                    </td>
                    <td>
                        <input type="text" name="user_name" />
                    </td>
                </tr>
                <tr>
                    <td>
                        Password
                    </td>
                    <td>
                        <input type="password" name="password" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        <input type="submit" value="Login">
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><% if (request.getAttribute("message") != null) {%><div><%=request.getAttribute("message")%></div><% }%></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <a href="signup.jsp">New User?</a>
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>
