package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.SchedulerDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.model.CnfSchedule;
import com.google.gson.Gson;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by administrator on 14/5/17.
 */
public class ScheduleOperationsServlet implements HttpRequestHandler {

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
        SchedulerDao schedulerDao = H2DatabaseAccess.getSchedulerDao();
        if (format.equals("json")) {
            if (action.equals("add")) {
                System.out.println("HERE ADD");
                CnfData cnfData = CnfData.getInstance();
                String allDevicesJson = cnfData.getAllDevicesAsJSON();
                String allViewsJson = cnfData.getAllViewsAsJSON();
                String allLinksJson = cnfData.getAllLinksAsJSON();
                String allSectionsJson = cnfData.getAllSectionsAsJSON();
                String responseJson = "[" + allDevicesJson + "," + allViewsJson + "," + allLinksJson + "," + allSectionsJson + "]";
                printWriter.write(responseJson);
            }

            if (action.equals("insert")) {
                String deviceId = request.getParameter("device");
                String viewId = request.getParameter("view");
                String startTime = request.getParameter("starttime");
                startTime = convertTo24HoursFormat(startTime);
                String endTime = request.getParameter("endtime");
                endTime = convertTo24HoursFormat(endTime);
                CnfSchedule cnfSchedule = new CnfSchedule();
                cnfSchedule.setCnfDevice(deviceId);
                cnfSchedule.setScheduleCnfView(viewId);
                cnfSchedule.setPreviousCnfView("");
                cnfSchedule.setUsername(request.getSession().getAttribute("username").toString());
                cnfSchedule.setIsDeleted("FALSE");
                cnfSchedule.setStatus("INACTIVE");
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                try {
                    Date startDate = dateFormat.parse(startTime);
                    cnfSchedule.setStartTime(startDate);
                    Date endDate = dateFormat.parse(endTime);
                    cnfSchedule.setEndTime(endDate);
                    boolean insert = schedulerDao.insertSchedule(cnfSchedule);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String responseJson = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"payload\": {\n" +
                        "  }\n" +
                        "}";
                printWriter.write(responseJson);
                System.out.println("request " + deviceId + ":" + viewId + ":" + startTime + ":" + endTime);
            }


            // Update Schedule

            if (action.equals("update")) {
                String scheduleId = request.getParameter("scheduleId");
                String deviceId = request.getParameter("device");
                String viewId = request.getParameter("view");
                String startTime = request.getParameter("starttime");
                startTime = convertTo24HoursFormat(startTime);
                String endTime = request.getParameter("endtime");
                endTime = convertTo24HoursFormat(endTime);

                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                try {
                    CnfSchedule schedule = schedulerDao.getSchedule(scheduleId);
                    schedule.setCnfDevice(deviceId);
                    schedule.setScheduleCnfView(viewId);
                    schedule.setUsername(request.getSession().getAttribute("username").toString());
                    Date startDate = dateFormat.parse(startTime);
                    schedule.setStartTime(startDate);
                    Date endDate = dateFormat.parse(endTime);
                    schedule.setEndTime(endDate);
                    boolean updateSchedule = schedulerDao.updateSchedule(schedule);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String responseJson = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"payload\": {\n" +
                        "  }\n" +
                        "}";
                printWriter.write(responseJson);
                System.out.println("request " + deviceId + ":" + viewId + ":" + startTime + ":" + endTime);
            }


            if (action.equals("edit")) {
                String scheduleId = request.getParameter("scheduleId");
                System.out.println("EDIT SCHEDULE " + scheduleId);
                try {
                    CnfSchedule schedule = schedulerDao.getSchedule(scheduleId);
                    CnfData cnfData = CnfData.getInstance();
                    Gson gson = new Gson();
                    String cnfScheduleJson = gson.toJson(schedule);
                    String allDevicesJson = cnfData.getAllDevicesAsJSON();
                    String allViewsJson = cnfData.getAllViewsAsJSON();
                    String allLinksJson = cnfData.getAllLinksAsJSON();
                    String allSectionsJson = cnfData.getAllSectionsAsJSON();
                    String responseJson = "[" + allDevicesJson + "," + allViewsJson + "," + allLinksJson + "," + allSectionsJson + ","+ cnfScheduleJson + "]";
                    printWriter.write(responseJson);
                } catch (SQLException e) {
                    e.printStackTrace();
                }



            }
            if(action.equals("status")){
                System.out.println("DELETE SCHEDULE");
                String scheduleId = request.getParameter("scheduleId");
                String status = request.getParameter("status");

                // Get Schedule
                try {
                    CnfSchedule schedule = schedulerDao.getSchedule(scheduleId);

                    // Set it to be deleted
                    schedule.setStatus(status);
                    boolean delete = schedulerDao.updateSchedule(schedule);
                    String responseJson = "{\n" +
                            "  \"success\": true,\n" +
                            "  \"payload\": {\n" +
                            "  }\n" +
                            "}";
                    printWriter.write(responseJson);
                    System.out.println("Successfully change schedule status" + scheduleId);


                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (action.equals("delete")) {
                System.out.println("DELETE SCHEDULE");
                String scheduleId = request.getParameter("scheduleId");

                // Get Schedule
                try {
                    CnfSchedule schedule = schedulerDao.getSchedule(scheduleId);

                    // Set it to be deleted
                    schedule.setIsDeleted("TRUE");
                    boolean delete = schedulerDao.updateSchedule(schedule);
                    String responseJson = "{\n" +
                            "  \"success\": true,\n" +
                            "  \"payload\": {\n" +
                            "  }\n" +
                            "}";
                    printWriter.write(responseJson);
                    System.out.println("Successfully deleted schedule " + scheduleId);


                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        } else {

            request.getRequestDispatcher("/WEB-INF/jsp/scheduleOperations.jsp").forward(request, response);
        }

    }

    private String convertTo24HoursFormat(String time) {

        String[] parts = time.split(" ");
        String ampm = parts[1];
        String timeT = parts[0];
        String[] sparts = timeT.split(":");
        String hours = sparts[0];
        String minutes = sparts[1];
        if (ampm.equalsIgnoreCase("PM")) {
            int hrs = Integer.parseInt(hours) + 12;
            hours = String.valueOf(hrs);
        }
        String reTime = hours + ":" + minutes;
        return reTime;

    }


}
