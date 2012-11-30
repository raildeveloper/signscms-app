// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter.types;

import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;

import java.math.BigDecimal;

/**
 * Represents a single row from the Vehicle Position CSV file.
 * @author John
 */
public class VehiclePositionCsvRow extends TripBasedCsvRow {

    private String startTime;

    private String startDate;

    private BigDecimal scheduleRelationship;

    private String vehicleId;

    private String vehicleLabel;

    private String licensePlate;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal bearing;

    private BigDecimal odometer;

    private BigDecimal speed;

    private BigDecimal currentStopSequence;

    private BigDecimal currentStatus;

    private BigDecimal timestamp;

    private BigDecimal congestionLevel;

    /**
     * Constructor.
     */
    public VehiclePositionCsvRow() {

        super();
    }

    /**
     * getter for startTime.
     * @return the startTime
     */
    public String getStartTime() {

        return startTime;
    }

    /**
     * setter for startTime.
     * @param val
     *            the startTime to set
     */
    public void setStartTime(String val) {

        this.startTime = val;
    }

    /**
     * getter for startDate.
     * @return the startDate
     */
    public String getStartDate() {

        return startDate;
    }

    /**
     * setter for startDate.
     * @param val
     *            the startDate to set
     */
    public void setStartDate(String val) {

        this.startDate = val;
    }

    /**
     * getter for scheduleRelationship.
     * @return the scheduleRelationship
     */
    public BigDecimal getScheduleRelationship() {

        return scheduleRelationship;
    }

    /**
     * setter for scheduleRelationship.
     * @param val the scheduleRelationship to set
     */
    public void setScheduleRelationship(BigDecimal val) {

        this.scheduleRelationship = val;
    }

    /**
     * getter for vehicleId.
     * @return the vehicleId
     */
    public String getVehicleId() {

        return vehicleId;
    }

    /**
     * setter for vehicleId.
     * @param val
     *            the vehicleId to set
     */
    public void setVehicleId(String val) {

        this.vehicleId = val;
    }

    /**
     * getter for vehicleLabel.
     * @return the vehicleLabel
     */
    public String getVehicleLabel() {

        return vehicleLabel;
    }

    /**
     * setter for vehicleLabel.
     * @param val
     *            the vehicleLabel to set
     */
    public void setVehicleLabel(String val) {

        this.vehicleLabel = val;
    }

    /**
     * getter for licensePlate.
     * @return the licensePlate
     */
    public String getLicensePlate() {

        return licensePlate;
    }

    /**
     * setter for licensePlate.
     * @param val
     *            the licensePlate to set
     */
    public void setLicensePlate(String val) {

        this.licensePlate = val;
    }

    /**
     * getter for latitude.
     * @return the latitude
     */
    public BigDecimal getLatitude() {

        return latitude;
    }

    /**
     * setter for latitude.
     * @param val
     *            the latitude to set
     */
    public void setLatitude(BigDecimal val) {

        this.latitude = val;
    }

    /**
     * getter for longitude.
     * @return the longitude
     */
    public BigDecimal getLongitude() {

        return longitude;
    }

    /**
     * setter for longitude.
     * @param val
     *            the longitude to set
     */
    public void setLongitude(BigDecimal val) {

        this.longitude = val;
    }

    /**
     * getter for bearing.
     * @return the bearing
     */
    public BigDecimal getBearing() {

        return bearing;
    }

    /**
     * setter for bearing.
     * @param val
     *            the bearing to set
     */
    public void setBearing(BigDecimal val) {

        this.bearing = val;
    }

    /**
     * getter for odometer.
     * @return the odometer
     */
    public BigDecimal getOdometer() {

        return odometer;
    }

    /**
     * setter for odometer.
     * @param val
     *            the odometer to set
     */
    public void setOdometer(BigDecimal val) {

        this.odometer = val;
    }

    /**
     * getter for speed.
     * @return the speed
     */
    public BigDecimal getSpeed() {

        return speed;
    }

    /**
     * setter for speed.
     * @param val
     *            the speed to set
     */
    public void setSpeed(BigDecimal val) {

        this.speed = val;
    }

    /**
     * getter for currentStopSequence.
     * @return the currentStopSequence
     */
    public BigDecimal getCurrentStopSequence() {

        return currentStopSequence;
    }

    /**
     * setter for currentStopSequence.
     * @param val
     *            the currentStopSequence to set
     */
    public void setCurrentStopSequence(BigDecimal val) {

        this.currentStopSequence = val;
    }

    /**
     * getter for currentStatus.
     * @return the currentStatus
     */
    public BigDecimal getCurrentStatus() {

        return currentStatus;
    }

    /**
     * setter for currentStatus.
     * @param val
     *            the currentStatus to set
     */
    public void setCurrentStatus(BigDecimal val) {

        this.currentStatus = val;
    }

    /**
     * getter for timestamp.
     * @return the timestamp
     */
    public BigDecimal getTimestamp() {

        return timestamp;
    }

    /**
     * setter for timestamp.
     * @param val
     *            the timestamp to set
     */
    public void setTimestamp(BigDecimal val) {

        this.timestamp = val;
    }

    /**
     * getter for congestionLevel.
     * @return the congestionLevel
     */
    public BigDecimal getCongestionLevel() {

        return congestionLevel;
    }

    /**
     * setter for congestionLevel.
     * @param val
     *            the congestionLevel to set
     */
    public void setCongestionLevel(BigDecimal val) {

        this.congestionLevel = val;
    }

    /**
     * Determines if data contents exist sufficiently to create a GTFS-R VehiclePosition
     * object with any data.
     * @return true if contents exist for VehiclePosition data
     */
    public boolean positionContentsExist() {
        return     bearing != null
                || latitude != null
                || longitude != null
                || odometer != null
                || speed != null;
    }

    /**
     * Determines if data contents exist sufficiently to create a GTFS-R VehicleDescriptor
     * object with any data.
     * @return true if contents exist for VehicleDescriptor data
     */
    public boolean vehicleDescriptorContentsExist() {
        return vehicleId != null
            || vehicleLabel != null
            || licensePlate != null;
    }

    /**
     * Determines if data contents exist sufficiently to create a GTFS-R TripDescriptor
     * object with any data.
     * @return true if contents exist for TripDescriptor data
     */
    public boolean tripDescriptorContentsExist() {
        return getRouteId() != null
            || startDate != null
            || startTime != null
            || getTripId() != null
            || (scheduleRelationship != null && ScheduleRelationship.valueOf(scheduleRelationship.intValue()) != null);
    }

}
