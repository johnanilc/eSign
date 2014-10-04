package classes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    
    private static final int pageImageWidth = 1224;
    private static final int pageImageHeight = 1584;
    private static final float pageImageWidthFactor = (float)0.6;
    private static final float pageImageHeightFactor = (float) 0.5;
    
    private static final int pdfPageHeight = 770;
    private static final int pdfPageWidth = 523;

    private static final String srcFilePath ="resources/signDocument.pdf";
    public static final String signatureImagePath ="resources/signature_anil.jpg";
    
    private static HashMap<String, Signature> signatures = new HashMap<>();

    
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String signatureId = request.getParameter("signatureId");
        if (signatureId == null){
            // create pdf with signatures.
            try{
                System.out.println("Signing Document..."); 
                downloadSignedPdf(response);
            }catch (Exception e){
                System.out.println("Signing Failed " + e.getMessage());
            }
        }else{
            // create/update signature location
            Signature sign = signatures.get(signatureId);
            if (sign == null){
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
        }
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
            String filePath = getServletContext().getRealPath(srcFilePath);
            String pageNumber = request.getParameter("pageNum");
            if (pageNumber != null){
                // render pdf page as image.
                int pageNum = Integer.parseInt(pageNumber);
                renderPdfAsImage(response, filePath, pageNum);
            }else{
                // show document for signing
                request.setAttribute("pages", getPageCount(filePath));
                request.getRequestDispatcher("signDocument.jsp").forward(request, response);
                
                // clear signatures if any set from previous load.
                signatures = new HashMap<>();
            }
            
        } catch (Exception ex) {
            Logger.getLogger(eSignServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void downloadSignedPdf(HttpServletResponse response) throws Exception {
        Image img = Image.getInstance(getServletContext().getRealPath(signatureImagePath));
        PdfReader reader = new PdfReader(getServletContext().getRealPath(srcFilePath));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, os);
        
        PdfContentByte canvas = stamper.getOverContent(1);
        System.out.println("Upper right x: " + canvas.getPdfDocument().right());
        System.out.println("Upper right y: " + canvas.getPdfDocument().top());
        System.out.println("Lower left x: " + canvas.getPdfDocument().left());
        System.out.println("Lower left y: " + canvas.getPdfDocument().bottom());
                
        try{
            for (Signature signature : signatures.values()){
                float x = signature.getLeft(); //* (pdfPageWidth / (pageImageWidth * pageImageWidthFactor));
                float y = signature.getTop();// * (pdfPageHeight / (pageImageHeight * pageImageHeightFactor));
                img.setAbsolutePosition(36+x, 806-y-100);
                canvas = stamper.getOverContent(signature.getPage()+1);
                canvas.addImage(img);
            }
        }finally{
            stamper.close();
        }
        
        // download signed pdf.
        response.setContentType("application/pdf");
        response.addHeader("Content-Disposition", "attachment; filename=signedPdf.pdf");
        response.setContentLength((int) os.size());

        OutputStream responseOutputStream = response.getOutputStream();
        responseOutputStream.write(os.toByteArray());
    }
    
    private int getPageCount(String filePath) throws Exception {
         try (PDDocument doc = PDDocument.load(filePath)) {
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
            return (pages != null) ? pages.size() : 0;
         }
    }
    
    
    private void renderPdfAsImage(HttpServletResponse response, String filePath, int pageNum) throws Exception {
          try (PDDocument doc = PDDocument.load(filePath)) {
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
            PDPage page = pages.get(pageNum);
            BufferedImage image = page.convertToImage();
            System.out.println("Image height " + image.getHeight());
            System.out.println("Image width " + image.getWidth());
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
}
