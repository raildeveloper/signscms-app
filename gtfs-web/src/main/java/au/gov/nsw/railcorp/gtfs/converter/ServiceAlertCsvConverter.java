// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

/**
 * Converter for the CSV format defined for Service Alerts data.
 * @author John
 */
public class ServiceAlertCsvConverter implements CsvConverter {

    /**
     * Constructor.
     */
    public ServiceAlertCsvConverter() {

        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     * @see au.gov.nsw.railcorp.gtfs.converter.CSVConverter#convertAndStoreCsv(java.lang.String)
     */
    @Override
    public boolean convertAndStoreCsv(String csvContents) {

        // TODO Auto-generated method stub
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
