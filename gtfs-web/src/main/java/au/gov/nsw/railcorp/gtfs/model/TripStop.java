// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.model;

import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TripStop.
 * @author paritosh
 */
public class TripStop {

    private String stopId;

    private int stopSequence;

    private String stopName = "";

    private String arrivalTime;

    private String departureTime;

    private String stopLatitude = "0";

    private String stopLongt = "0";

    private double distanceFromCurrent;

    private Date scheduledArrivalTime;

    private Date scheduledDepartureTime;

    private Date actualArrivalTime;

    private Date actualTimeAtStop;

    private Date actualDepartureTime;

    private Date predictedArrivalTime;

    private Date predictedDepartureTime;

    private ScheduleRelationship scheduleRelationship = ScheduleRelationship.SCHEDULED;

    public String getStopId() {

        return stopId;
    }

    public void setStopId(String stopid) {

        this.stopId = stopid;
    }

    public String getStopName() {

        return stopName;
    }

    public void setStopName(String stopname) {

        this.stopName = stopname;
    }

    public int getStopSequence() {

        return stopSequence;
    }

    public void setStopSequence(int stopsequence) {

        this.stopSequence = stopsequence;
    }

    public String getArrivalTime() {

        return arrivalTime;
    }

    /**
     * setArrivalTime.
     * @param arrivaltime
     *            at
     */
    public void setArrivalTime(String arrivaltime) {

        this.arrivalTime = arrivaltime;
        this.scheduledArrivalTime = this.gtfsTimeToDate(arrivaltime);
    }

    public String getDepartureTime() {

        return departureTime;
    }

    /**
     * setDepartureTime.
     * @param departuretime
     *            dt.
     */
    public void setDepartureTime(String departuretime) {

        this.departureTime = departuretime;
        this.scheduledDepartureTime = this.gtfsTimeToDate(departuretime);
    }

    /**
     * gtfsTimeToDate.
     * @param time
     *            time
     * @return date
     */
    public Date gtfsTimeToDate(String time) {

        final int split = 3;
        final int hour23 = 23;
        final int hour24 = 24;
        // split time of format HH:mm:ss into array
        final String[] timeParts = time.split(":");
        if (timeParts.length != split) {
            return null;
        }

        // convert times to int's
        int hour = Integer.valueOf(timeParts[0]);
        final int mins = Integer.valueOf(timeParts[1]);
        final int secs = Integer.valueOf(timeParts[2]);

        // deal with GTFS supporting times beyond 23:59:59 (offset to yesterday)
        final Calendar calendar = new GregorianCalendar();
        if (hour > hour23) {
            hour -= hour24;
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        // convert int's to calendar representing current day
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, mins);
        calendar.set(Calendar.SECOND, secs);

        return calendar.getTime();
    }

    public String getStopLatitude() {

        return stopLatitude;
    }

    public void setStopLatitude(String stoplatitude) {

        this.stopLatitude = stoplatitude;
    }

    public String getStopLongt() {

        return stopLongt;
    }

    public void setStopLongt(String stoplongt) {

        this.stopLongt = stoplongt;
    }

    public double getDistanceFromCurrent() {

        return distanceFromCurrent;
    }

    public void setDistanceFromCurrent(double distancefromCurrent) {

        this.distanceFromCurrent = distancefromCurrent;
    }

    /**
     * toString.
     * @return string
     */
    public String toString() {

        return "Stop Id : " + this.stopId + "\t" + " Arrival Time : "
        + this.arrivalTime + "\t" + " Departure Time : "
        + this.departureTime + "\t" + " Stop Lat : "
        + this.stopLatitude + "\t" + " Stop Long : " + this.stopLongt
        + "\t" + " Seq : " + this.stopSequence + "\t" + " Distance : "
        + this.distanceFromCurrent;

    }

    public Date getScheduledArrivalTime() {

        return scheduledArrivalTime;
    }

    public Date getScheduledDepartureTime() {

        return scheduledDepartureTime;
    }

    public Date getActualArrivalTime() {

        return actualArrivalTime;
    }

    public void setActualArrivalTime(Date actualarrivalTime) {

        this.actualArrivalTime = actualarrivalTime;
    }

    public Date getActualTimeAtStop() {

        return actualTimeAtStop;
    }

    public void setActualTimeAtStop(Date actualtimeAtStop) {

        this.actualTimeAtStop = actualtimeAtStop;
    }

    public Date getActualDepartureTime() {

        return actualDepartureTime;
    }

    public void setActualDepartureTime(Date actualdepartureTime) {

        this.actualDepartureTime = actualdepartureTime;
    }

    public Date getPredictedDepartureTime() {

        return predictedDepartureTime;
    }

    public void setPredictedDepartureTime(Date predicteddepartureTime) {

        this.predictedDepartureTime = predicteddepartureTime;
    }

    public Date getPredictedArrivalTime() {

        return predictedArrivalTime;
    }

    public void setPredictedArrivalTime(Date predictedarrivalTime) {

        this.predictedArrivalTime = predictedarrivalTime;
    }

    /**
     * getAnticipatedDwellTime.
     * @return long
     */
    // Determine the dwell time at this stop (in seconds)
    public long getAnticipatedDwellTime() {

        final int timecal = 1000;
        final long plannedArrival = (scheduledArrivalTime != null) ? scheduledArrivalTime.getTime() / timecal : 0;
        final long plannedDeparture = (scheduledDepartureTime != null) ? scheduledDepartureTime.getTime() / timecal : 0;
        return plannedDeparture - plannedArrival;
    }

    /**
     * getArrivalDelay.
     * @return long
     */
    // Return the seconds of delay that occurred or is predicted to occur at this stop
    public long getArrivalDelay() {

        final int timecal = 1000;
        if (actualArrivalTime != null && scheduledArrivalTime != null) {
            return (actualArrivalTime.getTime() - scheduledArrivalTime.getTime()) / timecal;
        }
        if (predictedArrivalTime != null && scheduledArrivalTime != null) {
            return (predictedArrivalTime.getTime() - scheduledArrivalTime.getTime()) / timecal;
        }
        return 0;
    }

    /**
     * getDepartureDelay.
     * @return long
     */
    public long getDepartureDelay() {

        final int timecal = 1000;
        if (actualDepartureTime != null && scheduledDepartureTime != null) {
            return (actualDepartureTime.getTime() - scheduledDepartureTime.getTime()) / timecal;
        }
        if (predictedDepartureTime != null && scheduledDepartureTime != null) {
            return (predictedDepartureTime.getTime() - scheduledDepartureTime.getTime()) / timecal;
        }
        return 0;
    }

    public ScheduleRelationship getScheduleRelationship() {

        return scheduleRelationship;
    }

    public void setScheduleRelationship(ScheduleRelationship schedulerelationship) {

        this.scheduleRelationship = schedulerelationship;
    }
}
