package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.dao.SchedulerDao;
import au.gov.nsw.sydneytrains.dao.UserDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.service.ContactUsService;
import au.gov.nsw.sydneytrains.service.SchedulerService;

import javax.servlet.ServletContextEvent;
import java.sql.Connection;

/**
 * Initialises the application.
 */
public class CnfDataObjectsListener implements javax.servlet.ServletContextListener {
    private CnfData cnfData;

    public void contextInitialized(ServletContextEvent servletContextEvent) {

        // Check for H2Database connection to be available
        Connection H2Db = H2DatabaseAccess.getDbConnection();
        if (H2Db == null) {
            System.out.println("ERROR: failed to get connection to H2Database");
            return;
        }

        // Initialise Tables for Contact Us & Scheduler
        System.out.println("Initialising Contact Us Database.");
        ContactUsDao contactUsDao = new ContactUsDao();
        if(!contactUsDao.initialiseDb()){
            System.out.println("ERROR: failed to initialise Contact Us Database Schema");
            return;
        }
        System.out.println("SUCCESS: Initialised Contact Us Database.");

        System.out.println("Initialising Scheduler Database.");
        SchedulerDao schedulerDao = new SchedulerDao();
        if(!schedulerDao.initialiseDb()){
            System.out.println("ERROR: failed to initialise Scheduler Database Schema");
            return;
        }
        System.out.println("SUCCESS: Initialised Scheduler Database.");

        // Read Users.csv into H2Database
        UserDao userDao = new UserDao();
        if (!userDao.initialiseDb()) {
            System.out.println("ERROR: failed to initialise Users configuration data");
            return;
        }
        System.out.println("SUCCESS: initialised Users configuration data");

        // Read all data from H2Database into CnfData
        CnfDao cnfDao = new CnfDao();
        cnfDao.setDbConnection(H2Db);
        if (!cnfDao.initialiseDb()) {
            System.out.println("ERROR: failed to initialise configuration data");
            return;
        }
        cnfData = CnfData.getInstance();
        if (!cnfData.readFromH2Database()) {
            System.out.println("ERROR: failed to read configuration data");
            return;
        }
        System.out.println("SUCCESS: initialised CnfData from H2Database");

        // Start Scheduler
        System.out.println("Start Scheduler Service");
        SchedulerService schedulerService = new SchedulerService();
        try {
            schedulerService.startScheduler();
            System.out.println("SUCCESS: Scheduler Service Started");
        }catch (Exception e){
            System.out.println("ERROR: Scheduler Service failed to start.");
            e.printStackTrace();
        }
        System.out.println("Start Contact-Us Form Scheduler Service");
        ContactUsService contactUsService = new ContactUsService();
        try{
        contactUsService.startScheduler();
            System.out.println("SUCCESS: ContactUs Service Started");
        }catch (Exception e){
            System.out.println("ERROR: ContactUs Service failed to start.");
            e.printStackTrace();
        }

    }

    public final void contextDestroyed(final ServletContextEvent servletContextEvent) {

    }
}
