package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.UserDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.*;
import com.google.gson.Gson;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by administrator on 27/4/17.
 */
public class SignControllerServlet implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {

        // Check the request type
        String format = (request.getParameter("action") != null) ? request.getParameter("action") : "html";

        // Prevent browser caching of response
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("application/json");

        if (format.equals("authenticate")) {

            final PrintWriter printWriter = response.getWriter();
            String userName = request.getParameter("Username");
            String password = request.getParameter("Password");
            System.out.println("HERE IN AUTHENTICATE" + userName + "-" + password);


            final UserDao userDao = H2DatabaseAccess.getUserDao();
            try {
                User user = userDao.authenticateUser(userName,password);
                if(user.isAuthenticated()){
                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", userName);
                    session.setAttribute("password", password);
                    session.setAttribute("firstName", user.getFirstName());
                    session.setAttribute("lastName", user.getLastName());
                    session.setAttribute("role", user.getRole());
                    session.setAttribute("area", user.getArea());
                    session.setAttribute("authenticated", String.valueOf(user.isAuthenticated()));
                }
                Gson gson = new Gson();
                //System.out.println(gson.toJson(user));
                printWriter.append(gson.toJson(user));
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/signController.jsp").forward(request, response);
        }

    }
}
