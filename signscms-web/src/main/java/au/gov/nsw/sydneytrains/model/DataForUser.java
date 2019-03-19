package au.gov.nsw.sydneytrains.model;

import java.util.HashMap;

/**
 * Created by Oscar on 24/04/2017.
 *
 * Contains the data for the response to the URL request of a user.
 */
public class DataForUser {

    private HashMap<String, CnfDevice> m_devices = new HashMap<>();
    private HashMap<String, CnfView> m_views = new HashMap<>();
    private HashMap<String, CnfLink> m_links = new HashMap<>();

    public DataForUser(HashMap<String, CnfDevice> devices, HashMap<String, CnfView> views, HashMap<String, CnfLink> links) {
        m_devices = devices;
        m_views = views;
        m_links = links;
    }
}
