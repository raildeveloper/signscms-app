// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.model.Trip;
import au.gov.nsw.railcorp.gtfs.model.TripStop;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TripUpdateConverter.
 * @author paritosh
 */
public class TripUpdateConverter extends GeneralProtocolBufferConverter {

    private static final String GTFS_VERSION = "1.0";

    private static final long MILLISECOND_IN_SECOND = 1000L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ActiveTrips activeTrips;

    /**
     * Constructor.
     */
    public TripUpdateConverter() {

        super();
    }

    /**
     * generateTripUpdates.
     * @return success.
     */
    public final boolean generateTripUpdates() {

        boolean result = true;

        // setup FeedMessage & Feed Header GTFS objects?
        final FeedHeader.Builder gtfsHeader = FeedHeader.newBuilder();
        gtfsHeader.setGtfsRealtimeVersion(GTFS_VERSION);
        // If the Incrementaility ever changes, will need to identify a new way to get
        // unique GTFS-R FeedEntity ID's that will be unique to content of entity.
        gtfsHeader.setIncrementality(Incrementality.FULL_DATASET);
        gtfsHeader.setTimestamp(System.currentTimeMillis() / MILLISECOND_IN_SECOND);

        // build FeedMessage composite of FeedHeader + TripUpdate FeedEntity
        final FeedMessage.Builder gtfsMessage = FeedMessage.newBuilder();
        gtfsMessage.setHeader(gtfsHeader);

        // iterate over each active trip being tracked, construct the babushka doll that is a GTFSR payload
        if (activeTrips.getActiveTrips() != null) {
            for (Trip trip : activeTrips.getActiveTrips()) {

                // if the delay is >= 1 hour out or trip has completed, then invalidate the result
                if (!trip.hasValidDelayPrediction()) {
                    continue;
                }

                // find the stop we should start publishing delay information from
                TripStop nextStop = trip.getCurrentStop() != null ? trip.getCurrentStop() : trip.getNextStop();
                if (nextStop == null && trip.getTripStops() != null) {
                    nextStop = trip.getTripStops().get(0);
                }

                // construct TripUpdate payload
                final TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
                tripUpdate.setTrip(trip.getTripDescriptor());
                tripUpdate.setTimestamp(trip.getRecordedTimeStamp());

                // iterate over stops, publish stops where delay delta changes
                long lastArrivalDelta = Long.MAX_VALUE;
                long lastDepartureDelta = Long.MAX_VALUE;
                for (TripStop stop : trip.getTripStops()) {

                    // don't publish delays for stops already passed
                    if (stop.getStopSequence() < nextStop.getStopSequence()) {
                        continue;
                    }

                    // calculate delay deltas
                    long arrivalDelay = stop.getArrivalDelay();
                    long departureDelay = stop.getDepartureDelay();

                    // if service is within 30 seconds of on-time running, round delay value to 0 seconds
                    // to keep the feed compact
                    arrivalDelay = (arrivalDelay > -30 && arrivalDelay < 30) ? 0 : arrivalDelay;
                    departureDelay = (departureDelay > -30 && departureDelay < 30) ? 0 : departureDelay;

                    // if service has arrived at stop early, publish a useful delay value
                    // rather than the arrival time
                    if (arrivalDelay < 0)
                        arrivalDelay = Math.max(arrivalDelay, departureDelay);

                    // check if delay delta has changed
                    if (lastArrivalDelta == arrivalDelay && lastDepartureDelta == departureDelay)
                        continue;
                    lastArrivalDelta = arrivalDelay;
                    lastDepartureDelta = departureDelay;

                    // construct StopTimeEvent payloads
                    final StopTimeEvent.Builder arrivalEvent = StopTimeEvent.newBuilder();
                    final StopTimeEvent.Builder departureEvent = StopTimeEvent.newBuilder();
                    arrivalEvent.setDelay((int) arrivalDelay);
                    departureEvent.setDelay((int) departureDelay);

                    // construct StopTimeUpdate payload, add to TripUpdate payload
                    final StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
                    stopTimeUpdate.setStopId(stop.getStopId());
                    stopTimeUpdate.setScheduleRelationship(stop.getScheduleRelationship());
                    stopTimeUpdate.setArrival(arrivalEvent);
                    stopTimeUpdate.setDeparture(departureEvent);
                    tripUpdate.addStopTimeUpdate(stopTimeUpdate);
                }

                // construct FeedEntity wrapped around TripUpdate
                final FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
                feedEntity.setId(trip.getTripId());
                feedEntity.setTripUpdate(tripUpdate);

                // add FeedEntity to FeedMessage
                gtfsMessage.addEntity(feedEntity);
            }
        }

        // System.out.println("TripUpdateConverter.generateTripUpdates invoked" );

        // Create Protocol buffer from FeedHeader & replace stored Protocol Buffer
        final FeedMessage newFeed = gtfsMessage.build();

        final byte[] newProtoBuf = newFeed.toByteArray();
        setCurrentProtoBuf(newProtoBuf);

        return result;
    }

    public ActiveTrips getActiveTrips() {

        return activeTrips;
    }

    public void setActiveTrips(ActiveTrips activeTrips) {

        this.activeTrips = activeTrips;
    }

}
