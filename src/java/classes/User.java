/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import db.DbConnection;
import classes.Document.DocumentDetail;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public User() {

    }

    public User(String userName, String password, String emailId, Date createdDate) {
        this.userName = userName;
        this.password = password;
        this.emailId = emailId;
        this.createdDate = createdDate;
    }

    public User(int userId) {
        try {
            this.userId = userId;
            loadUser();
        } catch (Exception ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return emailId;
    }

    public String getPassword() {
        return password;
    }

    public InputStream getSignature() {
        return signature;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setSignature(InputStream signature) {
        this.signature = signature;
    }

    private void loadUser() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from user where user_id = " + userId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    loadUser(rs);
                }
            }
        }
    }

    private void loadUser(ResultSet rs) throws Exception {
        setUserId(rs.getInt("user_id"));
        setUserName(rs.getString("user_name"));
        setPassword(rs.getString("password"));
        setCreatedDate(rs.getDate("created_date"));
        setSignature(rs.getBinaryStream("signature"));
        setEmailId(rs.getString("email_id"));
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

    public void update() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "update user set user_name = ?, password = ?, email_id = ?, created_date = ? where user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, emailId);
            statement.setTimestamp(4, new java.sql.Timestamp(createdDate.getTime()));
            statement.setInt(5, userId);
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("User updated in database");
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
                System.out.println("Signature saved into database for user - " + userName);
            }
        }
    }

    public static User getUser(String emailId) throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from user where email_id = '" + emailId + "'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return getUser(rs);
                }
            }
        }

        return null;
    }

    public static User getUser(String userName, String password) throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from user where email_id = '" + userName + "' and password = '" + password + "'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return getUser(rs);
                }
            }
        }
        return null;
    }

    private static User getUser(ResultSet rs) throws Exception {
        User user = new User();
        user.loadUser(rs);
        return user;
    }

    public static ArrayList<DocumentDetail> getUserDocuments(int userId, boolean isShared) throws Exception {
        ArrayList<DocumentDetail> documents = new ArrayList<>();

        //get own documents & documents shared for signing from document_signer but exclude owned documents.
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "";
            if (!isShared) {
                sql = "select d.document_id, d.name, d.content, d.date_updated, d.owner_id, d.last_signed_date, u.user_name from document d inner join user u on u.user_id = d.owner_id where owner_id = " + userId;
            } else {
                sql = "select d.document_id, d.name, d.content, d.date_updated, d.owner_id, d.last_signed_date, u.user_name from document_signer ds inner join document d on ds.document_id = d.document_id inner join user u on u.user_id = d.owner_id where ds.signer_id = " + userId + " and d.owner_id <> " + userId;
            }
            sql += " order by document_id desc";
            
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Document document = new Document(rs.getInt("document_id"));
                    document.setName(rs.getString("name"));
                    document.setContent(rs.getBlob("content").getBinaryStream());
                    document.setUpdatedDate(rs.getTimestamp("date_updated"));
                    document.setOwnerId(rs.getInt("owner_id"));
                    document.setLastSignedDate(rs.getTimestamp("last_signed_date"));
                    documents.add(new DocumentDetail(rs.getString("user_name").toUpperCase(), document));
                }
            }
        }

        return documents;
    }
}
