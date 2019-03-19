package au.gov.nsw.sydneytrains.servlet;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.model.CnfView;
import au.gov.nsw.sydneytrains.xml.CnfXml;
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
 * Created by administrator on 23/4/17.
 */
public class InitialiseDeviceView implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException
    {
        // Called when URL <server>:8080/Init is used
        // Possible parameters are: - action=load  : loads the SCMS_Config.xml file
		//							- action=clear : clears the H2Database

        // Prevent browser caching of response
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("application/json");


        String format = (request.getParameter("format") != null) ? request.getParameter("format") : "html";
		String action = (request.getParameter("action") != null) ? request.getParameter("action") : "view";

        if (format.equals("json")) {
        	if (action.equals("load")) {
				actionIsLoad(response);
			} else if (action.equals("clear")) {
        		actionIsClear(response);
			} else if (action.equals("showXML")) {
				actionIsShowXml(response);
			} else {
        		final PrintWriter writer = response.getWriter();
        		writer.write(new Gson().toJson("Error"));
			}
        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/init.jsp").forward(request, response);
        }

    }

    private void actionIsLoad(HttpServletResponse response) throws IOException {

		System.out.println("Loading XML configuration file");

		boolean bRv = loadXmlConfigFile();
        final PrintWriter writer = response.getWriter();
		if (bRv) {
			writer.append(new Gson().toJson("Success"));
		} else {
			writer.append(new Gson().toJson("Error"));
		}
    }

    private void actionIsClear(HttpServletResponse response) throws IOException {

		System.out.println("Clearing tables in H2Database");

		boolean bRv = clearH2Database();
		final PrintWriter writer = response.getWriter();
		if (bRv) {
			writer.append(new Gson().toJson("Success"));
		} else {
			writer.append(new Gson().toJson("Error"));
		}
	}

	private void actionIsShowXml(HttpServletResponse response) throws IOException {

    	System.out.println("Getting XML configuration data");

		// Load the configuration file into a CnfData instance
		CnfXml cnfXml = new CnfXml();
		boolean result = cnfXml.loadFile();

		final PrintWriter writer = response.getWriter();
		if (result) {
			String strXml = cnfXml.getAsXmlString();
			writer.append(new Gson().toJson(strXml));
		} else {
			writer.append(new Gson().toJson("Error"));
		}

	}
    /*
     * Read the configuration data from the XML configuration file.
     */
    private boolean loadXmlConfigFile(){

        // Load the configuration file into a CnfData instance
        CnfXml cnfXml = new CnfXml();
        if (!cnfXml.loadFile()) {
            System.out.println("ERROR, failed to load XML Config file");
            return false;
        }

        CnfData newData = cnfXml.getCnfData();

        // Validate cnfData first (and fix if necessary)
        newData.setLinks(CnfData.getInstance().getAllLinks());
        newData.validateAndFix();

        // Store the loaded data into the H2Database
        CnfDao cnfDao = new CnfDao();
        cnfDao.setDbConnection(H2DatabaseAccess.getDbConnection());

        try {
            cnfDao.setAllCnfDevices(newData.getAllDevices());
            cnfDao.setAllCnfViews(newData.getAllViews());
            cnfDao.setAllCnfViewLinks(newData.getAllViews());
            cnfDao.setAllCnfSections(newData.getAllSections());
            cnfDao.setAllCnfLinks(newData.getAllLinks());
        } catch (SQLException e) {
            System.out.println("ERROR, failed to write XML Config file data to H2 database");
            return false;
        }

        // Update the singleton CnfData
		CnfData.getInstance().setDevices(newData.getAllDevices());
        CnfData.getInstance().setViews(newData.getAllViews());
        CnfData.getInstance().setSections(newData.getAllSections());
        CnfData.getInstance().setLinks(newData.getAllLinks());

        return true;
    }

	/*
	 * Empty all tables in the H2Database
	 */
    private boolean clearH2Database() {
    	boolean result = false;
		CnfDao cnfDao = new CnfDao();
		cnfDao.setDbConnection(H2DatabaseAccess.getDbConnection());

		try {
			result = cnfDao.clearCnfTables();
		} catch (SQLException e) {
			System.out.println("ERROR, failed clear CNF tables in H2 database");
		}

		return result;
	}
}
