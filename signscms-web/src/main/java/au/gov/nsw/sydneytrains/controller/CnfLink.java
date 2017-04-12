package au.gov.nsw.sydneytrains.controller;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured links between device and view: the view can be changed.
 * A viewId of '0' means no view / blank
 */
public class CnfLink {

    private final Integer deviceId;
    private Integer viewId;

    public CnfLink(Integer deviceId, Integer viewId) {
        this.deviceId = deviceId;
        this.viewId = viewId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public Integer getViewId() {
        return viewId;
    }

    public void setViewId(Integer viewId) {
        this.viewId = viewId;
    }

    // Debugging
    public void print() {
        System.out.println("deviceId: " + this.deviceId
                + " viewId: " + this.viewId);
    }
}
