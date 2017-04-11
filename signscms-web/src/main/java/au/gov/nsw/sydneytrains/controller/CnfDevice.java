package au.gov.nsw.sydneytrains.controller;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured device: a pc that is configured to display a signage view
 */
public class CnfDevice {
    private final int deviceId;
    private final String deviceName;
    private final String description;
    private final int pixelsHorizontal;
    private final int pixelsVertical;

    public CnfDevice(final int deviceId, final String deviceName, final String description,
                     final int pixelsHorizontal, final int pixelsVertical)
    {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.description = description;
        this.pixelsHorizontal = pixelsHorizontal;
        this.pixelsVertical = pixelsVertical;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDescription() {
        return description;
    }

    public int getPixelsHorizontal() {
        return pixelsHorizontal;
    }

    public int getPixelsVertical() {
        return pixelsVertical;
    }
}
