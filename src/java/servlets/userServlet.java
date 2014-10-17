package servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Db.DbConnection;
import classes.Document;
import classes.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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
            User user = User.getUser(request);

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
            User user = User.getUser(request);
            if (request.getParameter("signature_image") != null) {
                // get signature image
                if (user.getSignature() != null) {
                  renderSignatureImage(user.getSignature(), response);
                }
            } else {
                // show user dashboard
                ArrayList<Document> documents = getUserDocuments(user.getUserId());
                request.setAttribute("documents", documents);
                request.getRequestDispatcher("uploadDocument.jsp").forward(request, response);
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

    private ArrayList<Document> getUserDocuments(int userId) throws Exception {
        ArrayList<Document> documents = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document where owner_id = " + userId + " order by document_id desc";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Document document = new Document(rs.getInt("document_id"));
                    document.setName(rs.getString("name"));
                    document.setContent(rs.getBlob("content").getBinaryStream());
                    document.setUpdatedDate(rs.getTimestamp("date_updated"));
                    document.setOwnerId(rs.getInt("owner_id"));
                    document.setLastSignedDate(rs.getTimestamp("last_signed_date"));
                    documents.add(document);
                }
            }
        }
        return documents;
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
