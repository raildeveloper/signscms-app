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
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by administrator on 14/5/17.
 */
public class ScheduleServlet implements HttpRequestHandler {

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
                ArrayList<CnfSchedule> schedules = new ArrayList<CnfSchedule>();
                SchedulerDao schedulerDao = H2DatabaseAccess.getSchedulerDao();
                //

                //System.out.println("GET ALL SCHEDULES");
                CnfSchedule schedule = new CnfSchedule();
                schedule.setCnfDevice("d1");
                schedule.setScheduleCnfView("v1");
                schedule.setPreviousCnfView("v2");
                schedule.setStartTime(new java.util.Date());
                schedule.setEndTime(new java.util.Date());
                schedule.setStatus("ACTIVE");
                schedule.setIsDeleted("FALSE");
                schedule.setUsername("townhall");

                try {
                    //schedulerDao.insertSchedule(schedule);
                    schedule.setUsername("pnandwani");
                    //schedulerDao.insertSchedule(schedule);
                    schedule.setIsDeleted("TRUE");
                    schedule.setUsername("lmiller");
                    //schedulerDao.insertSchedule(schedule);
                    schedules = schedulerDao.getAllSchedules();

                    //for(CnfSchedule cnfSchedule: schedules){
                        //System.out.println(cnfSchedule.getCnfScheduleId());
                    //}

                }catch (Exception e){
                    e.printStackTrace();
                }


                CnfData cnfData = CnfData.getInstance();
                String allDevicesJson = cnfData.getAllDevicesAsJSON();
                String allViewsJson = cnfData.getAllViewsAsJSON();
                String allLinksJson = cnfData.getAllLinksAsJSON();
                String allSectionsJson = cnfData.getAllSectionsAsJSON();

                Gson gson = new Gson();
                String schedulesJson = gson.toJson(schedules);
                String responseJson = "[" + schedulesJson + "," + allDevicesJson + "," + allViewsJson + "," + allLinksJson + "," + allSectionsJson + "]";
                printWriter.write(responseJson);
            }
        } else {

            request.getRequestDispatcher("/WEB-INF/jsp/scheduleList.jsp").forward(request, response);
        }

    }


}
