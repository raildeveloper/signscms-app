package au.gov.nsw.sydneytrains.controller;

import java.util.HashMap;
import java.util.Iterator;
import com.google.gson.Gson;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Holds the configuration data: device, view, link
 */
public class CnfData {

    private HashMap<Integer, CnfDevice> m_devices = new HashMap<Integer, CnfDevice>();
    private HashMap<Integer, CnfView> m_views = new HashMap<Integer, CnfView>();
    private HashMap<Integer, CnfLink> m_links = new HashMap<Integer, CnfLink>();

    public CnfData() {
        // Todo
    }

    /*
     * Reads the configuration from the specified source.
     */
    public boolean readFromSource() {
        // Todo : specify the source
        // Todo : read the data from the source
        // Temporary data
        Integer key = 1;
        m_devices.put(key, new CnfDevice(key, "TestPC1", "Test PC 1", 4*240, 240));
        key = 2;
        m_devices.put(key, new CnfDevice(key, "TestPC2", "Test PC 2", 8*240, 240));
        printDevices();

        String[] images = new String[]{"P5 blue.png", "P6 blue.png", "Stairs Down blue.png", "Arrow Down blue.png"};
        key = 3;
        m_views.put(key, new CnfView(key, "TestView1", "Test View 1", 4*240, 240, images));
        key = 4;
        m_views.put(key, new CnfView(key, "TestView2", "Test View 2", 8*240, 240, images));
        printViews();

        Integer deviceId = 1, viewId = 3;
        m_links.put(deviceId, new CnfLink(deviceId, viewId));
        deviceId = 2;
        m_links.put(deviceId, new CnfLink(deviceId, 0));
        printLinks();
        // End Temporary data
        return true;
    }

    /*
     * Writes the (updated) configuration back to the specified source.
     */
    public boolean writeToSource() {
        // Todo : specify the source
        // Todo : write the data to the source
        return false;
    }

    /*
     * Return the CnfDevice instance specified by deviceId or null.
     */
    public CnfDevice getDevice(Integer deviceId) {
        Iterator<Integer> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            if (m_devices.get(key).getDeviceId() == deviceId) {
                return m_devices.get(key);
            }
        }
        return null;
    }

    /*
     * Return the CnfDevice instance specified by deviceName or null.
     */
    public CnfDevice getDevice(String deviceName) {
        Iterator<Integer> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            if (m_devices.get(key).getDeviceName() == deviceName) {
                return m_devices.get(key);
            }
        }
        return null;
    }

    /*
     * Return all CnfDevice instances as a JSON string.
     */
    public String getAllDevicesAsJSON() {
        Gson gson = new Gson();
        return gson.toJSON(m_devices);
    }

    /*
     * Return the CnfView instance specified by viewId or null.
     */
    public CnfView getView(Integer viewId) {
        Iterator<Integer> keyIter = m_views.keySet().iterator();

        while(keyIter.hasNext()) {
            Integer key = keyIter.next();
            if (m_views.get(key).getViewId() == viewId) {
                return m_views.get(key);
            }
        }
        return null;
    }

    /*
     * Return the CnfView instance specified by viewName or null.
     */
    public CnfView getView(String viewName) {
        Iterator<Integer> keyIter = m_views.keySet().iterator();

        while(keyIter.hasNext()) {
            Integer key = keyIter.next();
            if (m_views.get(key).getViewName() == viewName) {
                return m_views.get(key);
            }
        }
        return null;
    }

    /*
     * Return all CnfView instances as a JSON string.
     */
    public String getAllViewsAsJSON() {
        Gson gson = new Gson();
        return gson.toJSON(m_views);
    }

    /*
     * Return the CnfLink instance specified by deviceId or null.
     */
    public CnfLink getLink(Integer deviceId) {
        Iterator<Integer> keyIter = m_links.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            if (m_links.get(key).getDeviceId() == deviceId) {
                return m_links.get(key);
            }
        }
        return null;
    }

    /*
     * Return all CnfLink instances as a JSON string.
     */
    public String getAllLinksAsJSON() {
        Gson gson = new Gson();
        return gson.toJSON(m_links);
    }

    /*
     * Debugging functions
     */

    public void printDevices() {
        System.out.println("******** DEVICES *********");
        Iterator<Integer> keyIter = m_devices.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            m_devices.get(key).print();
        }
        System.out.println("****** END DEVICES *******");
    }

    public void printViews() {
        System.out.println("********* VIEWS **********");
        Iterator<Integer> keyIter = m_views.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            m_views.get(key).print();
        }
        System.out.println("******* END VIEWS ********");
    }

    public void printLinks() {
        System.out.println("********* LINKS **********");
        Iterator<Integer> keyIter = m_links.keySet().iterator();

        while (keyIter.hasNext()) {
            Integer key = keyIter.next();
            m_links.get(key).print();
        }
        System.out.println("******* END LINKS ********");
    }
    //
}
