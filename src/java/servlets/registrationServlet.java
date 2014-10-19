/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import db.DbConnection;
import classes.DocumentSigner;
import classes.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 * @author johnanilc
 */
@WebServlet(name = "registrationServlet", urlPatterns = {"/registrationServlet"})
public class registrationServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (request.getParameter("add_signer") != null) {
                addSigner(request, response);
                return;
            }

            registerNewUser(request, response);
        } catch (Exception ex) {
            Logger.getLogger(registrationServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addSigner(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String signerName = request.getParameter("signer_name");
        String signerEmail = request.getParameter("signer_email");
        int documentId = Integer.parseInt(request.getParameter("document_id"));

        String validations = validateSigner(signerName, signerEmail);
        if (validations.length() > 0) {
            request.setAttribute("errors", validations);
            response.sendRedirect("add_signer.jsp?document_id=" + documentId);
            return;
        }

        if (!isDuplicateEmail(signerEmail)) {
            // create new user
            User esignUser = new User(signerName, "", signerEmail, new Date());
            esignUser.insert();
        }

        User signer = User.getUser(signerEmail);

        // add user to document signers.
        DocumentSigner documentSigner = new DocumentSigner(documentId, signer.getUserId());
        documentSigner.insert();

        // refresh the page
        response.sendRedirect("add_signer.jsp?document_id=" + documentId);
    }

    private String validateSigner(String signerName, String signerEmail) {
        if (signerName == null || signerName.isEmpty()) {
            return "Please specify the signer name.";
        }

        if (signerEmail == null || signerEmail.isEmpty()) {
            return "Please specify the signer email.";
        }

        if (!EmailValidator.getInstance().isValid(signerEmail)) {
            return "Please specify a valid email address.";
        }

        return "";
    }

    private void registerNewUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String userName = request.getParameter("user_name");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");
        String emailId = request.getParameter("email_id");

        String validations = validateUser(userName, password, confirmPassword, emailId);

        if (validations.length() > 0) {
            request.setAttribute("errors", validations);
            response.sendRedirect("signup.jsp");
            return;
        }

        if (isDuplicateEmail(emailId)) {
            // email already exists

            // if the user has a password, then this is an existing user so do not allow duplicate user
            // if the user does not have password, then this user was created when a document was shared for signing, so update the user
            // with additional details.
            // load the user.
            User user = User.getUser(emailId);
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // user already exists
                request.setAttribute("errors", "Email address already exists.");
                response.sendRedirect("signup.jsp");
                return;
            }

           // update user.
           user.setPassword(password);
           user.update();
        }else{
            // create user.
            User esignUser = new User(userName, password, emailId, new Date());
            esignUser.insert();
        }

        response.sendRedirect("login.jsp");
    }

    private String validateUser(String userName, String password, String confirmPassword, String emailId) {
        if (userName == null || userName.length() == 0) {
            return "Please specify a user name.";
        }

        if (password == null || password.length() == 0) {
            return "Please specify a password.";
        }

        if (confirmPassword == null || confirmPassword.length() == 0) {
            return "Please confirm password.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        if (emailId == null || emailId.length() == 0) {
            return "Please specify an email address.";
        }

        if (!EmailValidator.getInstance().isValid(emailId)) {
            return "Please specify a valid email address.";
        }

        return "";
    }

    private boolean isDuplicateEmail(String emailId) throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from user where email_id = '" + emailId + "'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
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
        processRequest(request, response);
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
        processRequest(request, response);
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
