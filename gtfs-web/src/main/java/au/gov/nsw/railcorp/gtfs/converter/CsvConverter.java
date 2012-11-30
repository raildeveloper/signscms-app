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

    /**
     * Obtains the most recent successfully converted ProtoBuf in binary format.
     * @return returns the current ProtoBuf.
     */
    byte[] getCurrentProtoBuf();

    /**
     * obtains a readable text representation of the most recent successfully
     * converted ProtoBuf.
     * @return The debug text for the current ProtoBuf
     */
    String getCurrentProtoBufDebug();
}
