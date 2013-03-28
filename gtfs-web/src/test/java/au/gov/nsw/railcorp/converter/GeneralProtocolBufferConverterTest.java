// RailCorp 2012
package au.gov.nsw.railcorp.converter;

import au.gov.nsw.railcorp.gtfs.converter.GeneralProtocolBufferConverter;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


/**
 * @author John
 *
 */
public class GeneralProtocolBufferConverterTest extends TestCase {

    GeneralProtocolBufferConverter converter;
    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        converter = new GeneralProtocolBufferConverter();
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        converter = null;
    }
    
    @Test
    public void testValidProtocolBuffer() {
        
        FeedMessage.Builder feed = FeedMessage.newBuilder();
        
        feed.setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("1.0").setTimestamp(101));
        
        InputStream in = new ByteArrayInputStream(feed.build().toByteArray());
        
        assertTrue(converter.storeProtocolBuffer(in));
    }
    
    @Test
    public void testInvalidProtocolBuffer() {
        InputStream in = new ByteArrayInputStream("Not a protocol buffer".getBytes());
        
        assertFalse(converter.storeProtocolBuffer(in));
    }
}
