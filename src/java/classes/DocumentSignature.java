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
import java.util.ArrayList;

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
    
    public static void insertSignatures(int documentId, ArrayList<DocumentSignature> signatures) throws Exception{
        // delete exisiting signatures of the document
        deleteSignatures(documentId);
        
        for (DocumentSignature signature : signatures){
            signature.insertSignature();
        }
    }
    
    public static void deleteSignatures(int documentId) throws Exception{
          try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM document_signature WHERE document_id = " + documentId;
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
    
    public static ByteArrayOutputStream getSignedDocument(Document document, User user, ArrayList<DocumentSignature> signatures) throws Exception{
        PdfReader reader = new PdfReader(document.getContent());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, os);

        Image signatureImage = Image.getInstance(getImageBytes(user.getSignature()));
        try{
            for (DocumentSignature signature : signatures){
                int x = signature.getSignLocationX();
                int y = signature.getSignLocationY();
                signatureImage.setAbsolutePosition(x, y);
                PdfContentByte canvas = stamper.getOverContent(signature.getPageNumber()+1);
                canvas.addImage(signatureImage);
            }
            return os;
        }finally{
            stamper.close();
        }
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