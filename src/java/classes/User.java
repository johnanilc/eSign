/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import Db.DbConnection;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author johnanilc
 */
public class User {
    private int userId = 0;
    private String userName = "";
    private String emailId = "";
    private String password = "";
    private Date createdDate = null;
    private InputStream signature = null;

    public User(String userName, String password, String emailId, Date createdDate) {
        this.userName = userName;
        this.password = password;
        this.emailId = emailId;
        this.createdDate = createdDate;
    }
    
    public User(int userId){
        this.userId = userId;
    }
    
    public int getUserId(){
        return userId;
    }
    
    public InputStream getSignature(){
        return signature;
    }
    
    public void setUserName(String userName){
        this.userName = userName;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void setEmailId(String emailId){
        this.emailId = emailId;
    }
    
    public void setCreatedDate(Date createdDate){
        this.createdDate = createdDate;
    }
    
    public void setSignature(InputStream signature){
        this.signature = signature;
    }

    public void insert() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO user (user_name, password, email_id, created_date) values (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, emailId);
            statement.setTimestamp(4, new java.sql.Timestamp(createdDate.getTime()));
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("User saved into database");
            }
        }
    }

    public void insertSignature() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "UPDATE user SET signature = ? where user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setBlob(1, signature);
            statement.setInt(2, userId);
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Signature saved into database");
            }
        }
    }
    
    public static User getUser(HttpServletRequest request) {
        UserSession session = (UserSession) request.getSession().getAttribute("user_session");
        return session.getUser();
    }
}
