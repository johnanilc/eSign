<%-- 
    Document   : signDocument
    Created on : Aug 24, 2014, 4:32:17 PM
    Author     : johnanilc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <style>
            .signature {
                position: absolute;
                left: 80px;
                top: 200px;
                width: 200px;
                background: none;
                border: 2px solid rgba(0,0,0,0.5);
                border-radius: 4px; padding: 8px;
            }
        </style>
        <script type="text/javascript" lang="javascript">
            function increaseZIndex(pages){
                for (i=0; i<pages; i++){
                    var index = parseInt(document.getElementById("page"+i).style.zIndex);
                    if (index === pages){
                        // rollover z-index based on number of pages
                        index = 1;
                    }else{
                        index = index + 1;
                    }
                    document.getElementById("page"+i).style.zIndex = index;
                }
                
                // hide next link on reaching last page, i.e. when last page has max z-index.
                if (index === pages){
                    document.getElementById("next").style.visibility = 'hidden';
                }else{
                    document.getElementById("next").style.visibility = 'visible';
                }
                
                // enable prev link.
                document.getElementById("prev").style.visibility = 'visible';
            }
            
            function decreaseZIndex(pages){
                for (i=pages-1; i>=0; i--){
                    var index = parseInt(document.getElementById("page"+i).style.zIndex);
                    if (index === 1){
                        index = pages;
                    }else{
                        index = index - 1;
                    }
                    document.getElementById("page"+i).style.zIndex = index;
                }
                
                // hide prev link on reaching first page, i.e. when first page has max z-index.
                if (index === pages){
                    document.getElementById("prev").style.visibility = 'hidden';
                }else{
                    document.getElementById("prev").style.visibility = 'visible';
                }
                
                // enable next link.
                document.getElementById("next").style.visibility = 'visible';
            }
            
            function allowDrop(ev) {
                ev.preventDefault();
            }
            
            function drag(ev) {
                var style = window.getComputedStyle(ev.target, null);
                ev.dataTransfer.setData("text/plain", ev.target.id + "," + (parseInt(style.getPropertyValue("left")) - ev.clientX) + "," + (parseInt(style.getPropertyValue("top")) - ev.clientY));
            }
            
            function drop(ev, id) {
                ev.preventDefault();
                var data = ev.dataTransfer.getData("text/plain").split(',');
                var signatureId = data[0];
                var signature = document.getElementById(signatureId);
                var left = ev.clientX + parseInt(data[1]);
                var top = ev.clientY + parseInt(data[2]);
                if (signatureId.substring(0,6) === 'esign_'){
                    // drop the original sign when moving the placed signature
                    signature.style.left = left + 'px';
                    signature.style.top = top + 'px';
                    
                    // update signature location on server
                    setSignatureLocation(signatureId, id, left, top);
                }else{
                    // drop a copy when placing a new signature on the document
                    var sign = document.createElement("IMG");
                    // signature id = esign_<page>_<timestamp>
                    var timestamp = new Date().getUTCMilliseconds();
                    sign.id = 'esign_' + parseInt(id) + "_" + timestamp;
                    sign.src = signature.src;
                    sign.style.position = 'absolute';
                    sign.style.left = left + 'px';
                    sign.style.top = top + 'px';
                    sign.addEventListener('dragstart', function() {drag(event);}, false);
                    ev.target.parentNode.appendChild(sign);
                    
                    // set signature location on server
                    setSignatureLocation(sign.id, id, left, top);
                }
            }
            
            function setSignatureLocation(signatureId, page, left, top){
                 var xmlHttp = new XMLHttpRequest();
                 xmlHttp.open("POST", "eSignServlet", true);
                 xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
                 xmlHttp.send("signature_id=" + signatureId + "&page=" + page + "&left=" + left + "&top=" + top);
            }
        </script>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sign Document</title>
    </head>
    <body>
        <% int documentId = Integer.parseInt(request.getParameter("document_id")); %>
        <% int pages = Integer.parseInt(request.getParameter("pages")); %>
        <table width="100%">
            <tr>
                <td width="25%" align="right">
                    <img id="signature" class="signature" src="userServlet?signature_image=1" draggable="true" ondragstart="drag(event)"/>
                </td>
                <td width="50%" align="right" valign="top">
                    <table width="100%">
                        <tr>
                            <td align="center">
                                <a href="#" id="prev" onclick="decreaseZIndex(<%=pages%>)" style="visibility: hidden">Previous</a>
                                <a href="#" id="next" onclick="increaseZIndex(<%=pages%>)">Next</a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <% for (int i = 0; i < pages; i++) {%>
                                <% int zindex = pages-i; %>
                                    <div id="page<%=i%>" style="position:absolute; z-index:<%=zindex%>" ondrop="drop(event, <%=i%>)" ondragover="allowDrop(event)">
                                        <img id="imgpage<%=i%>" style="border:1px solid black" src="eSignServlet?document_id=<%=documentId%>&page_num=<%=i%>" width="60%" height="50%" />
                                    </div>
                                <%} %>
                            </td>
                        </tr>
                    </table>
                </td>
                <td width="25%">
                    <form name="signDocument" method="post" action="eSignServlet">
                        <input type="hidden" name="document_id" value="<%=documentId%>" />
                        <input type="submit" value="Sign" />
                    </form>
                </td>
            </tr>
         </table>
    </body>
</html>