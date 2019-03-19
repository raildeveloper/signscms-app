package au.gov.nsw.sydneytrains.model;

/**
 * Created by administrator on 1/5/17.
 */
public class CnfSection{


    private final String sectionId;
    private final String deviceId;
    private final String positionId;
    private final String v_offset;

    public CnfSection(String sectionId, String deviceId, String positionId, String v_offset) {
        this.sectionId = sectionId;
        this.deviceId = deviceId;
        this.positionId = positionId;
        this.v_offset = v_offset;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getPositionId() {
        return positionId;
    }

    public String getV_offset() { return v_offset; }

    // Debugging
    public void print() {
        System.out.println(getAsString());
    }

    public String getAsString() {
        String string = "sectionId: " + this.sectionId
                + " deviceId: " + this.deviceId
                + " positionId: " + this.positionId
                + " v_offset: " + this.v_offset;
        return string;
    }


}
