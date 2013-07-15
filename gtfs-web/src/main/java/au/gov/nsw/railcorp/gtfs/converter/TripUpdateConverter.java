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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates Trip Updates.
 * @author Paritosh.
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
     * Generates Trip Updates for all Active Trips.
     * @return boolean result.
     */
    public final boolean generateTripUpdates() {

        final boolean result = true;

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
        final Map<String, Trip> tripMap = activeTrips.getActiveTripMap();
        if (tripMap != null) {
            for (Trip trip : activeTrips.getActiveTripMap().values()) {

                // if the delay is >= 1 hour out or trip has completed, then invalidate the result
                if (!trip.hasValidDelayPrediction()) {
                    continue;
                }
                // construct StopTimeEvent payload
                final StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
                stopTimeEvent.setDelay((int) trip.getCurrentDelay());

                // construct StopTimeUpdate payload
                final StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
                stopTimeUpdate.setArrival(stopTimeEvent);
                stopTimeUpdate.setDeparture(stopTimeEvent);
                stopTimeUpdate.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.SCHEDULED);

                // if service is at a stop, mark that as the delay forecast point; otherwise next stop
                TripStop nextStop = trip.getCurrentStop() != null ? trip.getCurrentStop() : trip.getNextStop();
                if (nextStop == null && trip.getTripStops() != null) {
                    nextStop = trip.getTripStops().get(0);
                }
                stopTimeUpdate.setStopId(nextStop.getStopId());

                // construct TripUpdate payload
                final TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
                tripUpdate.setTrip(trip.getTripDescriptor());
                tripUpdate.setTimestamp(trip.getRecordedTimeStamp());
                tripUpdate.addStopTimeUpdate(stopTimeUpdate);

                // construct FeedEntity wrapped around TripUpdate
                final FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
                feedEntity.setId(trip.getTripId());
                feedEntity.setTripUpdate(tripUpdate);

                // add FeedEntity to FeedMessage
                gtfsMessage.addEntity(feedEntity);
            }
        }

        // Create Protocol buffer from FeedHeader & replace stored Protocol Buffer
        final FeedMessage newFeed = gtfsMessage.build();

        final byte[] newProtoBuf = newFeed.toByteArray();
        setCurrentProtoBuf(newProtoBuf);

        return result;
    }

    public ActiveTrips getActiveTrips() {

        return activeTrips;
    }

    public void setActiveTrips(ActiveTrips activeTrip) {

        this.activeTrips = activeTrip;
    }

}
