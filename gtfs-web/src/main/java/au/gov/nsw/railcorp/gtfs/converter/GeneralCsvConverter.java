// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;


/**
 * Implements a general abstract CSV Converter to perform all the common parts
 * of reading the input CSV, parsing it and loading the results into the GTFSR
 * protocol buffer objects.
 * This converter is a thread-safe object, with expected access to read & update the
 * protocol buffer from different threads.
 * @author John
 */
public abstract class GeneralCsvConverter extends GeneralStoredProtocolBufferRetriever implements CsvConverter {

    private static final String GTFS_VERSION = "1.0";
    private static final long MILLISECOND_IN_SECOND = 1000L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor.
     */
    public GeneralCsvConverter() {

    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CsvConverter#convertAndStoreCsv(java.io.Reader)
     */
    @Override
    public final boolean convertAndStoreCsv(Reader csvContents) {

        boolean res = true;

        // setup FeedMessage & Feed Header GTFS objects?
        final FeedHeader.Builder gtfsHeader = FeedHeader.newBuilder();
        gtfsHeader.setGtfsRealtimeVersion(GTFS_VERSION);
        // If the Incrementaility ever changes, will need to identify a new way to get
        // unique GTFS-R FeedEntity ID's that will be unique to content of entity.
        gtfsHeader.setIncrementality(Incrementality.FULL_DATASET);
        gtfsHeader.setTimestamp(System.currentTimeMillis() / MILLISECOND_IN_SECOND);
        final FeedMessage.Builder gtfsMessage = FeedMessage.newBuilder();
        gtfsMessage.setHeader(gtfsHeader);

        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(csvContents, CsvPreference.STANDARD_PREFERENCE);

            final String[] header = getCsvHeaders();
            final CellProcessor[] processors = getProcessors();

            log.info("CSV File contents for {}:", this.getClass().toString());
            Object row;
            while ((row = beanReader.read(getCsvRowClass(), header, processors)) != null) {
                log.info(beanReader.getUntokenizedRow());
                final FeedEntity.Builder entity = FeedEntity.newBuilder();
                entity.setId(String.valueOf(beanReader.getRowNumber()));
                if (processCsvRowAndBuildGtfsrEntity(row, entity, gtfsMessage)) {
                    gtfsMessage.addEntity(entity);
                }
            }
            log.info("Read CSV file of {} Lines", beanReader.getRowNumber());

            // Create Protocol buffer from FeedHeader & replace stored Protocol Buffer
            final FeedMessage newFeed = gtfsMessage.build();

            final byte[] newProtoBuf = newFeed.toByteArray();
            setCurrentProtoBuf(newProtoBuf);

        } catch (IOException e) {
            log.error("Failed to Process CSV - " + e.getMessage());
            res = false;
        } catch (SuperCsvConstraintViolationException e) {
            log.error("Failed to Process CSV - " + e.getMessage());
            if (e.getCsvContext() != null) {
                log.error(e.getCsvContext().toString());
            }
            res = false;
        } catch (SuperCsvCellProcessorException e) {
            log.error("Failed to Process CSV - " + e.getMessage());
            if (e.getCsvContext() != null) {
                log.error(e.getCsvContext().toString());
            }
            res = false;
        } finally {
            if (beanReader != null) {
                try {
                    beanReader.close();
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return res;
    }

    /**
     * getter for log.
     * @return the log
     */
    protected final Logger getLog() {
        return log;
    }

    /**
     * Obtains the class type that should be used to store the CSV row contents.
     * @return the CSV row storage class
     */
    protected abstract Class<?> getCsvRowClass();

    /**
     * Obtains all the Super CSV processors required for the CSV format.
     * The processors determine the enforced typing for columns in the CSV.
     * @return array of CellProcessors setup for the VehiclePosition CSV format
     */
    protected abstract CellProcessor[] getProcessors();

    /**
     * Obtains the list of header names for the CSV columns. This list
     * must match the member names of the CSV row storage class precisely
     * for mapping purposes
     * @return The list of CSV header fields.
     */
    protected abstract String[] getCsvHeaders();

    /**
     * Does all processing required to process the CSV row and build the
     * required GTFS-R classes. The GTFS-R objects will be added to the provided
     * GTFS-R Message builder.
     * @param row
     *            The object representing the row
     * @param gtfsEntity
     *          A GTFS builder object that will be modified to add the data for this row
     * @param gtfsMessage
     *          A GTFS builder object that is used to possibly add to existing content rather than
     *          create a new Entity if some form of aggregation is appropriate.
     * @return true if an entity was successfully populated
     */
    protected abstract boolean processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity, Builder gtfsMessage);

}
