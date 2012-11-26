// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

/**
 * Converter for the CSV format defined for Vehicle Position data.
 * @author John
 */
public class VehiclePositionCsvConverter implements CsvConverter {

    /**
     * Constructor.
     */
    public VehiclePositionCsvConverter() {

        super();
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CSVConverter#convertAndStoreCsv(java.lang.String)
     */
    @Override
    public boolean convertAndStoreCsv(String csvContents) {

        // TODO Auto-generated method stub
        final int i = 0;
        return false;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CSVConverter#getCurrentProtoBuf()
     */
    @Override
    public byte[] getCurrentProtoBuf() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CSVConverter#getCurrentProtoBufDebug()
     */
    @Override
    public String getCurrentProtoBufDebug() {

        // TODO Auto-generated method stub
        return null;
    }
}
