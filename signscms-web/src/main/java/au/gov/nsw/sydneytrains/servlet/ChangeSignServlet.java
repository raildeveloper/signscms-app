package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import com.google.gson.Gson;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by administrator on 27/5/17.
 */
public class ChangeSignServlet implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {

        // Check the request type
        String format = (request.getParameter("format") != null) ? request.getParameter("format") : "html";

        String action = (request.getParameter("action") != null) ? request.getParameter("action") : "view";
        String deviceId = (request.getParameter("deviceId") != null) ? request.getParameter("deviceId") : "null";
        //System.out.println("HERE !! format-> " + format + "device Id " + deviceId + "action -> " + action);

        // Prevent browser caching of response
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("application/json");
        if (format.equals("json")) {
            if (action.equals("view")) {
                final PrintWriter printWriter = response.getWriter();
                CnfData cnfData = CnfData.getInstance();
                String allDevicesJson = cnfData.getAllDevicesAsJSON();
                String allViewsJson = cnfData.getAllViewsAsJSON();
                String allLinksJson = cnfData.getAllLinksAsJSON();
                String responseJson = "[" + allDevicesJson + "," +allViewsJson + "," + allLinksJson + "]";

                printWriter.write(responseJson);
            }
            if (action.equals("change")) {
                final PrintWriter printWriter = response.getWriter();
                boolean successUpdt = false;
                String viewId = (request.getParameter("viewId") != null) ? request.getParameter("viewId") : "null";

                final CnfDao cnfDao = H2DatabaseAccess.getCnfDao();
                try {
                    if (null != CnfData.getInstance().getLink(deviceId)) {
						// Update the device view link in the database
						successUpdt = cnfDao.updateDeviceView(deviceId, viewId);
					} else {
                    	successUpdt = cnfDao.addDeviceView(deviceId, viewId);
					}

                } catch (SQLException s) {
                    s.printStackTrace();
                }
                if (successUpdt) {
                    // TODO is this necessary? There is a bug in reading from the DB: the images are not read with the view
                    // CnfData.getInstance().readFromDatabase();
                    CnfData.getInstance().updateLink(deviceId, viewId);
                    //System.out.println("Success update");
                    printWriter.write(new Gson().toJson("Success"));
                } else {
                    printWriter.write(new Gson().toJson("Error"));
                }
            }
        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/changeSign.jsp").forward(request, response);
        }

    }


}
