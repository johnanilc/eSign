package servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import classes.Document;
import classes.DocumentSignature;
import classes.User;
import classes.UserSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 *
 * @author johnanilc
 */
@WebServlet(urlPatterns = {"/eSignServlet"})
public class eSignServlet extends HttpServlet {

//    private static final int pageImageWidth = 1224;
//    private static final int pageImageHeight = 1584;
//    private static final float pageImageWidthFactor = (float) 0.6;
//    private static final float pageImageHeightFactor = (float) 0.5;
//
//    private static final int pdfPageHeight = 770;
//    private static final int pdfPageWidth = 523;
    
    private static final int PDF_PAGE_BOTTOM_LEFT_X_COORDINATE = 36;
    private static final int PDF_PAGE_TOP_RIGHT_Y_COORDINATE = 806;
    
    private static HashMap<String, Signature> signatures = new HashMap<>();

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String signatureId = request.getParameter("signature_id");
            if (signatureId != null) {
                // create/update signature location
                Signature sign = signatures.get(signatureId);
                if (sign == null) {
                    int page = Integer.parseInt(request.getParameter("page"));
                    sign = new Signature(signatureId, page);
                    signatures.put(signatureId, sign);
                    System.out.println("Created New Signature: " + signatureId);
                }
                int left = Integer.parseInt(request.getParameter("left"));
                int top = Integer.parseInt(request.getParameter("top"));
                sign.setLeft(left);
                sign.setTop(top);
                System.out.println("Updated Signature: " + signatureId);
                return;
            }

            if (!signatures.isEmpty()) {
                // create pdf with signatures.
                int documentId = Integer.parseInt(request.getParameter("document_id"));
                Document document = Document.getDocument(documentId);
                signDocument(document, getUser(request), getIpAddr(request));
            }

            response.sendRedirect("userServlet");
        } catch (Exception ex) {
            Logger.getLogger(eSignServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int documentId = Integer.parseInt(request.getParameter("document_id"));
            String pageNumber = request.getParameter("page_num");

            Document document = Document.getDocument(documentId);
            if (pageNumber != null) {
                // render pdf page as image.
                int pageNum = Integer.parseInt(pageNumber);
                renderPdfPagesAsImages(response, document, pageNum);
            } else {
                // clear signatures if any set from previous load.
                signatures = new HashMap<>();

                // show document for signing
                int pages = getPageCount(document);
                response.sendRedirect("sign_document.jsp?document_id=" + documentId + "&pages=" + pages);
            }
        } catch (Exception ex) {
            Logger.getLogger(eSignServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private User getUser(HttpServletRequest request) {
        UserSession session = (UserSession) request.getSession().getAttribute("user_session");
        return session.getUser();
    }

    private void signDocument(Document document, User user, String signerIPAddress) throws Exception {
        ArrayList<DocumentSignature> documentSignatures = getTranslatedDocumentSignatures(document.getDocumentId(), user.getUserId());
        // save the document signatures
        DocumentSignature.insertSignatures(document.getDocumentId(), documentSignatures, user.getUserId(), signerIPAddress);
    }

    private ArrayList<DocumentSignature> getTranslatedDocumentSignatures(int documentId, int userId) {
        // translate signature locations from image to document coordinate system.
        ArrayList<DocumentSignature> documentSignatures = new ArrayList<>();
        for (Signature signature : signatures.values()) {
            int x = PDF_PAGE_BOTTOM_LEFT_X_COORDINATE + signature.getLeft(); //* (pdfPageWidth / (pageImageWidth * pageImageWidthFactor));
            int y = PDF_PAGE_TOP_RIGHT_Y_COORDINATE - signature.getTop() - 60;// * (pdfPageHeight / (pageImageHeight * pageImageHeightFactor));
            documentSignatures.add(new DocumentSignature(documentId, userId, signature.getPage() + 1, x, y));
        }
        return documentSignatures;
    }

    private int getPageCount(Document document) throws Exception {
        try (PDDocument doc = PDDocument.load(document.getContent())) {
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
            return (pages != null) ? pages.size() : 0;
        }
    }

    private void renderPdfPagesAsImages(HttpServletResponse response, Document document, int pageNum) throws Exception {
        try (PDDocument doc = PDDocument.load(document.getContent())) {
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
            PDPage page = pages.get(pageNum);
            BufferedImage image = page.convertToImage();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outStream);
            byte[] imageBytes = outStream.toByteArray();
            response.setContentType("image/jpeg");
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
        }
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
        processGetRequest(request, response);
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
        processPostRequest(request, response);
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

    private class Signature {

        private String signatureId = "";
        private int page = 0;
        private int left = 0;
        private int top = 0;

        public Signature(String signatureId, int page) {
            this.signatureId = signatureId;
            this.page = page;
        }

        public String getSignatureId() {
            return signatureId;
        }

        public int getPage() {
            return page;
        }

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public void setTop(int top) {
            this.top = top;
        }
    }
}
