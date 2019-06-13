package au.gov.nsw.sydneytrains.model;

/**
 * Created by Oscar on 10/04/2017.
 * <p>
 * Configured device: a pc that is configured to display a signage view
 */
public class CnfDevice implements Comparable<CnfDevice>{
    private final String deviceId;
    private final String deviceName;
    private final String description;
    private final String pi_name;
    private final int pixelsHorizontal;
    private final int pixelsVertical;
    private int positionId;
    private String location;

    public CnfDevice(final String deviceId, final String deviceName, final String description, final String pi_name,
                     final int pixelsHorizontal, final int pixelsVertical, final String location) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.description = description;
        this.pi_name = pi_name;
        this.pixelsHorizontal = pixelsHorizontal;
        this.pixelsVertical = pixelsVertical;
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDescription() {
        return description;
    }

    public String getPi_name() {
        return pi_name;
    }

    public int getPixelsHorizontal() {
        return pixelsHorizontal;
    }

    public int getPixelsVertical() {
        return pixelsVertical;
    }

    public String getLocation() { return  location;}

    // Debugging
    public void print() {
        System.out.println("deviceId: " + this.deviceId
                + " deviceName: " + this.deviceName
                + " description: " + this.description
                + " pi_name: " + this.pi_name
                + " pixels (HxV): " + this.pixelsHorizontal
                        + "x" + this.pixelsVertical + "location" + this.location);
    }

    public int getPositionId(){return positionId;}
    public void setPositionId(int positionId){
        this.positionId = positionId;
    }
    public int compareTo(CnfDevice cnfDevice){
        int comparePositionId = cnfDevice.getPositionId();
        return positionId - comparePositionId;
    }
}
