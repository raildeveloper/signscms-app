package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.model.CnfData;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by administrator on 14/5/17.
 */
public class DeviceServlet implements HttpRequestHandler {

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

        if (format.equals("json")) {
            if (action.equals("getAllData")) {
                final PrintWriter printWriter = response.getWriter();
                CnfData cnfData = CnfData.getInstance();
                String allDevicesJson = cnfData.getAllDevicesAsJSONSectionSort();
                String allViewsJson = cnfData.getAllViewsAsJSON();
                String allLinksJson = cnfData.getAllLinksAsJSON();
                String allSectionsJson = cnfData.getAllSectionsAsJSON();

                String responseJson = "[" + allDevicesJson + "," + allViewsJson + "," + allLinksJson + "," + allSectionsJson + "]";
                //System.out.println("responseJson " + allDevicesJson);
                printWriter.write(responseJson);
            }
        } else {

            request.getRequestDispatcher("/WEB-INF/jsp/deviceList.jsp").forward(request, response);
        }

    }


}
