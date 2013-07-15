// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TripStop.
 * @author paritosh
 *
 */
public class TripStop {

    private String stopId;

    private String stopSequence;

    private String arrivalTime;

    private String departureTime;

    private String stopLatitude;

    private String stopLongt;

    private double distanceFromCurrent;

    private Date scheduledArrivalTime;

    private Date scheduledDepartureTime;

    private Date actualArrivalTime;

    private Date actualDepartureTime;

    public String getStopId() {

        return stopId;
    }

    public void setStopId(String stopid) {

        this.stopId = stopid;
    }

    public String getStopSequence() {

        return stopSequence;
    }

    public void setStopSequence(String stopsequence) {

        this.stopSequence = stopsequence;
    }

    public String getArrivalTime() {

        return arrivalTime;
    }

    /**
     * setArrivalTime.
     * @param arrivaltime time
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
     * @param departuretime time
     */
    public void setDepartureTime(String departuretime) {

        this.departureTime = departuretime;
        this.scheduledDepartureTime = this.gtfsTimeToDate(departuretime);
    }

    /**
     * gtfsTimeToDate.
     * @param time time
     * @return date
     */
    public Date gtfsTimeToDate(String time) {

        final int timepartnumber = 3;
        final int hour23 = 23;
        final int hour24 = 24;
        // split time of format HH:mm:ss into array
        final String[] timeParts = time.split(":");
        if (timeParts.length != timepartnumber) {
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

    public void setDistanceFromCurrent(double distancefromcurrent) {

        this.distanceFromCurrent = distancefromcurrent;
    }

    /**
     * toString.
     * @return tripStop.
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

    public Date getActualDepartureTime() {

        return actualDepartureTime;
    }

    public void setActualDepartureTime(Date actualdepartureTime) {

        this.actualDepartureTime = actualdepartureTime;
    }
}
