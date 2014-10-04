package classes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Db.DbConnection;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
     * Processes post requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userId = 1; //Integer.parseInt(request.getParameter("user_id"));
        Part filePart = request.getPart("document");
        Document document = new Document(getFileName(filePart), filePart.getInputStream(), new Date(), userId);
        try{
            document.insert();
            request.setAttribute("message", "Upload Successful");
        }catch (Exception e){
            System.out.println(e.getMessage());
            request.setAttribute("message", "Upload Error" + e.getMessage());
        }
        
        response.sendRedirect("userServlet?user_id=1");
    }
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response){
      int userId = Integer.parseInt(request.getParameter("user_id"));
      try{
          ArrayList<Document> documents = getUserDocuments(userId);
          request.setAttribute("documents", documents);
          request.getRequestDispatcher("uploadDocument.jsp").forward(request, response);
      }catch(Exception e){
        System.out.println(e.getMessage());
      }
      
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
    
    private ArrayList<Document> getUserDocuments(int userId) throws Exception{
        ArrayList<Document> documents = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()){
            String sql = "select * from document where owner_id = " + userId + " order by document_id asc";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()){
                    Document document = new Document(rs.getInt("document_id"));
                    document.setName(rs.getString("name"));
                    document.setContent(rs.getBlob("content").getBinaryStream());
                    document.setUpdatedDate(rs.getTimestamp("date_updated"));
                    document.setOwnerId(rs.getInt("owner_id"));
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

    public static class Document {
        private int documentId = 0;
        private String name = "";
        private InputStream content = null;
        private Date updatedDate = null;
        private int ownerId = 0;
        
        public Document(int documentId) {
            this.documentId = documentId;
        }
        
        public Document(String name, InputStream content, Date updatedDate, int ownerId){
            this.name = name;
            this.updatedDate = updatedDate;
            this.ownerId = ownerId;
            this.content = content;
        }
        
        public int getDocumentId(){
            return documentId;
        }
        
        public String getName() {
            return name;
        }
        
        public InputStream getContent(){
            return content;
        }
        
        public void setContent(InputStream content){
            this.content = content;
        }
        
        public void setName(String name){
            this.name = name;
        }
        
        public String getUpdatedDate(){
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return df.format(updatedDate);
        }
        
        public void setUpdatedDate(Date updatedDate){
            this.updatedDate = updatedDate;
        }
        
        public int getOwnerId(){
            return ownerId;
        }
        
        public void setOwnerId(int ownerId){
            this.ownerId = ownerId;
        }
        
        public void insert() throws Exception {
            try(Connection conn = DbConnection.getConnection()) {
                String sql = "INSERT INTO document (name, content, date_updated, owner_id) values (?, ?, ?, ?)";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, name);
                
                if (content != null) {
                    // fetches input stream of the upload file for the blob column
                    statement.setBlob(2, content);
                }
                statement.setTimestamp(3, new java.sql.Timestamp(updatedDate.getTime()));
                statement.setInt(4, ownerId);
                // sends the statement to the database server
                int row = statement.executeUpdate();
                if (row > 0) {
                    System.out.println("File uploaded and saved into database");
                }
            }
        }
    }
    
    
    private static class User {
        private int userId = 0;
        private String firstName = "";
        private String middleName = "";
        private String lastName = "";
        private String emailId = "";
        private String password = "";
        
    }
    
    private static class DocumentSignature{
        private int documentId = 0;
        private int signerId = 0;
        private int pageNumber = 0;
        private int sign_location_x = 0;
        private int sign_location_y = 0;
    }
}
