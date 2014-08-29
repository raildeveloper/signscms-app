// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.converter.types.ServiceAlertCsvRow;
import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.model.Trip;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.Alert.Builder;
import com.google.transit.realtime.GtfsRealtime.Alert.Cause;
import com.google.transit.realtime.GtfsRealtime.Alert.Effect;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Converter for the CSV format defined for Service Alerts data.
 * @author John
 */
public class ServiceAlertCsvConverter extends GeneralCsvConverter {

    private static final String ENGLISH_LANGUAGE = "en";

    // Set true to scan service alert feed for cancellations and propagate to trip updates
    private static final boolean ENABLE_TRIP_UPDATE_CANCELLATIONS = false;

    /* Spring Injected Transit Bundle Bean */
    private ActiveTrips generator;

    private TripUpdateConverter protoStorage;

    /**
     * Constructor.
     */
    public ServiceAlertCsvConverter() {

        super();
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getCsvRowClass()
     */
    @Override
    protected Class<?> getCsvRowClass() {

        return ServiceAlertCsvRow.class;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getProcessors()
     */
    @Override
    protected CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
        /* activeStart */new Optional(new ParseBigDecimal()),
        /* activeEnd */new Optional(new ParseBigDecimal()),
        /* agencyId */new Optional(),
        /* routeId */new Optional(),
        /* routeType */new Optional(new ParseBigDecimal()),
        /* tripId */new Optional(),
        /* stopId */new Optional(),
        /* cause */new Optional(new ParseBigDecimal()),
        /* effect */new Optional(new ParseBigDecimal()),
        /* url */new Optional(),
        /* headerText */new Optional(),
        /* description */new Optional()
        };

        return processors;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#getCsvHeaders()
     */
    @Override
    protected String[] getCsvHeaders() {

        final String[] headers = new String[] {
        "activeStart",
        "activeEnd",
        "agencyId",
        "routeId",
        "routeType",
        "tripId",
        "stopId",
        "cause",
        "effect",
        "url",
        "headerText",
        "description"
        };
        return headers;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#processCsvRowAndBuildGtfsrEntity (java.lang.Object,
     *      com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder, com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder)
     */
    @Override
    protected boolean processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity, FeedMessage.Builder gtfsMessage) {

        assert row.getClass() == ServiceAlertCsvRow.class;

        if (row.getClass() == ServiceAlertCsvRow.class) {
            final ServiceAlertCsvRow currRow = (ServiceAlertCsvRow) row;

            if (currRow.rowContentsExist()) {
                final Alert.Builder existingAlert = findDuplicateAlert(currRow, gtfsMessage);
                if (null == existingAlert) {
                    // Build the GTFS-R Alert object
                    final Alert.Builder alert = Alert.newBuilder();

                    writeActivePeriod(currRow, alert);
                    writeCause(currRow, alert);
                    writeDescription(currRow, alert);
                    writeEffect(currRow, alert);
                    writeHeaderText(currRow, alert);
                    writeUrl(currRow, alert);
                    writeEntitySelector(currRow, alert);

                    gtfsEntity.setAlert(alert);
                    return true;
                } else {
                    // This should return false so that the entity is not added, but just the altered alert entity exists.
                    writeEntitySelector(currRow, existingAlert);
                }
            }
        }
        return false;
    }

    /**
     * Finds any duplicate Alerts that may already exist in the Service Alert feed.
     * This could happen as the CSV input does not account for the 1 to many relationship
     * with the entity selector in the Alert.
     * @param currRow
     *            The current CSV row
     * @param gtfsEntity
     *            The main entity for alerts
     * @return A Builder object for an alert with the same data if one exists, null otherwise.
     */
    private Builder findDuplicateAlert(ServiceAlertCsvRow currRow, FeedMessage.Builder gtfsMessage) {

        int i = 0;
        for (FeedEntity e : gtfsMessage.getEntityList()) {
            if (e.hasAlert()) {
                final Alert a = e.getAlert();
                if ((
                (currRow.getEffect() == null && !a.hasEffect())
                ||
                (currRow.getEffect() != null && a.getEffect() == Effect.valueOf(currRow.getEffect().intValue()))
                )
                &&
                (
                (currRow.getCause() == null && !a.hasCause())
                ||
                (currRow.getCause() != null && a.getCause() == Cause.valueOf(currRow.getCause().intValue()))
                )
                && isEqualToTranslatedString(currRow.getUrl(), a.getUrl())
                && isEqualToTranslatedString(currRow.getHeaderText(), a.getHeaderText())
                && isEqualToTranslatedString(currRow.getDescription(), a.getDescriptionText()))
                {
                    return gtfsMessage.getEntityBuilder(i).getAlertBuilder();
                }
            }
            ++i;
        }
        return null;
    }

    /**
     * Determines whether a string is in the default translation of the GTFS Translated String.
     * @param s
     *            The java string to compare
     * @param ts
     *            The translated string to compare
     * @return true if string s is default translation for the GTFS translated string
     */
    private boolean isEqualToTranslatedString(String s, TranslatedString ts) {

        if ((s != null) == (ts != null)) {
            if (s == null || s.equals(ts.getTranslation(0).getText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes out the url for this CSV row data.
     * @param row
     *            The current data to use
     * @param alert
     *            A builder object that the result will be written into
     */
    private void writeUrl(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getUrl() != null) {
            alert.setUrl(convertTranslatedString(row.getUrl()));
        }
    }

    /**
     * Writes out the header text for this CSV row data.
     * @param row
     *            The current data to use
     * @param alert
     *            A builder object that the result will be written into
     */
    private void writeHeaderText(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getHeaderText() != null) {
            alert.setHeaderText(convertTranslatedString(row.getHeaderText()));
        }
    }

    /**
     * Writes out the description for this CSV row data.
     * @param row
     *            The current data to use
     * @param alert
     *            A builder object that the result will be written into
     */
    private void writeDescription(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getDescription() != null) {
            alert.setDescriptionText(convertTranslatedString(row.getDescription()));
        }
    }

    /**
     * Writes out the effect for this CSV row data.
     * @param row
     *            The current data to use
     * @param alert
     *            A builder object that the result will be written into
     */
    private void writeEffect(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getEffect() != null) {
            final Effect e = Effect.valueOf(row.getEffect().intValue());
            if (e != null) {
                alert.setEffect(e);
            } else {
                getLog().error("Unknown Effect value {} encountered", row.getEffect().intValue());
            }
        }
    }

    /**
     * Writes out the cause for this CSV row data.
     * @param row
     *            The current data to use
     * @param alert
     *            A builder object that the result will be written into
     */
    private void writeCause(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getCause() != null) {
            final Cause c = Cause.valueOf(row.getCause().intValue());
            if (c != null) {
                alert.setCause(c);
            } else {
                getLog().error("Unknown Cause value {} encountered", row.getCause().intValue());
            }
        }
    }

    /**
     * Converts the CSV contents to a GTFS-R EntitySelector object.
     * @param currRow
     *            An object representing the CSV row data to convert
     * @return The created GTFS-R Entity Selector object
     */
    private void writeEntitySelector(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.informedEntityContentExists()) {
            final EntitySelector.Builder sel = EntitySelector.newBuilder();
            if (row.getAgencyId() != null) {
                sel.setAgencyId(row.getAgencyId());
            }
            if (row.getRouteType() != null) {
                sel.setRouteType(row.getRouteType().intValue());
            }
            if (row.getStopId() != null) {
                sel.setStopId(row.getStopId());
            }
            if (row.getRouteId() != null) {
                sel.setRouteId(row.getRouteId());
            }
            if (row.getTripId() != null) {
                final TripDescriptor.Builder trip = TripDescriptor.newBuilder();
                trip.setTripId(row.getTripId());
                sel.setTrip(trip);
            }
            alert.addInformedEntity(sel);
        }
    }

    /**
     * Returns a GTFS-R TranslatedString object for the provided string as an English
     * translation.
     * @param description
     *            The string to convert
     * @return a GTFS-R TranslatedString object
     */
    private TranslatedString convertTranslatedString(String description) {

        if (description == null) {
            return null;
        }
        final Translation.Builder trans = Translation.newBuilder();
        trans.setText(description);
        trans.setLanguage(ENGLISH_LANGUAGE);

        final TranslatedString.Builder resString = TranslatedString.newBuilder();
        resString.addTranslation(trans);
        return resString.build();
    }

    /**
     * Converts the CSV Active Period contents to a GTFS-R TimeRange object.
     * @param currRow
     *            An object representing the CSV row data to convert
     * @param alert
     *            The Alert builder object to populate
     */
    private void writeActivePeriod(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.timeRangeContentExists()) {
            final TimeRange.Builder time = TimeRange.newBuilder();
            if (row.getActiveStart() != null) {
                time.setStart(row.getActiveStart().longValue());
            }
            if (row.getActiveEnd() != null) {
                time.setEnd(row.getActiveEnd().longValue());
            }
            alert.addActivePeriod(time);
        }
    }

    /*
     * (non-Javadoc)
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#processTripUpdates(com.google.transit.realtime.GtfsRealtime.FeedMessage)
     */
    // @Override
    // Trip Updates generation has been moved inside RTTA.
    /**
     * Process Trip Updates.
     * @param feedMessage FeedMessage
     * @return boolean
     */
    protected boolean processTripUpdates(FeedMessage feedMessage) {

        // Loop over service alerts looking for cancelled message
        // set to true to enable cancelled service matching
        if (ENABLE_TRIP_UPDATE_CANCELLATIONS) {
            final List<FeedEntity> entity = feedMessage.getEntityList();
            final Iterator<FeedEntity> entityIterator = entity.iterator();
            while (entityIterator.hasNext()) {
                final FeedEntity feedEntity = entityIterator.next();
                if (feedEntity.hasAlert()) {
                    final Alert alert = feedEntity.getAlert();
                    if (alert != null && alert.getHeaderText() != null && alert.getHeaderText().getTranslationList().size() > 0) {
                        final String headerText = alert.getHeaderText().getTranslationList().get(0).getText();
                        if ("Cancelled".equalsIgnoreCase(headerText) && alert.getInformedEntityCount() > 0) {
                            applyCancellationToEntities(alert.getInformedEntityList());
                        }
                    }
                }
            }
        }

        // Commented - Paritosh - Don't need to generate TripUpdates from Service Alerts anymore
        // if (protoStorage == null) {
        // return false;
        // }
        // protoStorage.generateTripUpdates();
        return true;
    }

    /**
     * Cancelation method.
     * @param informedEntities
     *            entities
     */
    // Got a cancellation service alert, apply that to the trips concerned
    protected void applyCancellationToEntities(List<EntitySelector> informedEntities) {

        final Map<String, Trip> tripMap = generator.getActiveTripMap();
        if (tripMap == null) {
            return;
        }

        for (EntitySelector entitySelector : informedEntities) {
            if (entitySelector.getTrip() != null) {

                // Try to match a trip in the activeTripMap against this descriptor (defer
                // responsibility for Trip data loading from H2 to VehiclePositionCsvConverter)
                final Trip trip = tripMap.get(entitySelector.getTrip().getTripId());
                if (trip != null) {
                    getLog().info("MARK TRIP AS CANCELLED " + trip.getTripId());
                    trip.markAsCancelled();
                }

            }
        }
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
