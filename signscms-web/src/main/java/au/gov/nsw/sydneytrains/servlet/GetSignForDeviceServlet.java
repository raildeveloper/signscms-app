package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.model.CnfDevice;
import au.gov.nsw.sydneytrains.model.CnfLink;
import au.gov.nsw.sydneytrains.model.CnfView;
import com.google.gson.Gson;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by administrator on 23/4/17.
 */
public class GetSignForDeviceServlet implements HttpRequestHandler {

    // Spring Injected

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
        // Check the request type
        String format = (request.getParameter("format") != null) ? request.getParameter("format") : "html";
        String deviceName = request.getParameter("hostname");
        String sectionName = request.getParameter("section");


        // Prevent browser caching of response
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("application/json");

        // Device IP Address
        String deviceIPAddress = request.getRemoteAddr();
        String deviceHost = request.getRemoteHost();
        System.out.println("Request from: sectionName " + sectionName + " device name " + deviceName + " IP address " + deviceIPAddress + " host " + deviceHost);

        if (format.equals("json")) {
            if(sectionName != null){
                sendResponseForSection(response, sectionName);
            } else {
                sendResponseForDevice(response, deviceName);
            }
        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/sign.jsp").forward(request, response);
        }
    }

    private void sendResponseForDevice(HttpServletResponse httpServletResponse, String hostName) throws IOException{

        final PrintWriter writer = httpServletResponse.getWriter();
        Gson gson = new Gson();

        CnfData configData = CnfData.getInstance();
        CnfView defaultView = configData.getDefaultView();

        CnfDevice cnfDevice = configData.getDeviceByName(hostName);
        if (null == cnfDevice) {
            writer.append(gson.toJson(defaultView));
            System.out.println("Reply: " + defaultView.getAsString());
            return;
        }

        // Get the CnfView LINK for this cnfDevice
        CnfLink cnfLink = configData.getLink(cnfDevice.getDeviceId());
        if (null == cnfLink) {
            writer.append(gson.toJson(defaultView));
            System.out.println("Reply: " + defaultView.getAsString());
            return;
        }

        // Get the CnfView in this link
        CnfView cnfView = configData.getViewById(cnfLink.getViewId());
        cnfView.setCnfDevice(cnfDevice);
        if (null == cnfView) {
            writer.append(gson.toJson(defaultView));
            System.out.println("Reply: " + defaultView.getAsString());
            return;
        }

        writer.append(gson.toJson(cnfView));
        System.out.println("Reply: " + cnfView.getAsString());
    }

    private void sendResponseForSection(HttpServletResponse httpServletResponse, String section) throws IOException{

        final PrintWriter writer = httpServletResponse.getWriter();
        Gson gson = new Gson();
        System.out.println("section  -->" + section);
        CnfData configData = CnfData.getInstance();
        List<CnfDevice> cnfDevices = configData.getDevicesBySectionId(section);
        List<CnfView> cnfViews = new ArrayList<>();
        for(CnfDevice cnfDevice: cnfDevices){
            System.out.println("Inside For Device");
            cnfDevice.print();
            CnfLink cnfLink = configData.getLink(cnfDevice.getDeviceId());

            CnfView cnfView = configData.getViewById(cnfLink.getViewId());
            CnfView cnfView1 = new CnfView(cnfView.getViewId(),cnfView.getViewName(),cnfView.getDescription(),cnfView.getPixelsHorizontal(),cnfView.getPixelsVertical(),cnfView.getAssociated_Device(),cnfView.getImages());
            System.out.println("cnfView ------- " + cnfView.getAsString());
            //cnfDevice.print();
            cnfView1.setCnfDevice(cnfDevice);
            System.out.println("What has been set in CnfView");
            cnfView1.getCnfDevice().print();
            cnfViews.add(cnfView1);
        }

        writer.append(gson.toJson(cnfViews));
        System.out.println("Reply: " + gson.toJson(cnfViews));
    }
}

