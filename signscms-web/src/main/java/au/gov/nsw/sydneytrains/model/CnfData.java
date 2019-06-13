package au.gov.nsw.sydneytrains.model;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by Oscar on 10/04/2017.
 * <p>
 * Holds the configuration data: device, view, link
 */
public class CnfData {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static String DEFAULT_DEVICEID = "0";
    public static String DEFAULT_VIEWID = "0";

	private HashMap<String, CnfDevice> m_devices = new HashMap<>();
    private HashMap<String, CnfView> m_views = new HashMap<>();
    private HashMap<String, CnfLink> m_links = new HashMap<>();
    private HashMap<String, CnfSection> m_sections = new HashMap<>();


    private final ClassLoader classLoader = getClass().getClassLoader();


    // There is only one instance of CnfData
    private static CnfData sINSTANCE;

    public static synchronized CnfData getInstance() {
        if (sINSTANCE == null) {
            sINSTANCE = new CnfData();
        }
        return sINSTANCE;
    }


    public CnfData() {
        /* Empty */
    }

    /*
     * Read the configuration data from the H2Database
     */
    public boolean readFromH2Database() {

        boolean result = false;

        // Try configuration database first and file second
        Connection dbConnection = H2DatabaseAccess.getDbConnection();
        if (null == dbConnection) {
            System.out.println("ERROR: failed to get dbConnection");
        } else {
            System.out.println("SUCCESS: got a dbConnection");

            result = readFromDatabase();
            if (!result) {
                System.out.println("ERROR: failed to read from dbConnection");
            }
        }

        if (result) {
            printDevices();
            printViews();
            printLinks();
            printSections();
        }

        return result;
    }

    /*
     * Return the CnfDevice instance specified by deviceId or null.
     */
    public CnfDevice getDeviceById(String deviceId) {
        Iterator<String> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_devices.get(key).getDeviceId().equals(deviceId)) {
                return m_devices.get(key);
            }
        }
        return null;
    }

    /*
     * Return the CnfDevice instance specified by deviceName or null.
     */
    public CnfDevice getDeviceByName(String deviceName) {
        Iterator<String> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            String keyDeviceName = m_devices.get(key).getPi_name();
            if (keyDeviceName.equalsIgnoreCase(deviceName)) {
                return m_devices.get(key);
            }
        }
        return null;
    }


    /*
    * Return the List CnfDevice specified by Section Name or null.
    */
    public List<CnfDevice> getDevicesBySectionId(String section) {
        List<CnfDevice> cnfDevices = new ArrayList<>();
        Iterator<String> keyIter = m_sections.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_sections.get(key).getSectionId().equals(section)) {
                CnfDevice cnfDevice = getDeviceById(m_sections.get(key).getDeviceId());
                cnfDevice.setPositionId(Integer.parseInt(m_sections.get(key).getPositionId()));
                cnfDevices.add(cnfDevice);
            }
        }
        // Sort CnfDevices on Position -- To be added
        Collections.sort(cnfDevices);
        return cnfDevices;
    }


    /*
     * Return the list of CnfDevices
     */
    public HashMap<String, CnfDevice> getAllDevices() { return m_devices; }

	public void setDevices(HashMap<String, CnfDevice> devices) {
		this.m_devices = devices;
	}

	/*
     * A default device is used to indicate that a specific device doesn't exist
     */
    public CnfDevice getDefaultDevice() {
        return new CnfDevice(DEFAULT_DEVICEID, "ERROR", "ERROR", "ERROR",0, 0, "ERROR");
    }

    /*
     * Return all CnfDevice instances as a JSON string.
     */
    public String getAllDevicesAsJSON() {
        Gson gson = new Gson();
        SortedSet<String> keys = new TreeSet<String>(m_devices.keySet());
        Map<String,CnfDevice> sorted_mDevices = new TreeMap<>();
        for(String key:keys){
            sorted_mDevices.put(key,m_devices.get(key));
        }
        return gson.toJson(sorted_mDevices);
    }

    /*
     * Return all CnfDevice instances as a JSON string sorted by sections.
     */
    public String getAllDevicesAsJSONSectionSort() {
        Gson gson = new Gson();
        LinkedHashMap<String,CnfDevice> sorted_mDevice = new LinkedHashMap<>();


        Iterator<String> keyIter = m_sections.keySet().iterator();
        List<String> sectionList = new ArrayList<>();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            //System.out.println("key-" + key + " :section id: " +  m_sections.get(key).getSectionId());

            sectionList.add(m_sections.get(key).getSectionId());



        }

        Set<String> uniqueSectionSet = new HashSet<>(sectionList);
        for(String section : uniqueSectionSet){
            //System.out.println("Size of Unique Section" + uniqueSectionSet.size() + " section:" + section);
            List<CnfDevice> devices = getDevicesBySectionId(section);
            for(CnfDevice device : devices){
                //System.out.println("device " + device.getDeviceId());
                sorted_mDevice.put(device.getDeviceId(),device);
            }

        }






      /*  SortedSet<String> keys = new TreeSet<String>(m_devices.keySet());
        Map<String,CnfDevice> sorted_mDevices = new TreeMap<>();
        for(String key:keys){
            sorted_mDevices.put(key,m_devices.get(key));
        }*/
      //System.out.println("json" + gson.toJson(sorted_mDevice));
        return gson.toJson(sorted_mDevice);
    }

    /*
     * Return the CnfView instance specified by viewId or null.
     */
    public CnfView getViewById(String viewId) {
        Iterator<String> keyIter = m_views.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_views.get(key).getViewId().equals(viewId)) {
                return m_views.get(key);
            }
        }
        return null;
    }

    /*
     * Return the CnfView instance specified by viewName or null.
     */
    public CnfView getViewByName(String viewName) {
        Iterator<String> keyIter = m_views.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_views.get(key).getViewName().equals(viewName)) {
                return m_views.get(key);
            }
        }
        return null;
    }

    /*
     * Return the list of CnfViews
     */
    public HashMap<String, CnfView> getAllViews() { return m_views; }

	public void setViews(HashMap<String, CnfView> views) {
		this.m_views = views;
	}

    /*
     * A default view is used to indicate that a specific view doesn't exist (ERROR)
     * or that the associated device is blank
     */
    public CnfView getDefaultView() {
        return new CnfView(DEFAULT_VIEWID, "BLANK", "BLANK", 0, 0,"BLANK", new String[0]);
    }

    /*
     * Return all CnfView instances as a JSON string.
     */
    public String getAllViewsAsJSON() {
        Gson gson = new Gson();
        SortedSet<String> keys = new TreeSet<String>(m_views.keySet());
        Map<String,CnfView> sorted_mViews = new TreeMap<>();
        for(String key:keys){
            sorted_mViews.put(key,m_views.get(key));
        }
        return gson.toJson(sorted_mViews);
    }

    /*
     * Return the CnfLink instance specified by deviceId or null.
     */
    public CnfLink getLink(String deviceId) {
        Iterator<String> keyIter = m_links.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_links.get(key).getDeviceId().equals(deviceId)) {
                return m_links.get(key);
            }
        }
        return null;
    }

    /*
     * Return the list of CnfViews
     */
    public HashMap<String, CnfLink> getAllLinks() { return m_links; }

    public void setLinks(HashMap<String, CnfLink> links) { this.m_links = links; }

    /*
     * Update the link with a new viewId. A viewId of '0' means that the link will be deleted.
     */
    public void updateLink(String deviceId, String viewId) {
        if (viewId.equals(DEFAULT_VIEWID)) {
            // Remove the link
            m_links.remove(deviceId);
        } else {
            // Update the link
            CnfLink link = getLink(deviceId);
            if (null != link) {
                link.setViewId(viewId);
            } else {
                // Link doesn't exist yet
                // Validate deviceId and viewId
                if (null == getDeviceById(deviceId) || null == getViewById(viewId)) {
                    System.out.println("ERROR in LINK config data: deviceId " + deviceId + " viewId " + viewId);
                } else {
                    m_links.put(deviceId, new CnfLink(deviceId, viewId));
                }
            }
        }

        updateSourceWithLinkChanges();
    }

    /*
     * Update both the database and the XML file with the changes
     */
    private void updateSourceWithLinkChanges() {
        // Update the database
        if (!writeLinksToDatabase()) {
            System.out.println("ERROR: failed to write link updates to database");
        }
    }

    /*
     * Return all CnfLink instances as a JSON string.
     */
    public String getAllLinksAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(m_links);
    }

    /*
     * Return the CnfSection instance specified by deviceId or null.
     */
    public CnfSection getSectionByDeviceId(String deviceId) {
        Iterator<String> keyIter = m_sections.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            if (m_sections.get(key).getDeviceId().equals(deviceId)) {
                return m_sections.get(key);
            }
        }
        return null;
    }


    /*
     * Return the list of CnfSections
     */
    public HashMap<String, CnfSection> getAllSections() { return m_sections; }

	public void setSections(HashMap<String, CnfSection> sections) {
		this.m_sections = sections;
	}

    /*
     * Return all CnfSection instances as a JSON string.
     */
    public String getAllSectionsAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(m_sections);
    }

    /*
     * Validate the data. Used for validating the links after XML configuration data has been read.
     * Any links that have a non exiting deviceId or viewId are removed.
     */
    public void validateAndFix() {

		Iterator<String> keyIter = m_links.keySet().iterator();

		while (keyIter.hasNext()) {
			String key = keyIter.next();
			String deviceId = m_links.get(key).getDeviceId();
			String viewId = m_links.get(key).getViewId();

			if (null == getDeviceById(deviceId) || null == getViewById(viewId)) {
				keyIter.remove(); // TODO: test this, it could cause a crash
			}
		}
	}

    /*
     * Debugging functions
     */

    public void printDevices() {
        System.out.println("******** DEVICES *********");
        Iterator<String> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            m_devices.get(key).print();
        }
        System.out.println("****** END DEVICES *******");
    }

    public void printViews() {
        System.out.println("********* VIEWS **********");
        Iterator<String> keyIter = m_views.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            m_views.get(key).print();
        }
        System.out.println("******* END VIEWS ********");
    }

    public void printLinks() {
        System.out.println("********* LINKS **********");
        Iterator<String> keyIter = m_links.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            m_links.get(key).print();
        }
        System.out.println("******* END LINKS ********");
    }

    public void printSections() {
        System.out.println("******** SECTIONS ********");
        Iterator<String> keyIter = m_sections.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            m_sections.get(key).print();
        }
        System.out.println("****** END SECTIONS ******");
    }
    //

    /*
     * DATABASE FUNCTIONS
     */
    public boolean readFromDatabase() {
        boolean success = false;
        System.out.println("Initialising CnfData from Db");
        final CnfDao cnfDao = H2DatabaseAccess.getCnfDao();
		if (cnfDao == null) {
            System.out.println("ERROR in readFromDatabase : Failed to get database access");
        } else {
			try {
				m_devices = cnfDao.getAllCnfDevices();
				m_views = cnfDao.getAllCnfViews();
				m_links = cnfDao.getAllDeviceViewLinks();
				m_sections = cnfDao.getAllSections();
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        return success;
    }

    private boolean writeToDatabase() {
        boolean success = false;
        final CnfDao cnfDao = H2DatabaseAccess.getCnfDao();
		if (cnfDao == null) {
            System.out.println("ERROR in writeToDatabase : Failed to get database access");
        } else {
			try {
				cnfDao.setAllCnfDevices(m_devices);
				cnfDao.setAllCnfViews(m_views);
				cnfDao.setAllCnfViewLinks(m_views);
				cnfDao.setAllCnfLinks(m_links);
				cnfDao.setAllCnfSections(m_sections);
				success = true;
			} catch (SQLException se) {
				log.error(se.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return success;
    }

    private boolean writeLinksToDatabase() {
        boolean success = false;
        final CnfDao cnfDao = H2DatabaseAccess.getCnfDao();
        if (cnfDao == null) {
            System.out.println("ERROR in writeLinksToDatabase : Failed to get database access");
        } else {
			try {
				cnfDao.setAllCnfLinks(m_links);
				success = true;
			} catch (SQLException se) {
				log.error(se.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return success;
    }
}
