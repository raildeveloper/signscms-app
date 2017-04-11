package au.gov.nsw.sydneytrains.controller;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured links between device and view: the view can be changed.
 * A viewId of '0' means no view / blank
 */
public class CnfLink {

    private final int deviceId;
    private int viewId;

    public CnfLink(int deviceId, int viewId) {
        this.deviceId = deviceId;
        this.viewId = viewId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }
}
