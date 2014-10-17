/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classes;

import Db.DbConnection;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author johnanilc
 */

public class DocumentSignature{
    private int documentSignatureId = 0;
    private int documentId = 0;
    private int signerId = 0;
    private int pageNumber = 0;
    private int signLocationX = 0;
    private int signLocationY = 0;
    
    public DocumentSignature(int documentSignatureId){
        this.documentSignatureId = documentSignatureId;
    }
    
    public DocumentSignature(int documentId, int signerId, int pageNumber, int signLocationX, int signLocationY){
        this.documentId = documentId;
        this.signerId = signerId;
        this.pageNumber = pageNumber;
        this.signLocationX = signLocationX;
        this.signLocationY = signLocationY;        
    }
    
    public int getDocumentSignatureId(){
        return documentSignatureId;
    }
    
    public int getDocumentId(){
        return documentId;
    }
    
    public int getSignerId(){
        return signerId;
    }
    
    public int getPageNumber(){
        return pageNumber;
    }
    
    public int getSignLocationX(){
        return signLocationX;
    }
    
    public int getSignLocationY(){
        return signLocationY;
    }
    
    public void setDocumentId(int documentId){
        this.documentId = documentId;
    }
    
    public void setSignerId(int signerId){
        this.signerId = signerId;
    }
    
    public void setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
    }
    
    public void setSignLocationX(int signLocationX){
        this.signLocationX = signLocationX;
    }
    
    public void setSignLocationY(int signLocationY){
        this.signLocationY = signLocationY;
    }
    
    public static void insertSignatures(int documentId, ArrayList<DocumentSignature> signatures, int signerId) throws Exception{
        // delete exisiting signatures of the document
        deleteSignatures(documentId, signerId);
        
        for (DocumentSignature signature : signatures){
            // insert the signature
            signature.insertSignature();
        }
        
        // update last signed date of the document
        updateLastSignedDate(documentId, new Date());
    }
    
    public static void deleteSignatures(int documentId, int signerId) throws Exception{
          try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM document_signature WHERE document_id = " + documentId;
            if (signerId > 0){
                sql += " and signer_id = " + signerId;
            }
            PreparedStatement statement = conn.prepareStatement(sql);
            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signatures deleted - " + documentId);
            }
        }
    }
    
    private void insertSignature() throws Exception{
         try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO document_signature (document_id, signer_id, page_number, sign_location_x, sign_location_y) values (?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, documentId);
            statement.setInt(2, signerId);
            statement.setInt(3, pageNumber);
            statement.setInt(4, signLocationX);
            statement.setInt(5, signLocationY);

            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document signature saved into database");
            }
        }
    }
    
    private static void updateLastSignedDate (int documentId, Date signedDate) throws Exception{
         try (Connection conn = DbConnection.getConnection()) {
            String sql = "update document set last_signed_date = ? where document_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setTimestamp(1, new java.sql.Timestamp(signedDate.getTime()));
            statement.setInt(2, documentId);

            // sends the statement to the database server
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("Document last signed date updated");
            }
        }
    }
    
    public static ByteArrayOutputStream getSignedDocument(Document document, User user) throws Exception{
        PdfReader reader = new PdfReader(document.getContent());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, os);

        // get user signature
        Image signatureImage = Image.getInstance(getImageBytes(user.getSignature()));
        try{
            for (DocumentSignature signature : getDocumentSignatures(document.getDocumentId())){
                int x = signature.getSignLocationX();
                int y = signature.getSignLocationY();
                signatureImage.setAbsolutePosition(x, y);
                PdfContentByte canvas = stamper.getOverContent(signature.getPageNumber());
                canvas.addImage(signatureImage);
            }
            return os;
        }finally{
            stamper.close();
        }
    }
    
    private static ArrayList<DocumentSignature> getDocumentSignatures(int documentId) throws Exception{
        ArrayList<DocumentSignature> signatures = new ArrayList<>();
         try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document_signature where document_id = " + documentId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    DocumentSignature signature = new DocumentSignature(rs.getInt("document_signature_id"));
                    signature.setDocumentId(rs.getInt("document_id"));
                    signature.setSignerId(rs.getInt("signer_id"));
                    signature.setPageNumber(rs.getInt("page_number"));
                    signature.setSignLocationX(rs.getInt("sign_location_x"));
                    signature.setSignLocationY(rs.getInt("sign_location_y"));
                    signatures.add(signature);
                }
            }
        }
        return signatures;
    }
    
    private static byte[] getImageBytes(InputStream signatureImage) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (signatureImage.read(buffer) != -1) { 
            out.write(buffer);
        }
        signatureImage.reset();
        return out.toByteArray();
    }
}