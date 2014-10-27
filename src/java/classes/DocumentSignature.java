/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import db.DbConnection;
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
public class DocumentSignature {

    private static final float SIGNATURE_IMAGE_WIDTH = 140.0f;
    private static final float SIGNATURE_IMAGE_HEIGHT = 25.0f;

    private int documentSignatureId = 0;
    private int documentId = 0;
    private int signerId = 0;
    private int pageNumber = 0;
    private int signLocationX = 0;
    private int signLocationY = 0;

    public DocumentSignature(int documentSignatureId) {
        this.documentSignatureId = documentSignatureId;
    }

    public DocumentSignature(int documentId, int signerId, int pageNumber, int signLocationX, int signLocationY) {
        this.documentId = documentId;
        this.signerId = signerId;
        this.pageNumber = pageNumber;
        this.signLocationX = signLocationX;
        this.signLocationY = signLocationY;
    }

    public int getDocumentSignatureId() {
        return documentSignatureId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public int getSignerId() {
        return signerId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getSignLocationX() {
        return signLocationX;
    }

    public int getSignLocationY() {
        return signLocationY;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public void setSignerId(int signerId) {
        this.signerId = signerId;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setSignLocationX(int signLocationX) {
        this.signLocationX = signLocationX;
    }

    public void setSignLocationY(int signLocationY) {
        this.signLocationY = signLocationY;
    }

    public static void insertSignatures(int documentId, ArrayList<DocumentSignature> signatures, int signerId, String signerIPAddress) throws Exception {
        // delete exisiting signatures of the document
        deleteSignatures(documentId, signerId);

        for (DocumentSignature signature : signatures) {
            // insert the signature
            signature.insertSignature();
        }

        Date signedDate = new Date();

        // update last signed date of the document
        Document.updateLastSignedDate(documentId, signedDate);

        // update signed date & signer IP address against the document signer
        int updateCount = DocumentSigner.updateSignDetails(documentId, signerId, signedDate, signerIPAddress);

        if (updateCount == 0) {
            // insert document signer
            DocumentSigner signer = new DocumentSigner(documentId, signerId);
            signer.setSignedDate(signedDate);
            signer.setSignerIPAddress(signerIPAddress);
            signer.insert();
        }
    }

    public static void deleteSignatures(int documentId, int signerId) throws Exception {
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "DELETE FROM document_signature WHERE document_id = " + documentId;
            if (signerId > 0) {
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

    private void insertSignature() throws Exception {
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

    public static ByteArrayOutputStream getSignedDocument(Document document) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(document.getContent());
        PdfStamper stamper = new PdfStamper(reader, os);

        // get all signers of the document
        ArrayList<User> signers = DocumentSignature.getDocumentSigners(document.getDocumentId());

        try {
            for (User signer : signers) {
                if (signer.getSignature() == null) {
                    continue;
                }

                // get user signature
                Image signatureImage = Image.getInstance(getImageBytes(signer.getSignature()));

                for (DocumentSignature signature : getDocumentSignatures(document.getDocumentId(), signer.getUserId())) {
                    int x = signature.getSignLocationX();
                    int y = signature.getSignLocationY();
                    //signatureImage.setAbsolutePosition(x, y);
                    PdfContentByte canvas = stamper.getOverContent(signature.getPageNumber());

                    //System.out.println("bottom: " + canvas.getPdfDocument().bottom());
                    //System.out.println("top: " + canvas.getPdfDocument().top());
                    //System.out.println("left: " + canvas.getPdfDocument().left());
                    //System.out.println("right: " + canvas.getPdfDocument().right());

                    //canvas.addImage(signatureImage);
                    canvas.addImage(signatureImage, SIGNATURE_IMAGE_WIDTH, 0, 0, SIGNATURE_IMAGE_HEIGHT, x, y);
                }
            }

            return os;
        } finally {
            stamper.close();
        }
    }

    public static ArrayList<User> getDocumentSigners(int documentId) throws Exception {
        ArrayList<User> signers = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select distinct signer_id from document_signature where document_id = " + documentId;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int signerId = rs.getInt("signer_id");
                    User signer = new User(signerId);
                    signers.add(signer);
                }
            }
        }
        return signers;
    }

    private static ArrayList<DocumentSignature> getDocumentSignatures(int documentId, int userId) throws Exception {
        ArrayList<DocumentSignature> signatures = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "select * from document_signature where document_id = " + documentId + " and signer_id = " + userId;
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

    private static byte[] getImageBytes(InputStream signatureImage) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (signatureImage.read(buffer) != -1) {
            out.write(buffer);
        }
        signatureImage.reset();
        return out.toByteArray();
    }
}
