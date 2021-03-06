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
    private String signerIPAddress = null;

    public DocumentSigner(int documentSignerId, int documentId, int signerId, Date signedDate, String signerIPAddress) {
        this.documentSignerId = documentSignerId;
        this.documentId = documentId;
        this.signerId = signerId;
        this.signedDate = signedDate;
        this.signerIPAddress = signerIPAddress;
    }

    public DocumentSigner(int documentId, int signerId) {
        this.documentId = documentId;
        this.signerId = signerId;
    }

    public int getDocumentSignerId() {
        return documentSignerId;
    }

    public int getSignerId() {
        return signerId;
    }

    public String getSignerIPAddress() {
        return signerIPAddress;
    }

    public String getSignedDate() {
        if (signedDate == null) {
            return "";
        }

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return df.format(signedDate);
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    public void setSignerIPAddress(String signerIPAddress) {
        this.signerIPAddress = signerIPAddress;
    }

    public void insert() throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO document_signer (document_id, signer_id, signed_date, signer_ip_address) values (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, documentId);
            statement.setInt(2, signerId);
            if (signedDate != null) {
                statement.setTimestamp(3, new java.sql.Timestamp(signedDate.getTime()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }
            statement.setString(4, signerIPAddress);

            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signer saved into database");
            }
        }
    }
    
    public static void delete(int documentId, int signerId) throws Exception{
        // can only be done by the document owner
        
        // delete all signatures if any of the user against the document.
        DocumentSignature.deleteSignatures(documentId, signerId);
        
        // delete the signer against the document.
         try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM document_signer WHERE document_id = " + documentId + " and signer_id = " + signerId;
            PreparedStatement statement = conn.prepareStatement(sql);
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signer deleted for document" + documentId);
            }
        }
    }

    public static ArrayList<ParticipantSigner> getParticipantSigners(int documentId) throws Exception {
        ArrayList<ParticipantSigner> signers = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document_signer ds inner join user u on u.user_id = ds.signer_id where document_id = " + documentId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ParticipantSigner signer = new ParticipantSigner(rs.getString("user_name"), rs.getString("email_id"), getDocumentSigner(rs));
                    signers.add(signer);
                }
            }
        }

        return signers;
    }

    public static DocumentSigner getDocumentSigner(ResultSet rs) throws Exception {
        return new DocumentSigner(rs.getInt("document_signer_id"), rs.getInt("document_id"), rs.getInt("signer_id"), rs.getTimestamp("signed_date"), rs.getString("signer_ip_address"));
    }

    public static void deleteSigners(int documentId) throws Exception {
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

    public static int updateSignDetails(int documentId, int signerId, Date signedDate, String signerIPAddress) throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "update document_signer set signed_date = ?, signer_ip_address = ? where signer_id = ? and document_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setTimestamp(1, new java.sql.Timestamp(signedDate.getTime()));
            statement.setString(2, signerIPAddress);
            statement.setInt(3, signerId);
            statement.setInt(4, documentId);

            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document last signed date updated against signer");
            }

            return row;
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

        public int getSignerId() {
            return signer.getSignerId();
        }

        public String getSignedDate() {
            return signer.getSignedDate();
        }

        public String getSignerIPAddress() {
            return signer.getSignerIPAddress();
        }
    }
}
