package servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import classes.Document;
import classes.User;
import classes.UserSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author johnanilc
 */
@WebServlet(urlPatterns = {"/userServlet"})
@MultipartConfig(maxFileSize = 16177215) //upload file upto 16 MB
public class userServlet extends HttpServlet {

    /**
     * Processes post requests for both HTTP <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = getUser(request);

            Part filePart;
            if (request.getPart("document") != null) {
                filePart = request.getPart("document");
                uploadDocument(request, filePart, user);
            } else if (request.getPart("signature") != null) {
                filePart = request.getPart("signature");
                uploadSignature(request, filePart, user);
            }

            response.sendRedirect("userServlet");
        } catch (Exception ex) {
            Logger.getLogger(userServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private User getUser(HttpServletRequest request){
        UserSession session = (UserSession) request.getSession().getAttribute("user_session");
        return session.getUser();
    }

    private void uploadSignature(HttpServletRequest request, Part filePart, User user) {
        try {
            user.setSignature(filePart.getInputStream());
            user.insertSignature();
            request.setAttribute("signature_upload", "Upload Successful");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            request.setAttribute("signature_upload", "Upload Error" + e.getMessage());
        }
    }

    private void uploadDocument(HttpServletRequest request, Part filePart, User user) throws Exception {
        Document document = new Document(getFileName(filePart), filePart.getInputStream(), new Date(), user.getUserId());
        try {
            document.insert();
            request.setAttribute("document_upload", "Upload Successful");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            request.setAttribute("document_upload", "Upload Error" + e.getMessage());
        }
    }

    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = getUser(request);
            if (request.getParameter("signature_image") != null) {
                // get signature image
                if (user.getSignature() != null) {
                  renderSignatureImage(user.getSignature(), response);
                }
            } else {
                // show user dashboard
                request.getRequestDispatcher("upload_document.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void renderSignatureImage(InputStream signature, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        int numRead;
        byte[] buf = new byte[1024];
        while ((numRead = signature.read(buf)) >= 0) {
            outStream.write(buf, 0, numRead);
        }
        signature.reset();

        byte[] imageBytes = outStream.toByteArray();
        response.setContentType("image/jpeg");
        response.setContentLength(imageBytes.length);
        response.getOutputStream().write(imageBytes);
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        for (String content : partHeader.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
