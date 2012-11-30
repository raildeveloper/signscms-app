// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter.types;

/**
 * Represents common elements of CSV rows dealing with trips.
 * @author John
 */
public class TripBasedCsvRow {

    private String tripId;

    private String routeId;

    private String stopId;

    /**
     * Constructor.
     */
    public TripBasedCsvRow() {

        super();
    }

    /**
     * getter for tripId.
     * @return the tripId
     */
    public String getTripId() {

        return tripId;
    }

    /**
     * setter for tripId.
     * @param val
     *            the tripId to set
     */
    public void setTripId(String val) {

        this.tripId = val;
    }

    /**
     * getter for routeId.
     * @return the routeId
     */
    public String getRouteId() {

        return routeId;
    }

    /**
     * setter for routeId.
     * @param val
     *            the routeId to set
     */
    public void setRouteId(String val) {

        this.routeId = val;
    }

    /**
     * getter for stopId.
     * @return the stopId
     */
    public String getStopId() {

        return stopId;
    }

    /**
     * setter for stopId.
     * @param val
     *            the stopId to set
     */
    public void setStopId(String val) {

        this.stopId = val;
    }

}
