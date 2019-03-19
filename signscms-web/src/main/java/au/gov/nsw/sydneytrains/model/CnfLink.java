package au.gov.nsw.sydneytrains.model;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured links between device and view: the view can be changed.
 * A viewId of '0' means no view / blank
 */
public class CnfLink {

    private final String deviceId;
    private String viewId;

    public CnfLink(String deviceId, String viewId) {
        this.deviceId = deviceId;
        this.viewId = viewId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    // Debugging
    public void print() {
        System.out.println("deviceId: " + this.deviceId + " viewId: " + this.viewId);
    }
}
