/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import Db.DbConnection;
import classes.User;
import classes.UserSession;
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
import javax.servlet.http.HttpSession;

/**
 *
 * @author johnanilc
 */
@WebServlet(name = "loginServlet", urlPatterns = {"/loginServlet"})
public class loginServlet extends HttpServlet {

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
            // verify password
            String userName = request.getParameter("user_name");
            String password = request.getParameter("password");
            User user = getUser(userName, password);
            if (user != null){
                // navigate the user to the dashboard console.
                HttpSession session = request.getSession();
                session.setMaxInactiveInterval(300);
                session.setAttribute("user_session", new UserSession(user, session.getId(), new Date()));
                request.getRequestDispatcher("userServlet").forward(request, response);
            }
            
            // invalid user
            request.setAttribute("message", "Invalid user name or password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } catch (Exception ex) {
            Logger.getLogger(loginServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private User getUser(String userName, String password) throws Exception{
        try (Connection conn = DbConnection.getConnection()){
            String sql = "select * from user where userName = '" + userName + "' and password = '" + password + "'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if(rs.next()){
                  return new User(rs.getString("user_name"), rs.getString("password"), rs.getString("email_id"), rs.getDate("created_date"));
                }
            }
        }
        return null;
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
