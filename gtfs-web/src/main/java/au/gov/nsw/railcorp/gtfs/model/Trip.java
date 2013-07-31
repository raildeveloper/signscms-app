// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.model;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class Trip {

    // **** Trip prediction algorithm settings ****

    // Should early running times be published in the feed?
    private static final boolean shouldPublishEarlyPredictions = true;

    // Should delay forecasts be cascaded to the next trip in the block?
    private static final boolean shouldCascadeDelayToNextTrip = true;

    // Should delay forecasts optimistically factor in the opportunity to
    // recover back to timetable based on excess station dwell times?
    private static final boolean shouldNegateDwellTimes = true;

    // What is the minimum required dwell time for a service at a stop? (in seconds)
    private static final long minimumDwellTime = 30L;

    // **** END Trip prediction algorithm settings ****

    private static final long MILLISECOND_IN_SECOND = 1000L;

    // The all important key for this service
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

    // track the number of times there has been no vehicle operating this trip in
    // the vehpos payload, use it to invalidate the cache record
    private int missedVehicleUpdates = 0;

    // reference to TripDescriptor received via GTFSRVehiclePosition feed
    private TripDescriptor tripDescriptor;

    // current position of vehicle operating trip
    private VehiclePosition vehiclePosition;

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
    private long currentDelay = Long.MAX_VALUE;

    public String getTripId() {

        return tripId;
    }

    public void setTripId(String tripId) {

        this.tripId = tripId;
    }

    public TripDescriptor getTripDescriptor() {

        return tripDescriptor;
    }

    public void setTripDescriptor(TripDescriptor tripDescriptor) {

        this.tripDescriptor = tripDescriptor;
    }

    public Long getRecordedTimeStamp() {

        return recordedTimeStamp;
    }

    public void setRecordedTimeStamp(Long recordedTimeStamp) {

        this.recordedTimeStamp = recordedTimeStamp;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        Date date = new Date(recordedTimeStamp * MILLISECOND_IN_SECOND);

        this.timeStampLocal = dateFormat.format(date);
    }

    public List<TripStop> getTripStops() {

        return tripStops;
    }

    public void setTripStops(List<TripStop> tripStops) {

        this.tripStops = tripStops;
    }

    // Return if this Trip has tripStops defined
    public boolean hasTripStops() {

        return tripStops != null && tripStops.size() > 0;
    }

    public String toString() {

        return "Trip Id : " + this.tripId + "\t" + "Recorded Time Stamp : "
        + this.recordedTimeStamp + "\t" + this.timeStampLocal + "\n"
        + toStringTripStops();
    }

    public String toStringTripStops() {

        StringBuilder output = new StringBuilder();
        Iterator<TripStop> iterator = this.tripStops.iterator();
        while (iterator.hasNext()) {
            TripStop stops = iterator.next();
            output.append("\n");
            output.append(stops.toString());
        }
        return output.toString();
    }

    public TripStop getCurrentStop() {

        return currentStop;
    }

    public void setCurrentStop(TripStop currentStop) {

        this.currentStop = currentStop;
    }

    public TripStop getLastStop() {

        return lastStop;
    }

    public void setLastStop(TripStop lastStop) {

        this.lastStop = lastStop;
    }

    public TripStop getNextStop() {

        return nextStop;
    }

    public void setNextStop(TripStop nextStop) {

        this.nextStop = nextStop;
    }

    public long getCurrentDelay() {

        return currentDelay;
    }

    public void setCurrentDelay(long currentDelay) {

        this.currentDelay = currentDelay;
    }

    // Update the distance from the vehicle to each stop
    public void calculateDistanceForStops(VehiclePosition vp) {

        Position position = vp.getPosition();
        for (TripStop tripStop : tripStops) {
            double distance = distance(Double.valueOf(position.getLatitude()),
            Double.valueOf(position.getLongitude()),
            Double.valueOf(tripStop.getStopLatitude()),
            Double.valueOf(tripStop.getStopLongt()), 'K');
            tripStop.setDistanceFromCurrent(distance);
        }
    }

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

    // Return the stop after the specified stop, or null if none
    public TripStop findNextStop() {

        int currentIndex = tripStops.indexOf(currentStop);
        int lastIndex = tripStops.indexOf(lastStop);
        if (currentIndex == 0 || (lastIndex > -1 && lastIndex == currentIndex - 1))
            return (currentIndex + 1 < tripStops.size()) ? tripStops.get(currentIndex + 1) : null;
        else
            return null;
    }

    public Trip getNextTrip() {

        return nextTrip;
    }

    public void setNextTrip(Trip nextTrip) {

        this.nextTrip = nextTrip;
    }

    // Return whether this service has commenced operation
    public boolean hasStarted() {

        // Check if service is scheduled to have commenced operation
        Date serviceDeparts = tripStops.get(0).getScheduledDepartureTime();
        Date now = new Date();
        if (serviceDeparts.getTime() < now.getTime())
            return false;

        // Return true if we are at or have passed a waypoint station
        return (lastStop != null || currentStop != null);
    }

    // Return whether this service has completed operation
    public boolean hasCompleted() {

        return (lastStop != null) && (nextStop == null);
    }

    // Return whether this trip has a valid delay prediction available
    @SuppressWarnings("unused")
    public boolean hasValidDelayPrediction() {

        // Undefined delay is stored as MAX_VALUE
        if (currentDelay == Double.MAX_VALUE)
            return false;

        // If delay is early on-time running, and we are forbidden from publishing that, return invalid
        if (currentDelay < 0 && !shouldPublishEarlyPredictions)
            return false;

        // Stop providing delay forecasting when delay is over an hour
        if (currentDelay <= -3600 || currentDelay >= 3600)
            return false;

        // Service must be a future prediction, or have reached a waypoint to provide a valid forecast
        return !this.hasStarted() || currentStop != null || nextStop != null;
    }

    public void calculateDelay() {

        // If VehiclePosition is undefined, continue to propagate last known delay
        if (vehiclePosition == null)
            return;

        // Plot a 250m radius around each platform to determine vehicle currently at station
        // (250m found through trial and error, an 8 car train is 163m long but track circuits and
        // stops don't always perfectly align eg look at a service stopped at Domestic Airport)
        final double maxProximity = 0.25;

        // Convert recorded timestamp to Date object
        Date date = new Date(recordedTimeStamp * MILLISECOND_IN_SECOND);

        // Recalculate vehicle positioning
        this.calculateDistanceForStops(vehiclePosition);
        TripStop nearestStop = this.findNearestStop();

        // Determine if vehicle is currently at the stop
        boolean currentlyAtStop = (nearestStop != null && nearestStop.getDistanceFromCurrent() <= maxProximity);

        // if service is at Central Station and has not just passed the previous
        // waypoint, then this is the CQ/Central trip changeover issue, and the last known
        // delay should remain propagated
        if (nearestStop.getStopName().contains("Central Station")) {
            boolean isFirstStop = (nearestStop == tripStops.get(0));
            boolean hasPassedPrevious = (nearestStop != lastStop) && (lastStop != null) && (lastStop.getActualDepartureTime() != null);
            if (!isFirstStop && !hasPassedPrevious)
                return;
        }

        if (!currentlyAtStop && currentStop != null) {
            // Vehicle has moved away from the current stop
            lastStop = currentStop;
            lastStop.setActualDepartureTime(lastStop.getActualTimeAtStop());
            currentStop = null;

            // Update delay based on departure time
            this.setDelayBasedOnStop(lastStop, false);
        }
        else if (currentlyAtStop) {
            if (currentStop == null) {
                // Vehicle has just arrived at the current stop
                currentStop = nearestStop;
                currentStop.setActualArrivalTime(date);
                nextStop = this.findNextStop();
            }
            else if (nearestStop != currentStop) {
                // Vehicle position has jumped between stations
                lastStop = currentStop;
                lastStop.setActualDepartureTime(lastStop.getActualTimeAtStop());
                currentStop = nearestStop;
                currentStop.setActualArrivalTime(date);
                nextStop = this.findNextStop();
            }

            // Update stop departed timestamp while the vehicle remains at the stop
            nearestStop.setActualTimeAtStop(new Date());

            // Update delay based on current time vs planned departure time
            this.setDelayBasedOnStop(currentStop, true);
        }

        // System.out.println(tripId+" current delay: "+String.valueOf(currentDelay) +
        // "\n -- last: "+lastStop+"\n -- now: "+currentStop+"\n -- next: "+nextStop);
    }

    // Update the delay for the vehicle based on it passing a stop
    private void setDelayBasedOnStop(TripStop fromStop, boolean vehicleIsAtStop)
    {

        // cast planned and actual arrival/departure timestamps to doubles in seconds
        long plannedArrival = (fromStop.getScheduledArrivalTime() != null) ? fromStop.getScheduledArrivalTime().getTime() / 1000 : 0;
        long plannedDeparture = (fromStop.getScheduledDepartureTime() != null) ? fromStop.getScheduledDepartureTime().getTime() / 1000 : 0;
        long actualArrival = (fromStop.getActualArrivalTime() != null) ? fromStop.getActualArrivalTime().getTime() / 1000 : 0;
        long actualDeparture = (fromStop.getActualDepartureTime() != null) ? fromStop.getActualDepartureTime().getTime() / 1000 : 0;

        // if service has not yet departed the stop, use the current at stop time
        // rather than waypoint passed time
        if (vehicleIsAtStop && fromStop.getActualTimeAtStop() != null)
            actualDeparture = fromStop.getActualTimeAtStop().getTime() / 1000;

        long arrivalDelay = (actualArrival > 0) ? actualArrival - plannedArrival : 0;
        long departureDelay = (actualDeparture > 0) ? actualDeparture - plannedDeparture : 0;

        // double plannedDwellTime = plannedDeparture - plannedArrival;

        // if service is currently at a stop and is not yet scheduled to have departed,
        // assume service will depart on time
        if (vehicleIsAtStop && actualDeparture < plannedDeparture) {
            arrivalDelay = Math.max(0, arrivalDelay);

            // if arrival delay is an improvement on the last known delay, then propagate that
            if (Math.abs(arrivalDelay) < Math.abs(currentDelay))
                currentDelay = arrivalDelay;
        }

        // if vehicle has just departed a stop, or is still at the stop + has not yet
        // departed + is delayed, then revise current delay for trip
        else if (!vehicleIsAtStop || actualDeparture >= plannedDeparture) {
            currentDelay = departureDelay;
        }

        // Update predicted arrival/departure times at each stop
        this.cascadeCurrentDelayToStops();
    }

    // Propagate the current delay to the TripStop entities
    public void cascadeCurrentDelayToStops() {

        long delay = currentDelay;
        for (TripStop stop : tripStops) {

            // If we have already passed this stop, don't update it
            if (lastStop != null && stop.getStopSequence() <= lastStop.getStopSequence())
                continue;
            if (currentStop != null && stop.getStopSequence() < currentStop.getStopSequence())
                continue;

            // If delay is less than 30 seconds, assume service will operate on time at this stop
            if (delay <= 30)
                delay = 0;

            // Determine how much time we could make up at this stop
            long excessDwellTime = (shouldNegateDwellTimes ? Math.max(0, stop.getAnticipatedDwellTime() - minimumDwellTime) : 0);

            // If we haven't yet passed this stop, predict arrival = schedule + rolling delay
            if (stop != currentStop) {
                stop.setPredictedArrivalTime(new Date(stop.getScheduledArrivalTime().getTime() + delay * MILLISECOND_IN_SECOND));
            }

            // Decrement delay by the amount of time we can make up
            delay = Math.max(0, delay - excessDwellTime);

            // Forecast departure time at this stop
            stop.setPredictedDepartureTime(new Date(stop.getScheduledDepartureTime().getTime() + delay * MILLISECOND_IN_SECOND));
        }

    }

    // Return the delay in seconds at the last stop of this trip
    public long delayAtLastStop() {

        if (tripStops != null)
            return tripStops.get(tripStops.size() - 1).getArrivalDelay();
        else
            return currentDelay;
    }

    // Propagate the delay from the specified previous trip onto this one
    public void cascadeDelayFromPreviousTrip(Trip previousTrip) {

        if (previousTrip.hasValidDelayPrediction() && shouldCascadeDelayToNextTrip) {
            long lastStopDelay = previousTrip.delayAtLastStop();
            currentDelay = Math.max(0, lastStopDelay - this.deltaTimeSinceTrip(previousTrip) + minimumDwellTime);
            recordedTimeStamp = previousTrip.getRecordedTimeStamp();

            this.cascadeCurrentDelayToStops();
        }

    }

    // Mark this service as cancelled by setting schedule relationship to skipped
    public void markAsCancelled() {

        final TripDescriptor.Builder trip = TripDescriptor.newBuilder();
        trip.setTripId(tripId);
        trip.setRouteId(routeId);
        trip.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
        tripDescriptor = trip.build();

        for (TripStop stop : tripStops) {
            stop.setScheduleRelationship(GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED);
        }
    }

    // Return the delta of seconds between the end of the specified trip and the start of this trip
    public long deltaTimeSinceTrip(Trip trip) {

        Date lastTripEnds = trip.scheduledEndTime();
        Date thisTripStarts = this.scheduledStartTime();
        if (lastTripEnds == null || thisTripStarts == null)
            return 0;
        return (thisTripStarts.getTime() - lastTripEnds.getTime()) / MILLISECOND_IN_SECOND;
    }

    // Time this trip is scheduled to commence operation
    public Date scheduledStartTime() {

        if (tripStops == null || tripStops.size() == 0)
            return null;
        TripStop firstStop = tripStops.get(0);
        return firstStop.getScheduledDepartureTime();
    }

    // Time this trip is scheduled to cease operating
    public Date scheduledEndTime() {

        if (tripStops == null || tripStops.size() == 0)
            return null;
        TripStop lastStop = tripStops.get(tripStops.size() - 1);
        return lastStop.getScheduledArrivalTime();
    }

    /*
     * Distance calculation helper methods
     */
    private double distance(double lat1, double lon1, double lat2, double lon2,
    char unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
        + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
        * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {

        return (rad * 180.0 / Math.PI);
    }

    public String getRouteId() {

        return routeId;
    }

    public void setRouteId(String routeId) {

        this.routeId = routeId;

        // If we know the tripId and routeId, set up a placeholder TripDescriptor instance
        if (routeId != null && tripId != null) {
            final TripDescriptor.Builder trip = TripDescriptor.newBuilder();
            trip.setTripId(tripId);
            trip.setRouteId(routeId);
            trip.setScheduleRelationship(TripDescriptor.ScheduleRelationship.SCHEDULED);
            tripDescriptor = trip.build();
        }
    }

    public String getServiceId() {

        return serviceId;
    }

    public void setServiceId(String serviceId) {

        this.serviceId = serviceId;
    }

    public String getHeadsign() {

        return headsign;
    }

    public void setHeadsign(String headsign) {

        this.headsign = headsign;
    }

    public int getDirectionId() {

        return directionId;
    }

    public void setDirectionId(int directionId) {

        this.directionId = directionId;
    }

    public String getBlockId() {

        return blockId;
    }

    public void setBlockId(String blockId) {

        this.blockId = blockId;
    }

    public String getShapeId() {

        return shapeId;
    }

    public void setShapeId(String shapeId) {

        this.shapeId = shapeId;
    }

    public VehiclePosition getVehiclePosition() {

        return vehiclePosition;
    }

    public void setVehiclePosition(VehiclePosition vehiclePosition) {

        this.vehiclePosition = vehiclePosition;
    }

    public int getMissedVehicleUpdates() {

        return missedVehicleUpdates;
    }

    public void setMissedVehicleUpdates(int missedVehicleUpdates) {

        this.missedVehicleUpdates = missedVehicleUpdates;
    }
}
