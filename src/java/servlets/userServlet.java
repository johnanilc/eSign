package servlets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import classes.Document;
import classes.User;
import classes.UserSession;
import com.google.gson.Gson;
import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author johnanilc
 */
@WebServlet(urlPatterns = {"/userServlet"})
@MultipartConfig(maxFileSize = 16177215) //upload file upto 16 MB
public class userServlet extends HttpServlet {

    private static final String SIGNATURE_FONT_NAME = "Lucida Handwriting";
    private static final int SIGNATURE_FONT_SIZE = 16;
    private static final int SIGNATURE_FONT_STYLE = Font.BOLD+Font.ITALIC;

    /**
     * Processes post requests for both HTTP <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = getUser(request);

            String signatureName = request.getParameter("signature_name");
            if (signatureName != null) {
                // signature creation
                String signatureOutput = request.getParameter("signature_output");
                createSignature(user, signatureName, signatureOutput);
            } else {
                Part filePart;
                if (request.getPart("document") != null) {
                    filePart = request.getPart("document");
                    uploadDocument(request, filePart, user);
                } else if (request.getPart("signature") != null) {
                    filePart = request.getPart("signature");
                    uploadSignature(request, filePart, user);
                }
            }

            response.sendRedirect("upload_document.jsp");
        } catch (Exception ex) {
            Logger.getLogger(userServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSignature(User user, String signatureName, String signatureOutput) throws Exception {
        BufferedImage signatureImage;
        if (signatureOutput != null && !signatureOutput.isEmpty()) {
            // create signature from image
            signatureImage = convertJsonToImage(signatureOutput);
        } else {
            signatureImage = createImageFromText(signatureName);
        }

        // save to database against user
        insertSignature(user, signatureImage);
    }

    private BufferedImage createImageFromText(String signatureName) {
        /*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font(SIGNATURE_FONT_NAME, SIGNATURE_FONT_STYLE, SIGNATURE_FONT_SIZE);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(signatureName)+10;
        int height = fm.getHeight();
        g2d.dispose();

        // create signature image from text
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(signatureName, 0, fm.getAscent());
        g2d.dispose();
        
        return img;
    }

    private void insertSignature(User user, BufferedImage signatureImage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(signatureImage, "png", baos);
        user.setSignature(new ByteArrayInputStream(baos.toByteArray()));
        user.insertSignature();
    }

    private static BufferedImage convertJsonToImage(String jsonString) {
        Gson gson = new Gson();
        SignatureLine[] signatureLines = gson.fromJson(jsonString, SignatureLine[].class);
        BufferedImage offscreenImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = offscreenImage.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, 200, 50);
        g2.setPaint(Color.black);
        for (SignatureLine line : signatureLines) {
            g2.drawLine(line.lx, line.ly, line.mx, line.my);
        }
        return offscreenImage;
    }

    private User getUser(HttpServletRequest request) {
        UserSession session = (UserSession) request.getSession().getAttribute("user_session");
        return session.getUser();
    }

    private void uploadSignature(HttpServletRequest request, Part filePart, User user) {
        try {
            user.setSignature(filePart.getInputStream());
            user.insertSignature();
            request.setAttribute("signature_upload", "Upload Successful");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            request.setAttribute("signature_upload", "Upload Error" + e.getMessage());
        }
    }

    private void uploadDocument(HttpServletRequest request, Part filePart, User user) throws Exception {
        Document document = new Document(getFileName(filePart), filePart.getInputStream(), new Date(), user.getUserId());
        try {
            document.insert();
            request.setAttribute("document_upload", "Upload Successful");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            request.setAttribute("document_upload", "Upload Error" + e.getMessage());
        }
    }

    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = getUser(request);
            if (request.getParameter("signature_image") != null) {
                // get signature image
                if (user.getSignature() != null) {
                    renderSignatureImage(user.getSignature(), response);
                }
            } else {
                // show user dashboard
                request.getRequestDispatcher("upload_document.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void renderSignatureImage(InputStream signature, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        int numRead;
        byte[] buf = new byte[1024];
        while ((numRead = signature.read(buf)) >= 0) {
            outStream.write(buf, 0, numRead);
        }
        signature.reset();

        byte[] imageBytes = outStream.toByteArray();
        response.setContentType("image/jpeg");
        response.setContentLength(imageBytes.length);
        response.getOutputStream().write(imageBytes);
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        for (String content : partHeader.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
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

    private static class SignatureLine {

        int lx, ly, mx, my;

        public SignatureLine() {
        }

        public int getLx() {
            return lx;
        }

        public void setLx(int lx) {
            this.lx = lx;
        }

        public int getLy() {
            return ly;
        }

        public void setLy(int ly) {
            this.ly = ly;
        }

        public int getMx() {
            return mx;
        }

        public void setMx(int mx) {
            this.mx = mx;
        }

        public int getMy() {
            return my;
        }

        public void setMy(int my) {
            this.my = my;
        }
    }
}
