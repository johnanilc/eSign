<%-- 
    Document   : create_signature
    Created on : Oct 19, 2014, 7:36:43 PM
    Author     : johnanilc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Signature Creation</title>
        <link href="css/jquery.signaturepad.css" rel="stylesheet">
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js"></script>
    </head>
    <body>
        <h1>Create Signature</h1>
        <table width="100%">
            <tr>
                <td align="center">
                    <form method="post" action="userServlet" class="sigPad">
                        <label for="name">Print your name</label>
                        <input type="text" name="signature_name" id="name" class="name">
                        <p class="typeItDesc">Review your signature</p>
                        <p class="drawItDesc">Draw your signature</p>
                        <ul class="sigNav">
                            <li class="typeIt"><a href="#type-it" class="current">Type It</a></li>
                            <li class="drawIt"><a href="#draw-it" >Draw It</a></li>
                            <li class="clearButton"><a href="#clear">Clear</a></li>
                        </ul>
                        <div class="sig sigWrapper">
                            <div class="typed"></div>
                            <canvas class="pad" width="198" height="55"></canvas>
                            <input type="hidden" name="signature_output" class="output">
                        </div>
                        <button type="submit">Create Signature</button>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="center">
                    <a href="userServlet">Go back</a>
                </td>
            </tr>
        </table>

        <script src="js/jquery.signaturepad.js"></script>
        <script>
            $(document).ready(function() {
                $('.sigPad').signaturePad();
            });
        </script>
        <script src="js/json2.min.js"></script>
    </body>
</html>
