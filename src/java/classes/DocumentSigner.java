/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import db.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author johnanilc
 */
public class DocumentSigner {
    private int documentSignerId = 0;
    private int documentId = 0;
    private int signerId = 0;
    private Date signedDate = null;

    public DocumentSigner(int documentSignerId, int documentId, int signerId, Date signedDate) {
        this.documentSignerId = documentSignerId;
        this.documentId = documentId;
        this.signerId = signerId;
        this.signedDate = signedDate;
    }

    public DocumentSigner(int documentId, int signerId) {
        this.documentId = documentId;
        this.signerId = signerId;
    }

    public int getDocumentSignerId() {
        return documentSignerId;
    }
    
    public int getSignerId(){
        return signerId;
    }

    public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    public void insert() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO document_signer (document_id, signer_id, signed_date) values (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, documentId);
            statement.setInt(2, signerId);
            if (signedDate != null) {
                statement.setTimestamp(3, new java.sql.Timestamp(signedDate.getTime()));
            }else{
                statement.setNull(3, java.sql.Types.DATE);
            }

            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signer saved into database");
            }
        }
    }

    public static ArrayList<ParticipantSigner> getParticipantSigners(int documentId) throws Exception {
        ArrayList<ParticipantSigner> signers = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document_signer ds inner join user u on u.user_id = ds.signer_id where document_id = " + documentId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    ParticipantSigner signer = new ParticipantSigner(rs.getString("user_name"), rs.getString("email_id"), getDocumentSigner(rs));
                    signers.add(signer);
                }
            }
        }

        return signers;
    }

    public static DocumentSigner getDocumentSigner(ResultSet rs) throws Exception {
        return new DocumentSigner(rs.getInt("document_signer_id"), rs.getInt("document_id"), rs.getInt("signer_id"), rs.getDate("signed_date"));
    }
    
    public static void deleteSigners(int documentId) throws Exception{
          try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM document_signer WHERE document_id = " + documentId;
            PreparedStatement statement = conn.prepareStatement(sql);
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signers deleted - " + documentId);
            }
        }
    }

    public static class ParticipantSigner {

        private String signerName = "";
        private String signerEmail = "";
        private DocumentSigner signer = null;

        public ParticipantSigner(String signerName, String signerEmail, DocumentSigner signer) {
            this.signerName = signerName;
            this.signerEmail = signerEmail;
            this.signer = signer;
        }

        public String getSignerName() {
            return signerName;
        }

        public String getSignerEmail() {
            return signerEmail;
        }
        
        public int getSignerId(){
            return signer.getSignerId();
        }

        public String getSignedDate() {
            if (signer.getSignedDate() == null){
                return "";
            }
            
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return df.format(signer.getSignedDate());
        }
    }
}
