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
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author johnanilc
 */
public class Document {
    private int documentId = 0;
    private String name = "";
    private InputStream content = null;
    private Date updatedDate = null;
    private Date lastSignedDate = null;
    private int ownerId = 0;

    public Document(int documentId) {
        this.documentId = documentId;
    }

    public Document(String name, InputStream content, Date updatedDate, int ownerId) {
        this.name = name;
        this.updatedDate = updatedDate;
        this.ownerId = ownerId;
        this.content = content;
    }

    public int getDocumentId() {
        return documentId;
    }

    public String getName() {
        return name;
    }

    public InputStream getContent() {
        return content;
    }
    
    public void setContent(InputStream content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatedDate() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return df.format(updatedDate);
    }
    
    public String getLastSignedDate(){
        if (lastSignedDate == null){
            return "";
        }
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return df.format(lastSignedDate);
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setLastSignedDate(Date lastSignedDate){
        this.lastSignedDate = lastSignedDate;
    }
    
    public boolean isSigned(){
        return (lastSignedDate != null);
    }

    public void insert() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
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
    
    public static void delete(int documentId) throws Exception{
        // delete all signatures of all users in the document
        DocumentSignature.deleteSignatures(documentId, 0);
        
        // TODO: delete all document signers
        
        // delete the document
        try (Connection conn = DbConnection.getConnection()) {
          String sql = "DELETE FROM document WHERE document_id = " + documentId;
          PreparedStatement statement = conn.prepareStatement(sql);
          // sends the statement to the database server
          int row = statement.executeUpdate();
          if (row > 0) {
              System.out.println("Document deleted - " + documentId);
          }
        }
    }

    public static Document getDocument(int documentId) throws Exception {
        Document document = new Document(documentId);
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document where document_id = " + documentId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    document.setName(rs.getString("name"));
                    document.setContent(rs.getBlob("content").getBinaryStream());
                    document.setUpdatedDate(rs.getTimestamp("date_updated"));
                    document.setOwnerId(rs.getInt("owner_id"));
                }
            }
        }

        return document;
    }
}
