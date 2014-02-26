// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter;





import au.gov.nsw.transport.rtta.intf.trippublish.pb.generated.Trippublish.TripPublishEntityMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a general protocol buffer converter that can receive a protocol buffer via
 * a reader object, verifies it and makes it available for retrieval.
 */
public abstract class GeneralProtocolBufferConverter extends GeneralStoredProtocolBufferRetriever {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Reads the Protocol Buffer data, validates it and stores data.
     * @param protoStream
     *            A stream containing the data contents in Protocol Buffer format
     * @return returns true if the CSV was successfully parsed and converted
     */
    public boolean storeProtocolBuffer(InputStream protoStream) {

        try {
            final FeedMessage feed = FeedMessage.parseFrom(protoStream);
            setCurrentProtoBuf(feed.toByteArray());
            return true;
        } catch (InvalidProtocolBufferException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        return false;
    }

    /**
     * Does processing required for generating tripUpdates.
     * @param feedMessage
     *            feed
     * @return success
     */
    public abstract String processLoadTripUpdates(TripPublishEntityMessage feedMessage);
}
