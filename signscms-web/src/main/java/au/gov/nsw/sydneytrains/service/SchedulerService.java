package au.gov.nsw.sydneytrains.service;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.dao.SchedulerDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.model.CnfLink;
import au.gov.nsw.sydneytrains.model.CnfSchedule;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public void startScheduler() throws Exception {
        try {

            LocalDateTime now = LocalDateTime.now();
            int second = now.get(ChronoField.SECOND_OF_MINUTE);
            //System.out.println("SECOND " + second);
            int delay = 60 - second;
            //System.out.println("DELAY " + delay);
            ScheduledFuture<?> files = executorService.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    //System.out.println("running");
                    changeSignsonSchedule();
                }
            }, delay, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    public void changeSignsonSchedule() {
        try {

            SchedulerDao schedulerDao = H2DatabaseAccess.getSchedulerDao();
            CnfDao cnfDao = H2DatabaseAccess.getCnfDao();
            ArrayList<CnfSchedule> schedules = schedulerDao.getAllSchedules();
            CnfData cnfData = CnfData.getInstance();

            for (CnfSchedule schedule : schedules) {

                //Apply schedule only if its active
                String status = schedule.getStatus();

                if (status.equalsIgnoreCase("ACTIVE")) {

                    //Get Schedule Start Time & End Times
                    Date startDate = schedule.getStartTime();
                    Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                    calendar.setTime(startDate);   // assigns calendar to given date
                    LocalTime sTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                    Date endDate = schedule.getEndTime();
                    calendar.setTime(endDate);
                    LocalTime eTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

                    ZoneId sydneyZone = ZoneId.of("Australia/Sydney");

                    LocalTime currentTime = LocalTime.now(sydneyZone);
                    String previousSchedule = schedule.getPreviousCnfView();
                    String device = schedule.getCnfDevice();
                    String scheduledView = schedule.getScheduleCnfView();

                    if (currentTime.isAfter(sTime) && previousSchedule.equalsIgnoreCase("")) {

                        // Additionally check if Current Time is before end Time
                        if(currentTime.isBefore(eTime)) {

                            System.out.println("APPLY SCHEDULE");
                            // Get the current view associated with device
                            CnfLink cnfLink = cnfData.getLink(device);
                            String currentView = cnfLink.getViewId();
                            // apply this in scheduler.

                            schedule.setPreviousCnfView(currentView);
                            boolean update = schedulerDao.updateSchedule(schedule);
                            if (update) {

                                cnfDao.updateDeviceView(device, scheduledView);
                                CnfData.getInstance().updateLink(device, scheduledView);
                            } else {
                                System.out.println("Couldn't update this schedule in database" + schedule.getCnfScheduleId());
                            }
                        }
                    }
                    if (currentTime.isAfter(eTime) && !(previousSchedule.equalsIgnoreCase(""))) {
                        System.out.println("REVERT SCHEDULE");

                        // Apply previous schedule
                        cnfDao.updateDeviceView(device,previousSchedule);
                        CnfData.getInstance().updateLink(device, previousSchedule);
                        schedule.setPreviousCnfView("");
                        schedulerDao.updateSchedule(schedule);


                    }
                    System.out.println(" Start time" + sTime.toString());
                    System.out.println(" End time" + eTime.toString());
                    System.out.println(" Time Now" + currentTime.toString());
                } else {
                    System.out.println("Schedule Id" + schedule.getCnfScheduleId() + " is not active.");
                }

            }


        } catch (SQLException e) {
            e.printStackTrace();
            //throw e;
        }
    }


}
