// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.converter.types.TripBasedCsvRow;
import au.gov.nsw.railcorp.gtfs.converter.types.VehiclePositionCsvRow;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.CongestionLevel;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;


import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Converter for the CSV format defined for Vehicle Position data.
 * @author John
 */
public class VehiclePositionCsvConverter extends GeneralCsvConverter {

    /**
     * Constructor.
     */
    public VehiclePositionCsvConverter() {

        super();
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getCsvRowClass()
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
     *      com.google.transit.realtime.GtfsRealtime.FeedEntity.Builder)
     */
    @Override
    protected boolean processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity) {

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
     * @param row The current data to use
     * @param vehiclePosition A builder object that the result will be written into
     */
    private void writeTimestamp(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getTimestamp() != null) {
            vehiclePosition.setTimestamp(row.getTimestamp().longValue());
        }
    }

    /**
     * Writes out the stop Id for this CSV row data.
     * @param row The current data to use
     * @param vehiclePosition A builder object that the result will be written into
     */
    private void writeStopId(final TripBasedCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getStopId() != null) {
            vehiclePosition.setStopId(row.getStopId());
        }
    }

    /**
     * Writes out the current stop sequence state for this CSV row data.
     * @param row The current data to use
     * @param vehiclePosition A builder object that the result will be written into
     */
    private void writeCurrentStopSequence(final VehiclePositionCsvRow row, final VehiclePosition.Builder vehiclePosition) {

        if (row.getCurrentStopSequence() != null) {
            vehiclePosition.setCurrentStopSequence(row.getCurrentStopSequence().intValue());
        }
    }

    /**
     * Writes out the CongestionLevel state for this CSV row data.
     * @param row The current data to use
     * @param vehiclePosition A builder object that the result will be written into
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
     * @param row The current data to use
     * @param vehiclePosition The vehicle position to populate
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
     * @param row the current data to use
     * @param trip The trip descriptor to populate
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
     * @param row An object representing the CSV rows data to convert
     * @param vehiclePosition The VehiclePosition to populate
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
     * @param row An object representing the CSV rows data to convert
     * @param vehiclePosition The VehiclePosition to populate
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
     * @param row An object representing the CSV rows data to convert
      * @param vehiclePosition The VehiclePosition to populate
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
}
