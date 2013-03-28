// RailCorp 2012
package au.gov.nsw.railcorp.gtfs.converter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * General implementation of a Protocol Buffer Retriever.
 * A thread safe container class which can store & retrieve from different threads.
 */
public class GeneralStoredProtocolBufferRetriever implements StoredProtocolBufferRetriever {

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
     * Sets the current protocol buffer to the new value.
     * As this function exits within a thread-safe container class, after calling this function it should
     * not be assumed the return from getCurrentProtoBuf will be the same Protocol buffer.
     * @param p The new protocol buffer
     */
    protected void setCurrentProtoBuf(byte[] p) {
        // Assigning the shared member protobuf is atomic, so no further sync required, as
        // reading only performed via getCurrentProtoBuf(), which also performs an atomic read
        protobuf = p;
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
        if (buf != null) {
            FeedMessage mesg;
            try {
                mesg = FeedMessage.parseFrom(buf);
                log.info(mesg.toString());
                return mesg.toString();
            } catch (InvalidProtocolBufferException e) {
                log.error(e.toString());
            }
        }
        return "";
    }

}
