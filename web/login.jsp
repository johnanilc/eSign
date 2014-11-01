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
        <form method="post" action="loginServlet">
            <table width="100%">
                <tr>
                    <td/>
                    <td colspan="2"> <h3>Login</h3></td>
                </tr>
                <tr>
                    <td width="10%"/>
                    <td align="left" width="10%">
                        Email
                    </td>
                    <td>
                        <input type="text" size="30px" name="user_name" />
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td align="left">
                        Password
                    </td>
                    <td>
                        <input type="password" size="30px" name="password" />
                    </td>
                </tr>
                <tr>
                    <td colspan="2"/>
                    <td>
                        <input type="submit" value="Login">
                    </td>
                </tr>
                <tr>
                    <td colspan="2"/>
                    <td>
                        <a href="signup.jsp">New User?</a>
                    </td>
                    <td/>
                </tr>
                 <tr>
                    <td/>
                    <td colspan="2" style="color: red"><% if (request.getAttribute("message") != null) {%><div><%=request.getAttribute("message")%></div><% }%></td>
                </tr>
            </table>
        </form>
    </body>
</html>
