/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import classes.Document;
import classes.DocumentSignature;
import static servlets.eSignServlet.signatureImagePath;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author johnanilc
 */
@WebServlet(name = "documentServlet", urlPatterns = {"/documentServlet"})
public class documentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{
            int documentId = Integer.parseInt(request.getParameter("document_id"));
            Document document = Document.getDocument(documentId);
            PdfReader reader = new PdfReader(document.getContent());
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, os);
            
            Image signatureImage = Image.getInstance(getServletContext().getRealPath(signatureImagePath));
            ArrayList<DocumentSignature> signatures = new ArrayList<>();
            try{
                for (DocumentSignature signature : signatures){
                    int x = signature.getSignLocationX();
                    int y = signature.getSignLocationY();
                    signatureImage.setAbsolutePosition(x, y);
                    PdfContentByte canvas = stamper.getOverContent(signature.getPageNumber()+1);
                    canvas.addImage(signatureImage);
                }
            }finally{
                stamper.close();
            }
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" + document.getName());
            response.setContentLength((int) os.size());
            
            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(os.toByteArray());
        }   catch (Exception ex) {
            Logger.getLogger(documentServlet.class.getName()).log(Level.SEVERE, null, ex);
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
        processRequest(request, response);
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
        processRequest(request, response);
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
