// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import au.gov.nsw.railcorp.gtfs.converter.types.ServiceAlertCsvRow;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.Alert.Cause;
import com.google.transit.realtime.GtfsRealtime.Alert.Effect;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ift.CellProcessor;


/**
 * Converter for the CSV format defined for Service Alerts data.
 * @author John
 */
public class ServiceAlertCsvConverter extends GeneralCsvConverter {

    private static final String ENGLISH_LANGUAGE = "en";

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
        /* activeStart */   new Optional(new ParseBigDecimal()),
        /* activeEnd */     new Optional(new ParseBigDecimal()),
        /* agencyId */      new Optional(),
        /* routeId */       new Optional(),
        /* routeType */     new Optional(new ParseBigDecimal()),
        /* tripId */        new Optional(),
        /* stopId */        new Optional(),
        /* cause */         new Optional(new ParseBigDecimal()),
        /* effect */        new Optional(new ParseBigDecimal()),
        /* url */           new Optional(),
        /* headerText */    new Optional(),
        /* description */   new Optional()
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
     * @see au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#processCsvRowAndBuildGtfsrEntity
     * (java.lang.Object, com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder)
     */
    @Override
    protected void processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity) {

        assert row.getClass() == ServiceAlertCsvRow.class;

        if (row.getClass() == ServiceAlertCsvRow.class) {
            final ServiceAlertCsvRow currRow = (ServiceAlertCsvRow) row;

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
        }

    }

    /**
     * Writes out the url for this CSV row data.
     * @param row The current data to use
     * @param alert A builder object that the result will be written into
     */
    private void writeUrl(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getUrl() != null) {
            alert.setUrl(convertTranslatedString(row.getUrl()));
        }
    }

    /**
     * Writes out the header text for this CSV row data.
     * @param row The current data to use
     * @param alert A builder object that the result will be written into
     */
    private void writeHeaderText(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getHeaderText() != null) {
            alert.setHeaderText(convertTranslatedString(row.getHeaderText()));
        }
    }

    /**
     * Writes out the description for this CSV row data.
     * @param row The current data to use
     * @param alert A builder object that the result will be written into
     */
    private void writeDescription(final ServiceAlertCsvRow row, final Alert.Builder alert) {

        if (row.getDescription() != null) {
            alert.setDescriptionText(convertTranslatedString(row.getDescription()));
        }
    }

    /**
     * Writes out the effect for this CSV row data.
     * @param row The current data to use
     * @param alert A builder object that the result will be written into
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
     * @param row The current data to use
     * @param alert A builder object that the result will be written into
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
     * @param currRow An object representing the CSV row data to convert
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
            if (row.getTripId() != null || row.getRouteId() != null) {
                final TripDescriptor.Builder trip = TripDescriptor.newBuilder();
                if (row.getTripId() != null) {
                    trip.setTripId(row.getTripId());
                }
                if (row.getRouteId() != null) {
                    sel.setRouteId(row.getRouteId());
                    trip.setRouteId(row.getRouteId());
                }
                sel.setTrip(trip);
            }
            alert.addInformedEntity(sel);
        }
    }

    /**
     * Returns a GTFS-R TranslatedString object for the provided string as an English
     * translation.
     * @param description The string to convert
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
     * @param currRow An object representing the CSV row data to convert
     * @param alert The Alert builder object to populate
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

}
