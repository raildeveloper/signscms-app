// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;

import java.io.Reader;

/**
 * Interface for all CSV Converters.
 * @author John
 */
public interface CsvConverter {

    /**
     * Parses the CSV data and stores the converted data.
     * @param csvContents
     *            The data contents in CSV format
     * @return returns true if the CSV was successfully parsed and converted
     */
    boolean convertAndStoreCsv(Reader csvContents);

}
