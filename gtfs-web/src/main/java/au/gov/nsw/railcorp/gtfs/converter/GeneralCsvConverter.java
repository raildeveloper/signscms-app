// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
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
public abstract class GeneralCsvConverter implements CsvConverter {

    private static final String GTFS_VERSION = "1.0";
    private static final long MILLISECOND_IN_SECOND = 1000L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The stored Protocol Buffer. This object reference requires synchronised access between reading
     * and writing.
     * Currently this is handled by never changing the value of the contents, but only assigning/reading
     * the reference. declared as volatile so propagation between threads is timely, but without
     * overhead of full synchronise.
     */
    private volatile byte[] protobuf;

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

            Object row;
            while ((row = beanReader.read(getCsvRowClass(), header, processors)) != null) {
                final FeedEntity.Builder entity = FeedEntity.newBuilder();
                entity.setId(String.valueOf(beanReader.getRowNumber()));
                processCsvRowAndBuildGtfsrEntity(row, entity);
                gtfsMessage.addEntity(entity);
            }

            // Create Protocol buffer from FeedHeader & replace stored Protocol Buffer
            final FeedMessage newFeed = gtfsMessage.build();
            // TODO - check if synchronisation required???
             final byte[] newProtoBuf = newFeed.toByteArray();
             protobuf = newProtoBuf;

        } catch (IOException e) {
            log.error(e.toString());
        } finally {
            if (beanReader != null) {
                try {
                    beanReader.close();
                } catch (IOException e) {
                    log.warn(e.toString());
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * This function exists within a thread-safe container class, with updates to
     * the protocol buffer potentially being made from other threads. As such,
     * each call to this function should be considered to potentially return a different
     * value.
     * @see au.gov.nsw.railcorp.gtfs.converter.CsvConverter#getCurrentProtoBuf()
     */
    @Override
    public final byte[] getCurrentProtoBuf() {

        return protobuf;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CsvConverter#getCurrentProtoBufDebug()
     */
    @Override
    public final String getCurrentProtoBufDebug() {

        final byte[] buf = getCurrentProtoBuf();
        FeedMessage mesg;
        try {
            mesg = FeedMessage.parseFrom(buf);
        } catch (InvalidProtocolBufferException e) {
            log.error(e.toString());
            return null;
        }
        return mesg.toString();
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
     */
    protected abstract void processCsvRowAndBuildGtfsrEntity(Object row, FeedEntity.Builder gtfsEntity);

}
