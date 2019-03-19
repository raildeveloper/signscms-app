package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.helper.MultipartMap;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Created by administrator on 14/5/17.
 */
@MultipartConfig(location = "/Contact", maxFileSize = 10485760L)
public class ContactUsServlet implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {

        String format = (request.getParameter("format") != null) ? request.getParameter("format") : "html";
        String action = request.getParameter("action");

        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("application/json");
        final PrintWriter printWriter = response.getWriter();
        if (format.equals("json")) {
            if (action.equals("insert")) {
                try {

                    MultipartMap map = new MultipartMap(request, this);

                    String name = map.getParameter("name");
                    String email = map.getParameter("email");
                    String mobile = map.getParameter("mobile");
                    String subject = map.getParameter("subject");
                    String message = map.getParameter("message");

                    /*Part filePart = request.getPart("file");

                    if(filePart != null) {

                        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
                        InputStream fileContent = filePart.getInputStream();

                        System.out.println(fileName);
                        System.out.println(fileContent.toString());
                    }
                    */

                    if (name != null){
                        ContactUsDao contactUsDao = H2DatabaseAccess.getContactUsDao();
                    boolean result = contactUsDao.insertContactUsForm(name, email, message, subject, message, "OPEN");
                    String responseJson;
                    if (result) {
                        responseJson = "{\n" +
                                "  \"success\": true,\n" +
                                "  \"payload\": {\n" +
                                "  }\n" +
                                "}";
                    } else {
                        responseJson = "{\n" +
                                "  \"success\": true,\n" +
                                "  \"payload\": {\n" +
                                "  }\n" +
                                "}";
                    }
                    printWriter.write(responseJson);
                    System.out.println("SUCCESSFULLY SAVED CONTACT US" + name + ":" + email + ":" + mobile + ":" + subject + ":" + message);
                } else {
                        System.out.println("NAME IS EMPTY");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            request.getRequestDispatcher("/WEB-INF/jsp/contactUs.jsp").forward(request, response);
        }

    }


}
