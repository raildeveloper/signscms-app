// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.dao.TripDao;
import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.helper.ChangedTrips;
import au.gov.nsw.railcorp.gtfs.helper.H2DatabaseAccess;
import au.gov.nsw.railcorp.gtfs.model.Trip;
import au.gov.nsw.railcorp.gtfs.model.Trip.TRIP_TYPES;
import au.gov.nsw.railcorp.gtfs.model.TripStop;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbActivity;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbStopStatus;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbTripSource;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripListMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripModelEntityMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripNodeMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

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

    private ChangedTrips changedTrips;

    private VehiclePositionCsvConverter protoStorage;

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

        final boolean result = true;
        final int positive30 = 30;
        final int negative30 = -30;

        // setup FeedMessage & Feed Header GTFS objects?
        final FeedHeader.Builder gtfsHeader = FeedHeader.newBuilder();
        gtfsHeader.setGtfsRealtimeVersion(GTFS_VERSION);
        // If the Incrementaility ever changes, will need to identify a new way
        // to get
        // unique GTFS-R FeedEntity ID's that will be unique to content of
        // entity.
        gtfsHeader.setIncrementality(Incrementality.FULL_DATASET);
        gtfsHeader.setTimestamp(System.currentTimeMillis()
        / MILLISECOND_IN_SECOND);

        // build FeedMessage composite of FeedHeader + TripUpdate FeedEntity
        final FeedMessage.Builder gtfsMessage = FeedMessage.newBuilder();
        gtfsMessage.setHeader(gtfsHeader);

        // iterate over each active trip being tracked, construct the babushka
        // doll that is a GTFSR payload

        // First Construct Messages for Changed Trips.
        if (changedTrips != null && changedTrips.getChangedTrips().size() > 0) {
            for (Trip changedTrip : changedTrips.getChangedTrips()) {
                final String changedTripId = changedTrip.getTripId();
                log.debug("Building GTFS R Trip Update for Changed Trips " + changedTripId);
                final TripUpdate.Builder changedTripUpdate = TripUpdate.newBuilder();

                final TripDescriptor.Builder changedTripBuilder = TripDescriptor.newBuilder();
                changedTripBuilder.setTripId(changedTrip.getTripId());
                changedTripBuilder.setRouteId(changedTrip.getRouteId());

                if (changedTrip.getTripType() == TRIP_TYPES.TRIP_CANCELLED) {
                    // Mark the trip as Cancelled
                    log.debug("Trip " + changedTripId + " has been cancelled.");
                    changedTripBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.CANCELED);
                } else {
                    if (changedTrip.getTripType() == TRIP_TYPES.TRIP_CHANGED) {
                        // Mark the trip as Replacement
                        log.debug("Trip " + changedTripId + " has been replaced.");
                        changedTripBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.REPLACEMENT);
                    } else if (changedTrip.getTripType() == TRIP_TYPES.TRIP_INSERTED) {
                        // Mark the trip as ADDED
                        log.debug("Trip " + changedTripId + " has been inserted.");
                        changedTripBuilder.setScheduleRelationship(TripDescriptor.ScheduleRelationship.ADDED);
                    }
                    // For changed and inserted trips go through all the stops
                    for (TripStop tripStop : changedTrip.getTripStops()) {
                        log.debug("Iterate through " + changedTripId + " all trip stops " + tripStop.getStopId());
                        final StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
                        final StopTimeEvent.Builder arrivalStopTimeEvent = StopTimeEvent.newBuilder();
                        final StopTimeEvent.Builder departureStopTimeEvent = StopTimeEvent.newBuilder();
                        long arrivalTime = 0L;
                        long departureTime = 0L;
                        arrivalTime = tripStop.getScheduledArrivalTime().getTime() / MILLISECOND_IN_SECOND;
                        departureTime = tripStop.getScheduledDepartureTime().getTime() / MILLISECOND_IN_SECOND;
                        log.debug("Trip " + changedTripId + " scheduled arrival time  " + arrivalTime + " scheduled departure time "
                        + departureTime + " for stop id " + tripStop.getStopId());
                        if (changedTrip.hasValidDelayPrediction()) {
                            log.debug("Trip " + changedTripId + " has prediction. ");
                            // Find the stop from where we should start publishing any delay information if it exists
                            TripStop nextStop = changedTrip.getCurrentStop() != null ? changedTrip.getCurrentStop() : changedTrip
                            .getNextStop();
                            if (nextStop == null && changedTrip.getTripStops() != null) {
                                nextStop = changedTrip.getTripStops().get(0);
                            }
                            log.debug("Trip " + changedTripId + " prediction to start from stop " + nextStop.getStopId());
                            if (tripStop.getStopSequence() >= nextStop.getStopSequence()) {
                                // Include delay for these stops
                                // calculate delay deltas
                                long arrivalDelay = tripStop.getArrivalDelay();
                                long departureDelay = tripStop.getDepartureDelay();
                                log.debug("Trip has " + changedTripId + " arrival delay " + arrivalDelay + " departure delay "
                                + departureDelay + " for stop id " + tripStop.getStopId());

                                arrivalDelay = (arrivalDelay > negative30 && arrivalDelay < positive30) ? 0
                                : arrivalDelay;
                                departureDelay = (departureDelay > negative30 && departureDelay < positive30) ? 0
                                : departureDelay;
                                // if service has arrived at stop early, publish a useful
                                // delay value
                                // rather than the arrival time
                                if (arrivalDelay < 0) {
                                    arrivalDelay = Math.max(arrivalDelay, departureDelay);
                                }
                                arrivalTime += arrivalDelay;
                                departureTime += departureDelay;
                                log.debug("Trip " + changedTripId + " now has arrival time " + arrivalTime + " & departure time "
                                + departureTime + " for stop id " + tripStop.getStopId());
                            }

                        }
                        arrivalStopTimeEvent.setTime(arrivalTime);
                        departureStopTimeEvent.setTime(departureTime);
                        stopTimeUpdate.setArrival(arrivalStopTimeEvent);
                        stopTimeUpdate.setDeparture(departureStopTimeEvent);
                        stopTimeUpdate.setStopId(tripStop.getStopId());
                        changedTripUpdate.addStopTimeUpdate(stopTimeUpdate);
                    }
                }
                changedTripUpdate.setTrip(changedTripBuilder);
                changedTripUpdate.setTimestamp(changedTrip.getRecordedTimeStamp());
                // construct FeedEntity wrapped around TripUpdate
                final FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
                feedEntity.setId(changedTrip.getTripId());
                feedEntity.setTripUpdate(changedTripUpdate);

                // add FeedEntity to FeedMessage
                gtfsMessage.addEntity(feedEntity);

            }
        }
        if (activeTrips.getActiveTrips() != null) {
            activeTripLoop: for (Trip trip : activeTrips.getActiveTrips()) {

                // if the delay is >= 1 hour out or trip has completed, then
                // invalidate the result
                if (!trip.hasValidDelayPrediction()) {
                    continue;
                }

                // Check if this trip is in changed list - If Yes - Ignore this trip as already included in changed trip gtfs message
                // construct above

                for (Trip changedTrip : changedTrips.getChangedTrips()) {
                    if (trip.getTripId().equalsIgnoreCase(changedTrip.getTripId())) {
                        continue activeTripLoop;
                    }
                }
                // find the stop we should start publishing delay information
                // from
                TripStop nextStop = trip.getCurrentStop() != null ? trip
                .getCurrentStop() : trip.getNextStop();
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

                    // if service is within 30 seconds of on-time running, round
                    // delay value to 0 seconds
                    // to keep the feed compact
                    arrivalDelay = (arrivalDelay > negative30 && arrivalDelay < positive30) ? 0
                    : arrivalDelay;
                    departureDelay = (departureDelay > negative30 && departureDelay < positive30) ? 0
                    : departureDelay;

                    // if service has arrived at stop early, publish a useful
                    // delay value
                    // rather than the arrival time
                    if (arrivalDelay < 0) {
                        arrivalDelay = Math.max(arrivalDelay, departureDelay);
                    }

                    // check if delay delta has changed
                    if (lastArrivalDelta == arrivalDelay
                    && lastDepartureDelta == departureDelay)
                    {
                        continue;
                    }
                    lastArrivalDelta = arrivalDelay;
                    lastDepartureDelta = departureDelay;

                    // construct StopTimeEvent payloads
                    final StopTimeEvent.Builder arrivalEvent = StopTimeEvent
                    .newBuilder();
                    final StopTimeEvent.Builder departureEvent = StopTimeEvent
                    .newBuilder();
                    arrivalEvent.setDelay((int) arrivalDelay);
                    departureEvent.setDelay((int) departureDelay);

                    // construct StopTimeUpdate payload, add to TripUpdate
                    // payload
                    final StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate
                    .newBuilder();
                    stopTimeUpdate.setStopId(stop.getStopId());
                    stopTimeUpdate.setScheduleRelationship(stop
                    .getScheduleRelationship());
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
        // Create Protocol buffer from FeedHeader & replace stored Protocol
        // Buffer
        final FeedMessage newFeed = gtfsMessage.build();

        final byte[] newProtoBuf = newFeed.toByteArray();
        setCurrentProtoBuf(newProtoBuf);

        return result;
    }

    public ActiveTrips getActiveTrips() {

        return activeTrips;
    }

    public void setActiveTrips(ActiveTrips activetrips) {

        this.activeTrips = activetrips;
    }

    public ChangedTrips getChangedTrips() {

        return changedTrips;
    }

    public void setChangedTrips(ChangedTrips changedtrips) {

        this.changedTrips = changedtrips;
    }

    public VehiclePositionCsvConverter getProtoStorage() {

        return protoStorage;
    }

    public void setProtoStorage(VehiclePositionCsvConverter protostorage) {

        this.protoStorage = protostorage;
    }

    /**
     * Processes Protocol Buffer message received from TripPublisher - Builds
     * objects for Change Trips.
     * @param feedMessage
     *            - TripModelEntityMessage
     * @return - boolean - true or false - status if processing Protocol Buffer
     *         message was successful or not.
     */
    @Override
    public boolean processLoadTripUpdates(TripModelEntityMessage feedMessage) {

        boolean returnMessage = true;
        boolean addStops = false;
        final TripDao tripDAO = H2DatabaseAccess.getTripDao();
        try {
            if (feedMessage.hasActiveTrips()) {
                final TripListMessage tripListMessage = feedMessage.getActiveTrips();

                final List<Trip> changedTripsList = new ArrayList<Trip>();

                tripMessageLoop: for (TripMessage tripMessage : tripListMessage.getTripMsgsList()) {
                    final String tripId = tripMessage.getTripId();
                    final PbActivity activity = tripMessage.getCurrentActivity();
                    final Trip trip = new Trip();
                    trip.setTripId(tripId);
                    trip.setRouteId(tripMessage.getRouteId());
                    trip.setServiceId(tripMessage.getServiceId());
                    trip.setBlockId(String.valueOf(tripMessage.getBlockId()));
                    trip.setRecordedTimeStamp(tripListMessage.getMsgTimestamp());
                    log.debug("Recived Trip Update for " + tripId + " complete message " + tripMessage.toString());
                    if (activity == PbActivity.AC_CANCEL) {
                        log.debug("Received Message that Trip " + tripId
                        + " has been CANCELLED");

                        trip.setTripType(TRIP_TYPES.TRIP_CANCELLED);
                        changedTripsList.add(trip);
                    } else {
                        final PbTripSource tripSource = tripMessage.getTripSource();
                        if (tripSource == PbTripSource.TC_INSERTED) {
                            // This is inserted trip
                            log.debug("Received Message that trip " + tripId + " has been INSERTED");
                            trip.setTripType(TRIP_TYPES.TRIP_INSERTED);
                            addStops = true;
                        } else if (tripSource == PbTripSource.TC_TIMETABLE) {
                            log.debug("Received Message that trip " + tripId + " has been CHANGED");
                            trip.setTripType(TRIP_TYPES.TRIP_CHANGED);
                            addStops = true;
                        } else {
                            addStops = false;
                            log.debug("Don't know what this trip is : " + tripId + " Nothing in Activity or Trip Source");
                        }

                        final List<TripStop> tripStops = new ArrayList<TripStop>();
                        if (addStops) {
                            if (!(tripMessage.getTripNodeMsgsList().size() > 0)) {
                                // Let trip publisher know of the error - but continue to process other trips in the message
                                returnMessage = false;
                                log.debug("Trip " + tripId + " doesn't have any nodes - omitting this from trip updates ");
                                continue tripMessageLoop;
                            }
                            for (TripNodeMessage tripNodeMessage : tripMessage.getTripNodeMsgsList()) {
                                log.debug("Trip " + tripId + " has following nodes --> " + tripNodeMessage.toString());
                                final PbStopStatus stopStatus = tripNodeMessage.getStopStatus();
                                if (stopStatus != PbStopStatus.SS_NONE) {
                                    final TripStop tripStop = new TripStop();
                                    tripStop.setStopId(tripNodeMessage.getStopId());

                                    final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
                                    final Date arrivalDate = new Date(tripNodeMessage.getArrivalTime() * MILLISECOND_IN_SECOND);
                                    final Date departureDate = new Date(tripNodeMessage.getDepartureTime() * MILLISECOND_IN_SECOND);
                                    tripStop.setArrivalTime(dateFormat.format(arrivalDate));
                                    tripStop.setDepartureTime(dateFormat.format(departureDate));

                                    tripStop.setStopSequence(tripNodeMessage.getStopSequence());

                                    // Fetching GTFS Stop Details from H2 database - this needs to be changed to be sent from Trip publisher
                                    // tripStop.setStopName(tripDAO.getStopNameForStopId(tripNodeMessage.getStopId()));
                                    tripDAO.getGtfsStopsDetails(tripStop, tripNodeMessage.getStopId());
                                    log.debug("Trip " + tripId + " has been following stopping pattern" + tripStop.toString());
                                    tripStops.add(tripStop);
                                }
                            }

                            trip.setTripStops(tripStops);
                            checkVehiclePosition(trip);
                            changedTripsList.add(trip);
                        }
                    }

                }
                changedTrips.setChangedTrips(changedTripsList);
            } else {
                log.debug("Proto Buff received from Trip Publisher is empty");
                returnMessage = false;
            }
        } catch (NullPointerException e) {
            log.debug(e.getMessage());
            returnMessage = false;
        } catch (SQLException s) {
            log.debug(s.getMessage());
            returnMessage = false;
        }
        return returnMessage;

    }

    /**
     * Check if vehiclePosition exits for this trip.
     * @param trip
     */
    private void checkVehiclePosition(Trip trip) {

        FeedMessage gtfsrprotobuf;
        try {
            gtfsrprotobuf = FeedMessage.parseFrom(protoStorage.getCurrentProtoBuf());
            final List<FeedEntity> entity = gtfsrprotobuf.getEntityList();
            final Iterator<FeedEntity> entityIterator = entity.iterator();
            while (entityIterator.hasNext()) {
                final FeedEntity feedEntity = entityIterator.next();
                if (feedEntity.hasVehicle()) {
                    final VehiclePosition vehiclePosition = feedEntity.getVehicle();
                    final TripDescriptor tripDescriptor = vehiclePosition.getTrip();
                    final String vpTripId = tripDescriptor.getTripId();
                    if (trip.getTripId().equalsIgnoreCase(vpTripId)) {
                        log.debug("Found Vehicle Position for " + trip.getTripId() + " currently at " + vehiclePosition.toString());
                        trip.setVehiclePosition(vehiclePosition);
                        log.debug("Now calculating delays for " + trip.getTripId());
                        trip.calculateDelay();
                        trip.setMissedVehicleUpdates(0);
                        log
                        .debug("Trip has vehicle position so should be some prediction - setting timestamp to reflect trip update --> "
                        + vehiclePosition.getTimestamp());
                        trip.setRecordedTimeStamp(vehiclePosition.getTimestamp());
                        break;
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            log.debug(e.getMessage());
        }

    }
}
