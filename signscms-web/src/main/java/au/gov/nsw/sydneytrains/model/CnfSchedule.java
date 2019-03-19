package au.gov.nsw.sydneytrains.model;

import java.util.Date;

public class CnfSchedule {


    private String cnfScheduleId;
    private String cnfDevice;
    private String previousCnfView;
    private String scheduleCnfView;
    private Date startTime;
    private Date endTime;
    private String status;
    private String isDeleted;
    private String username;

    public String getCnfScheduleId() {
        return cnfScheduleId;
    }

    public void setCnfScheduleId(String cnfScheduleId) {
        this.cnfScheduleId = cnfScheduleId;
    }

    public String getCnfDevice() {
        return cnfDevice;
    }

    public void setCnfDevice(String cnfDevice) {
        this.cnfDevice = cnfDevice;
    }

    public String getPreviousCnfView() {
        return previousCnfView;
    }

    public void setPreviousCnfView(String previousCnfView) {
        this.previousCnfView = previousCnfView;
    }

    public String getScheduleCnfView() {
        return scheduleCnfView;
    }

    public void setScheduleCnfView(String scheduleCnfView) {
        this.scheduleCnfView = scheduleCnfView;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
