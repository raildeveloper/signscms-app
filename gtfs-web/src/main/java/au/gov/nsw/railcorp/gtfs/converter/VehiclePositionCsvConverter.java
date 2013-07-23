// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.converter.types.TripBasedCsvRow;
import au.gov.nsw.railcorp.gtfs.converter.types.VehiclePositionCsvRow;
import au.gov.nsw.railcorp.gtfs.dao.TripDao;
import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.helper.H2DatabaseAccess;
import au.gov.nsw.railcorp.gtfs.model.Trip;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.CongestionLevel;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Converter for the CSV format defined for Vehicle Position data.
 * @author John
 */
public class VehiclePositionCsvConverter extends GeneralCsvConverter {

    /* Spring Injected Transit Bundle Bean */
    private ActiveTrips generator;

    private TripUpdateConverter protoStorage;

    /**
     * Constructor.
     */
    public VehiclePositionCsvConverter() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getCsvRowClass() {

        return VehiclePositionCsvRow.class;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getProcessors()
     */
    @Override
    protected CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
        /* trip_id */new Optional(),
        /* routeId */new Optional(),
        /* startTime */new Optional(),
        /* startDate */new Optional(),
        /* scheduleRelationship */new Optional(new ParseBigDecimal()),
        /* vehicleId */new Optional(new Unique()),
        /* vehicleLabel */new Optional(),
        /* licensePlate */new Optional(),
        /* latitude */new Optional(new ParseBigDecimal()),
        /* longitude */new Optional(new ParseBigDecimal()),
        /* bearing */new Optional(new ParseBigDecimal()),
        /* odometer */new Optional(new ParseBigDecimal()),
        /* speed */new Optional(new ParseBigDecimal()),
        /* currentStopSequence */new Optional(new ParseBigDecimal()),
        /* stopId */new Optional(),
        /* currentStatus */new Optional(new ParseBigDecimal()),
        /* timestamp */new Optional(new ParseBigDecimal()),
        /* congestionLevel" */new Optional(new ParseBigDecimal())
        };

        return processors;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getCsvHeaders()
     */
    @Override
    protected String[] getCsvHeaders() {

        final String[] header = new String[] {
        "tripId",
        "routeId",
        "startTime",
        "startDate",
        "scheduleRelationship",
        "vehicleId",
        "vehicleLabel",
        "licensePlate",
        "latitude",
        "longitude",
        "bearing",
        "odometer",
        "speed",
        "currentStopSequence",
        "stopId",
        "currentStatus",
        "timestamp",
        "congestionLevel"
        };

        return header;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#processCsvRowAndBuildGtfsrEntity (java.lang.Object,
     *      com.google.transit.realtime.GtfsRealtime.FeedEntity.Builder, Builder)
     */
    @Override
    protected boolean processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity, Builder gtfsMessage) {

        assert row.getClass() == VehiclePositionCsvRow.class;

        if (row.getClass() == VehiclePositionCsvRow.class) {
            final VehiclePositionCsvRow currRow = (VehiclePositionCsvRow) row;

            if (currRow.rowContentsExist()) {
                // Build the Vehicle Position GTFS-R object
                final VehiclePosition.Builder vehiclePosition = VehiclePosition.newBuilder();

                writeCongestionLevel(currRow, vehiclePosition);
                writeVehicleStopStatus(currRow, vehiclePosition);
                writeCurrentStopSequence(currRow, vehiclePosition);
                writeStopId(currRow, vehiclePosition);
                writeTimestamp(currRow, vehiclePosition);
                writePosition(currRow, vehiclePosition);
                writeVehicleDescriptor(currRow, vehiclePosition);
                writeTripDescriptor(currRow, vehiclePosition);

                gtfsEntity.setVehicle(vehiclePosition);
                return true;
            }
        }
        return false;
    }

    /**
     * Writes out the stop Id for this CSV row data.
     * @param row
     *            The current data to use
     * @param vehiclePosition
     *            A builder object that the result will be written into
     */
    private void writeTimestamp(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getTimestamp() != null) {
            vehiclePosition.setTimestamp(row.getTimestamp().longValue());
        }
    }

    /**
     * Writes out the stop Id for this CSV row data.
     * @param row
     *            The current data to use
     * @param vehiclePosition
     *            A builder object that the result will be written into
     */
    private void writeStopId(final TripBasedCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getStopId() != null) {
            vehiclePosition.setStopId(row.getStopId());
        }
    }

    /**
     * Writes out the current stop sequence state for this CSV row data.
     * @param row
     *            The current data to use
     * @param vehiclePosition
     *            A builder object that the result will be written into
     */
    private void writeCurrentStopSequence(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getCurrentStopSequence() != null) {
            vehiclePosition.setCurrentStopSequence(row.getCurrentStopSequence().intValue());
        }
    }

    /**
     * Writes out the CongestionLevel state for this CSV row data.
     * @param row
     *            The current data to use
     * @param vehiclePosition
     *            A builder object that the result will be written into
     */
    private void writeCongestionLevel(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getCongestionLevel() != null) {
            final CongestionLevel level = CongestionLevel.valueOf(row.getCongestionLevel().intValue());
            if (level != null) {
                vehiclePosition.setCongestionLevel(level);
            } else {
                getLog().error("Unknown CongestionLevel value {} encountered", row.getCongestionLevel().intValue());
            }
        }
    }

    /**
     * Writes out the VehicleStopStatus state for this CSV row data.
     * @param row
     *            The current data to use
     * @param vehiclePosition
     *            The vehicle position to populate
     */
    private void writeVehicleStopStatus(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getCurrentStatus() != null) {
            final VehicleStopStatus stopStatus = VehicleStopStatus.valueOf(row.getCurrentStatus().intValue());
            if (stopStatus != null) {
                vehiclePosition.setCurrentStatus(stopStatus);
            } else {
                getLog().error("Unknown VehicleStopStatus value {} encountered", row.getCurrentStatus().intValue());
            }
        }
    }

    /**
     * Writes the appropriate value of the schedule relationship enum to the trip descriptor builder.
     * @param row
     *            the current data to use
     * @param trip
     *            The trip descriptor to populate
     */
    private void writeScheduleRelationship(final VehiclePositionCsvRow row, final TripDescriptor.Builder trip) {

        if (row.getScheduleRelationship() != null) {
            final ScheduleRelationship sched = ScheduleRelationship.valueOf(row.getScheduleRelationship().intValue());
            if (sched != null) {
                trip.setScheduleRelationship(sched);
            } else {
                getLog().error("Unknown ScheduleRelationship value {} encountered",
                row.getScheduleRelationship().intValue());
            }
        }
    }

    /**
     * Converts the CSV contents to a GTFS-R Trip Descriptor object.
     * @param row
     *            An object representing the CSV rows data to convert
     * @param vehiclePosition
     *            The VehiclePosition to populate
     */
    private void writeTripDescriptor(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.tripDescriptorContentsExist()) {
            final TripDescriptor.Builder trip = TripDescriptor.newBuilder();

            if (row.getRouteId() != null) {
                trip.setRouteId(row.getRouteId());
            }
            writeScheduleRelationship(row, trip);
            if (row.getStartDate() != null) {
                trip.setStartDate(row.getStartDate());
            }
            if (row.getStartTime() != null) {
                trip.setStartTime(row.getStartTime());
            }
            if (row.getTripId() != null) {
                trip.setTripId(row.getTripId());
            }
            vehiclePosition.setTrip(trip);
        }
    }

    /**
     * Converts the CSV contents to a GTFS-R Vehicle Descriptor object.
     * @param row
     *            An object representing the CSV rows data to convert
     * @param vehiclePosition
     *            The VehiclePosition to populate
     */
    private void writeVehicleDescriptor(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.vehicleDescriptorContentsExist()) {
            final VehicleDescriptor.Builder vehicle = VehicleDescriptor.newBuilder();

            if (row.getVehicleId() != null) {
                vehicle.setId(row.getVehicleId());
            }
            if (row.getVehicleLabel() != null) {
                vehicle.setLabel(row.getVehicleLabel());
            }
            if (row.getLicensePlate() != null) {
                vehicle.setLicensePlate(row.getLicensePlate());
            }
            vehiclePosition.setVehicle(vehicle);
        }
    }

    /**
     * Converts the CSV contents to a GTFS-R Position object.
     * @param row
     *            An object representing the CSV rows data to convert
     * @param vehiclePosition
     *            The VehiclePosition to populate
     */
    private void writePosition(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.positionContentsExist()) {
            final Position.Builder pos = Position.newBuilder();

            // lat & long are required
            assert row.getLatitude() != null;
            assert row.getLongitude() != null;
            pos.setLatitude(row.getLatitude().floatValue());
            pos.setLongitude(row.getLongitude().floatValue());
            if (row.getBearing() != null) {
                pos.setBearing(row.getBearing().floatValue());
            }
            if (row.getOdometer() != null) {
                pos.setOdometer(row.getOdometer().doubleValue());
            }
            if (row.getSpeed() != null) {
                pos.setSpeed(row.getSpeed().floatValue());
            }
            vehiclePosition.setPosition(pos);
        }
    }

    private void predictTime(FeedMessage message) throws SQLException {

        // How many updates should a vehicle be absent from the vehicle position feed
        // for before it's invalidated?
        final int invalidateCacheItemSampleThreshold = 10;
        final List<FeedEntity> entity = message.getEntityList();
        getLog().info("TripUpdateGenerator.predictTime: got " + entity.size() + " vehicles");
        final Iterator<FeedEntity> entityIterator = entity.iterator();

        if (generator == null || generator.getActiveTripMap() == null) {
            return;
        }

        Map<String, Trip> tripMap = generator.getActiveTripMap();
        final List<Trip> trips = new ArrayList<Trip>();

        // Instantiate new hash map for trip ID -> trip associations if none exists
        if (tripMap == null) {
            tripMap = new HashMap<String, Trip>();
        }

        try {
            final TripDao tripDAO = H2DatabaseAccess.getTripDao();

            while (entityIterator.hasNext()) {
                final FeedEntity feedEntity = entityIterator.next();
                if (feedEntity.hasVehicle()) {

                    // Read position/descriptor from GTFSRVehiclePosition feed
                    final VehiclePosition vp = feedEntity.getVehicle();
                    final TripDescriptor tripDescriptor = vp.getTrip();
                    final Long recordedTime = vp.getTimestamp();

                    if (tripDescriptor.getRouteId() != null) {

                        // Attempt to recycle the Trip instance based on tripId,
                        // if unavailable then load from DB
                        Trip trip = tripMap.get(tripDescriptor.getTripId());
                        if (trip == null) {
                            trip = tripDAO.findTripWithFollowingTrip(tripDescriptor.getTripId());
                        }

                        // store timestamp and descriptor for use in GTFSRTripUpdate feed
                        trip.setRecordedTimeStamp(recordedTime);
                        trip.setTripDescriptor(tripDescriptor);

                        // store Trip instance in tracking sets if stops are defined
                        if (trip.hasTripStops()) {
                            trips.add(trip);
                            tripMap.put(trip.getTripId(), trip);

                            // update delay for service
                            trip.setVehiclePosition(vp);
                            trip.calculateDelay();
                            if (vp != null) {
                                trip.setMissedVehicleUpdates(0);
                            }

                            // copy delay value to next trip
                            final Trip nextTrip = trip.getNextTrip();
                            if (trip.hasValidDelayPrediction() && nextTrip != null) {
                                nextTrip.cascadeDelayFromPreviousTrip(trip);
                                trips.add(nextTrip);
                                getLog().info(
                                "TripUpdateGenerator: cascaded delay " + trip.getCurrentDelay() + " => " + nextTrip.getCurrentDelay()
                                + " to next trip " + nextTrip.getTripId());
                                tripMap.put(nextTrip.getTripId(), nextTrip);
                            }
                        }
                    }
                }
            }

            // Remove trips from lookup map that are no longer in the active feed
            final Map<String, Trip> tripMapCopy = new HashMap<String, Trip>(tripMap);
            for (Trip trip : tripMapCopy.values()) {
                if (trips.contains(trip)) {
                    continue;
                }
                if (trip.getMissedVehicleUpdates() > invalidateCacheItemSampleThreshold) {
                    getLog().info("TripUpdateGenerator: invalidated trip " + trip.getTripId());
                    tripMap.remove(trip.getTripId());
                } else {
                    trip.setMissedVehicleUpdates(trip.getMissedVehicleUpdates() + 1);
                }
            }

        } catch (SQLException e) {
            getLog().error(e.getMessage());
        }
        generator.setActiveTrips(trips);
        generator.setActiveTripMap(tripMap);
    }

    /*
     * (non-Javadoc)
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#processTripUpdates(com.google.transit.realtime.GtfsRealtime.FeedMessage)
     */
    @Override
    protected boolean processTripUpdates(FeedMessage feedMessage) {

        try {
            if (feedMessage != null) {
                predictTime(feedMessage);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            getLog().error(e.getMessage());
        }
        if (protoStorage == null) {
            return false;
        }
        protoStorage.generateTripUpdates();
        return true;
    }

    /**
     * getGenerator.
     * @return the generator
     */
    public ActiveTrips getGenerator() {

        return generator;
    }

    /**
     * setGenerator.
     * @param genrator
     *            the generator to set
     */
    public void setGenerator(ActiveTrips genrator) {

        this.generator = genrator;
    }

    /**
     * getProtoStorage.
     * @return the protoStorage
     */
    public TripUpdateConverter getProtoStorage() {

        return protoStorage;
    }

    /**
     * setProtoStorage.
     * @param protStorage
     *            the protoStorage to set
     */
    public void setProtoStorage(TripUpdateConverter protStorage) {

        this.protoStorage = protStorage;
    }
}
