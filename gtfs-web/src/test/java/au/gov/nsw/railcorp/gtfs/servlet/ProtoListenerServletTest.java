// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.gov.nsw.railcorp.gtfs.converter.GeneralProtocolBufferConverter;
import au.gov.nsw.railcorp.gtfs.converter.TripUpdateConverter;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paritosh
 */

public class ProtoListenerServletTest extends TestCase {

    private static final Logger log = LoggerFactory
    .getLogger(CSVListenerServletTest.class);

    @InjectMocks
    ProtoListenerServlet protoServlet;

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        protoServlet = new ProtoListenerServlet();
        final GeneralProtocolBufferConverter con = mock(GeneralProtocolBufferConverter.class);
        protoServlet.setConverter(con);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        protoServlet = null;
    }

    @Test
    public void testHandleRequestTripUpdateMessage() throws IOException,
    ServletException {

        log.info("testHandleRequestTripUpdateMessage");

        // Set up the ProtoBuff Input Stream

        // TripListMessage.Builder builder = TripListMessage.newBuilder();
        // builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        // // TripListMessage tripListMessage = builder.build();
        //
        // final TripPublishEntityMessage.Builder tripModelEntityMessage = TripPublishEntityMessage.newBuilder();
        //
        // tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        // tripModelEntityMessage.setActiveTrips(builder);
        // TripPublishEntityMessage message = tripModelEntityMessage.build();

        // TripList Message is no longer receieved - RTTA 2B - App Shall now receive GTFSR TripUpdate Proto Buff

        FeedHeader.Builder gtfsHeader = FeedHeader.newBuilder();
        gtfsHeader.setGtfsRealtimeVersion("1.0");
        gtfsHeader.setIncrementality(FeedHeader.Incrementality.FULL_DATASET);
        gtfsHeader.setTimestamp(System.currentTimeMillis() / 1000);

        FeedMessage.Builder gtfsMessage = FeedMessage.newBuilder();
        gtfsMessage.setHeader(gtfsHeader);

        final FeedMessage message = gtfsMessage.build();

        final byte[] messageByte = message.toByteArray();
        System.out.println("message " + message.toString());
        final InputStream byteIn = new ByteArrayInputStream(messageByte);

        final TripUpdateConverter converter = mock(TripUpdateConverter.class);
        protoServlet.setConverter(converter);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter writer = mock(PrintWriter.class);
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {

            @Override
            public int read() throws IOException {

                return byteIn.read();
            }

        });
        when(response.getWriter()).thenReturn(writer);
        when(sContext.getServletContextName()).thenReturn("testHandleRequestTripUpdateMessage");

        // when(TripModelEntityMessage.parseFrom(inputStream)).thenReturn(message);
        when(protoServlet.getConverter().processLoadTripUpdates(message)).thenReturn("true");

        protoServlet.handleRequest(request, response);
        verify(protoServlet.getConverter()).processLoadTripUpdates(message);
        verify(writer).append("Successful Update");
    }

    @Test
    public void testHandleEmptyMessage() throws IOException, ServletException {

        log.info("testHandleEmptyMessage");

        final TripUpdateConverter converter = mock(TripUpdateConverter.class);
        protoServlet.setConverter(converter);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter writer = mock(PrintWriter.class);
        // TripListMessage.Builder builder = TripListMessage.newBuilder();
        // builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        // final TripPublishEntityMessage.Builder tripModelEntityMessage = TripPublishEntityMessage.newBuilder();
        //
        // tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        // tripModelEntityMessage.setActiveTrips(builder);
        // TripPublishEntityMessage message = tripModelEntityMessage.build();
        // Phase2B - GTFSR Trip Update ProtoBuff is being published

        FeedHeader.Builder gtfsHeader = FeedHeader.newBuilder();
        gtfsHeader.setGtfsRealtimeVersion("1.0");
        gtfsHeader.setIncrementality(FeedHeader.Incrementality.FULL_DATASET);
        gtfsHeader.setTimestamp(System.currentTimeMillis() / 1000);

        FeedMessage.Builder gtfsMessage = FeedMessage.newBuilder();
        gtfsMessage.setHeader(gtfsHeader);

        final FeedMessage message = gtfsMessage.build();

        final byte[] messageByte = message.toByteArray();
        System.out.println("message " + message.toString());
        final InputStream byteIn = new ByteArrayInputStream(messageByte);

        when(request.getServletContext()).thenReturn(sContext);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {

            @Override
            public int read() throws IOException {

                return byteIn.read();
            }

        });
        when(response.getWriter()).thenReturn(writer);
        when(sContext.getServletContextName()).thenReturn("testHandleEmptyMessage");

        when(protoServlet.getConverter().processLoadTripUpdates(message)).thenReturn("false");
        protoServlet.handleRequest(request, response);
        verify(protoServlet.getConverter()).processLoadTripUpdates(message);
        verify(writer).append("Failed Protocol Buffer validation processing");

    }

}
