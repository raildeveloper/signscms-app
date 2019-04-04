package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.dao.SchedulerDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.helper.MultipartMap;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.model.CnfSchedule;
import com.google.gson.Gson;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by administrator on 14/5/17.
 */
@MultipartConfig(location = "/Contact", maxFileSize = 10485760L)
public class AdminServlet implements HttpRequestHandler {

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
            if (action.equals("getAllData")) {

                CnfData cnfData = CnfData.getInstance();
                String allDevicesJson = cnfData.getAllDevicesAsJSON();
                String allViewsJson = cnfData.getAllViewsAsJSON();
                String allLinksJson = cnfData.getAllLinksAsJSON();
                String allSectionsJson = cnfData.getAllSectionsAsJSON();

                Gson gson = new Gson();
                //String schedulesJson = gson.toJson(schedules);
                String responseJson = "[" + allDevicesJson + "," + allViewsJson + "," + allLinksJson + "," + allSectionsJson + "]";
                printWriter.write(responseJson);
            }
            else if (action.equals("upload")){
                System.out.println("HERE ");
                String selectedDevice = request.getParameter("device");
                String selectedView = request.getParameter("view");
                String image = request.getParameter("image");
                System.out.println(selectedDevice + " - " + selectedView + " - " + image);

            }
        } else {

            request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
        }

    }


}
