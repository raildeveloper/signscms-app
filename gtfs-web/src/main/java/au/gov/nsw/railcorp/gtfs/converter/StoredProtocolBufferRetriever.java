// RailCorp 2012
package au.gov.nsw.railcorp.gtfs.converter;


/**
 * Interface for a object which can provide a stored protocol buffer.
 */
public interface StoredProtocolBufferRetriever {

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
