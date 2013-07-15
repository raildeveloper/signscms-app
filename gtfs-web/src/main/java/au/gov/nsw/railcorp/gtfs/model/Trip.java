// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.model;

import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trip - Used for Time Predictions.
 * @author paritosh
 */
public class Trip {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    // ** Trip prediction algorithm settings **

    // Should delay forecasts be cascaded to the next trip in the block?
    private static final boolean CASCADE_DELAY = true;

    // Should early running times be published in the feed?
    private static final boolean PUBLISH_TIME_PREDICTIONS = true;

    private static final int TIME_CAL = 1000;

    private static final int TIME_NEGATIVE_HOUR = -3600;

    private static final int TIME_POSITIVE_HOUR = 3600;

    private static final double DISTANCE_CONST = 180.0;

    private static final long MILLISECOND_IN_SECOND = 1000L;

    private String tripId;

    // GTFS Trip attributes
    private String routeId;

    private String serviceId;

    private String headsign;

    private int directionId;

    private String blockId;

    private String shapeId;

    // timestamp of last recorded vehicle position
    private Long recordedTimeStamp;

    private String timeStampLocal;

    // reference to TripDescriptor received via GTFSRVehiclePosition feed
    private TripDescriptor tripDescriptor;

    // set of stops that this service will operate through
    private List<TripStop> tripStops;

    // If the trip is within 250m radius of a stop, store it here
    private TripStop currentStop;

    // The last stop passed by the vehicle operating this trip
    private TripStop lastStop;

    // The next planned stop this trip will pass
    private TripStop nextStop;

    // The next trip in the block
    private Trip nextTrip;

    // The current delay time in seconds
    private double currentDelay = Double.MAX_VALUE;

    public String getTripId() {

        return tripId;
    }

    public void setTripId(String trip) {

        this.tripId = trip;
    }

    public TripDescriptor getTripDescriptor() {

        return tripDescriptor;
    }

    public void setTripDescriptor(TripDescriptor tripDesc) {

        this.tripDescriptor = tripDesc;
    }

    public Long getRecordedTimeStamp() {

        return recordedTimeStamp;
    }

    /**
     * Set Recorded Time Stamp.
     * @param recTimeStamp
     *            timestamp
     */
    public void setRecordedTimeStamp(Long recTimeStamp) {

        this.recordedTimeStamp = recTimeStamp;
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        final Date date = new Date(recordedTimeStamp * MILLISECOND_IN_SECOND);

        this.timeStampLocal = dateFormat.format(date);
    }

    public List<TripStop> getTripStops() {

        return tripStops;
    }

    public void setTripStops(List<TripStop> tripStop) {

        this.tripStops = tripStop;
    }

    /**
     * hasTripStops.
     * @return success
     */
    // Return if this Trip has tripStops defined
    public boolean hasTripStops() {

        return tripStops != null && tripStops.size() > 0;
    }

    /**
     * toString.
     * @return stringtrip
     */
    public String toString() {

        return "Trip Id : " + this.tripId + "\t" + "Recorded Time Stamp : "
        + this.recordedTimeStamp + "\t" + this.timeStampLocal + "\n"
        + toStringTripStops();
    }

    /**
     * toStringTripStops.
     * @return stringtriptoStops
     */
    public String toStringTripStops() {

        final StringBuilder output = new StringBuilder();
        final Iterator<TripStop> iterator = this.tripStops.iterator();
        while (iterator.hasNext()) {
            final TripStop stops = iterator.next();
            output.append("\n");
            output.append(stops.toString());
        }
        return output.toString();
    }

    public TripStop getCurrentStop() {

        return currentStop;
    }

    public void setCurrentStop(TripStop currStop) {

        this.currentStop = currStop;
    }

    public TripStop getLastStop() {

        return lastStop;
    }

    public void setLastStop(TripStop laststop) {

        this.lastStop = laststop;
    }

    public TripStop getNextStop() {

        return nextStop;
    }

    public void setNextStop(TripStop nextstop) {

        this.nextStop = nextstop;
    }

    public double getCurrentDelay() {

        return currentDelay;
    }

    public void setCurrentDelay(double currentdelay) {

        this.currentDelay = currentdelay;
    }

    /**
     * calculateDistanceForStops.
     * @param vp
     *            vehicleposition.
     */
    // Update the distance from the vehicle to each stop
    public void calculateDistanceForStops(VehiclePosition vp) {

        final Position position = vp.getPosition();
        for (TripStop tripStop : tripStops) {
            final double distance = distance(Double.valueOf(position.getLatitude()),
            Double.valueOf(position.getLongitude()),
            Double.valueOf(tripStop.getStopLatitude()),
            Double.valueOf(tripStop.getStopLongt()), 'K');
            tripStop.setDistanceFromCurrent(distance);
        }
    }

    /**
     * findNearestStop.
     * @return tripstop
     */
    // Return the nearest stop to the current vehicle position
    public TripStop findNearestStop() {

        TripStop bestStop = null;
        double minDistance = Double.MAX_VALUE;
        for (TripStop tripStop : tripStops) {
            if (tripStop.getDistanceFromCurrent() < minDistance) {
                bestStop = tripStop;
                minDistance = tripStop.getDistanceFromCurrent();
            }
        }
        return bestStop;
    }

    /**
     * findNextStop.
     * @return tripstop
     */
    // Return the stop after the specified stop, or null if none
    public TripStop findNextStop() {

        final int currentIndex = tripStops.indexOf(currentStop);
        final int lastIndex = tripStops.indexOf(lastStop);
        if (currentIndex == 0 || (lastIndex > -1 && lastIndex == currentIndex - 1)) {
            return (currentIndex + 1 < tripStops.size()) ? tripStops.get(currentIndex + 1) : null;
        } else {
            return null;
        }
    }

    public Trip getNextTrip() {

        return nextTrip;
    }

    public void setNextTrip(Trip nexttrip) {

        this.nextTrip = nexttrip;
    }

    /**
     * hasStarted.
     * @return success.
     */
    // Return whether this service has commenced operation
    public boolean hasStarted() {

        // Check if service is scheduled to have commenced operation
        final Date serviceDeparts = tripStops.get(0).getScheduledDepartureTime();
        final Date now = new Date();
        if (serviceDeparts.getTime() < now.getTime()) {
            return false;
        }
        // Return true if we are at or have passed a waypoint station
        return lastStop != null || currentStop != null;
    }

    /**
     * hasCompleted.
     * @return success.
     */
    // Return whether this service has completed operation
    public boolean hasCompleted() {

        return (lastStop != null) && (nextStop == null);
    }

    /**
     * hasvalidDelayPredictions.
     * @return success.
     */
    // Return whether this trip has a valid delay prediction available
    @SuppressWarnings("unused")
    public boolean hasValidDelayPrediction() {

        // Undefined delay is stored as MAX_VALUE
        if (currentDelay == Double.MAX_VALUE) {
            return false;
        }
        // If delay is early on-time running, and we are forbidden from publishing that, return invalid
        if (currentDelay < 0 && !PUBLISH_TIME_PREDICTIONS) {
            return false;
        }
        // Stop providing delay forecasting when delay is over an hour
        if (currentDelay <= TIME_NEGATIVE_HOUR || currentDelay >= TIME_POSITIVE_HOUR) {
            return false;
        }

        // Service must be a future prediction, or have reached a waypoint to provide a valid forecast
        return !this.hasStarted() || currentStop != null || nextStop != null;
    }

    /**
     * calculateDelayForVehicle.
     * @param vp
     *            vehiclePostion
     */
    public void calculateDelayForVehicle(VehiclePosition vp) {

        // Plot a 250m radius around each platform to determine vehicle currently at station
        // (250m found through trial and error, an 8 car train is 163m long but track circuits and
        // stops don't always perfectly align eg look at a service stopped at Domestic Airport)
        final double maxProximity = 0.25;

        // Convert recorded timestamp to Date object
        final Date date = new Date(recordedTimeStamp * MILLISECOND_IN_SECOND);

        // Recalculate vehicle positioning
        this.calculateDistanceForStops(vp);
        final TripStop nearestStop = this.findNearestStop();

        // Determine if vehicle is currently at the stop
        final boolean currentlyAtStop = nearestStop != null && nearestStop.getDistanceFromCurrent() <= maxProximity;

        if (!currentlyAtStop && currentStop != null) {
            // Vehicle has moved away from the current stop
            lastStop = currentStop;
            currentStop = null;

            // Update delay based on departure time
            this.setDelayBasedOnStop(lastStop, false);
        } else if (currentlyAtStop) {
            if (currentStop == null) {
                // Vehicle has just arrived at the current stop
                currentStop = nearestStop;
                currentStop.setActualArrivalTime(date);
                nextStop = this.findNextStop();
            } else if (nearestStop != currentStop) {
                // Vehicle position has jumped between stations
                lastStop = currentStop;
                currentStop = nearestStop;
                currentStop.setActualArrivalTime(date);
                nextStop = this.findNextStop();
            }

            // Update stop departed timestamp while the vehicle remains at the stop
            nearestStop.setActualDepartureTime(date);

            // Update delay based on current time vs planned departure time
            this.setDelayBasedOnStop(currentStop, true);
        }

        log.info(tripId + " current delay: " + String.valueOf(currentDelay)
        + "\n -- last: " + lastStop + "\n -- now: " + currentStop + "\n -- next: " + nextStop);
    }

    // Update the delay for the vehicle based on it passing a stop
    private void setDelayBasedOnStop(TripStop fromStop, boolean vehicleIsAtStop)
    {

        // cast planned and actual arrival/departure timestamps to doubles in seconds
        final double plannedArrival = (fromStop.getScheduledArrivalTime() != null) ? fromStop.getScheduledArrivalTime().getTime()
        / TIME_CAL : 0;
        final double plannedDeparture = (fromStop.getScheduledDepartureTime() != null) ? fromStop.getScheduledDepartureTime().getTime()
        / TIME_CAL : 0;
        final double actualArrival = (fromStop.getActualArrivalTime() != null) ? fromStop.getActualArrivalTime().getTime() / TIME_CAL : 0;
        final double actualDeparture = (fromStop.getActualDepartureTime() != null) ? fromStop.getActualDepartureTime().getTime() / TIME_CAL
        : 0;
        double arrivalDelay = actualArrival - plannedArrival;
        final double departureDelay = actualDeparture - plannedDeparture;
        // double plannedDwellTime = plannedDeparture - plannedArrival;

        // if service is currently at a stop and is not yet scheduled to have departed,
        // assume service will depart on time
        if (vehicleIsAtStop && actualDeparture < plannedDeparture) {
            arrivalDelay = Math.max(0, arrivalDelay);

            // if arrival delay is an improvement on the last known delay, then propagate that
            if (Math.abs(arrivalDelay) < Math.abs(currentDelay)) {
                currentDelay = arrivalDelay;
            }
        } else if (!vehicleIsAtStop || actualDeparture >= plannedDeparture) {
            // if vehicle has just departed a stop, or is still at the stop + has not yet
            // departed + is delayed, then revise current delay for trip
            currentDelay = departureDelay;
        }
    }

    /**
     * cascadeDelayFromPreviousTrip.
     * @param previousTrip
     *            previousTrip
     */
    // Propagate the delay from the specified previous trip onto this one
    public void cascadeDelayFromPreviousTrip(Trip previousTrip) {

        if (previousTrip.hasValidDelayPrediction() && CASCADE_DELAY) {
            currentDelay = Math.max(0, previousTrip.getCurrentDelay() - this.deltaTimeSinceTrip(previousTrip));
            recordedTimeStamp = previousTrip.getRecordedTimeStamp();
        }
    }

    /**
     * deltaTimeSinceTrip.
     * @param trip
     *            trip
     * @return trip
     */
    // Return the delta of seconds between the end of the specified trip and the start of this trip
    public double deltaTimeSinceTrip(Trip trip) {

        final Date lastTripEnds = trip.scheduledEndTime();
        final Date thisTripStarts = this.scheduledStartTime();
        if (lastTripEnds == null || thisTripStarts == null) {
            return 0;
        }
        return (thisTripStarts.getTime() - lastTripEnds.getTime()) / MILLISECOND_IN_SECOND;
    }

    /**
     * scheduledStartTime.
     * @return date.
     */
    // Time this trip is scheduled to commence operation
    public Date scheduledStartTime() {

        if (tripStops == null || tripStops.size() == 0) {
            return null;
        }
        final TripStop firstStop = tripStops.get(0);
        return firstStop.getScheduledDepartureTime();
    }

    /**
     * scheduledEndTime.
     * @return date.
     */
    // Time this trip is scheduled to cease operating
    public Date scheduledEndTime() {

        if (tripStops == null || tripStops.size() == 0) {
            return null;
        }
        final TripStop laststop = tripStops.get(tripStops.size() - 1);
        return laststop.getScheduledArrivalTime();
    }

    /*
     * Distance calculation helper methods
     */
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {

        final double theta = lon1 - lon2;
        final int a = 60;
        final double b = 1.1515;
        final double c = 1.609344;
        final double d = 0.8684;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
        + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
        * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * a * b;
        if (unit == 'K') {
            dist = dist * c;
        } else if (unit == 'N') {
            dist = dist * d;
        }
        return dist;
    }

    private double deg2rad(double deg) {

        return deg * Math.PI / DISTANCE_CONST;
    }

    private double rad2deg(double rad) {

        return rad * DISTANCE_CONST / Math.PI;
    }

    public String getRouteId() {

        return routeId;
    }

    /**
     * setRouteId.
     * @param routeid
     *            routeid
     */
    public void setRouteId(String routeid) {

        this.routeId = routeid;

        // If we know the tripId and routeId, set up a placeholder TripDescriptor instance
        if (routeid != null && tripId != null) {
            final TripDescriptor.Builder trip = TripDescriptor.newBuilder();
            trip.setTripId(tripId);
            trip.setRouteId(routeid);
            trip.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED);
            tripDescriptor = trip.build();
        }
    }

    public String getServiceId() {

        return serviceId;
    }

    public void setServiceId(String serviceid) {

        this.serviceId = serviceid;
    }

    public String getHeadsign() {

        return headsign;
    }

    public void setHeadsign(String headSign) {

        this.headsign = headSign;
    }

    public int getDirectionId() {

        return directionId;
    }

    public void setDirectionId(int directionid) {

        this.directionId = directionid;
    }

    public String getBlockId() {

        return blockId;
    }

    public void setBlockId(String blockid) {

        this.blockId = blockid;
    }

    public String getShapeId() {

        return shapeId;
    }

    public void setShapeId(String shapeid) {

        this.shapeId = shapeid;
    }

}
