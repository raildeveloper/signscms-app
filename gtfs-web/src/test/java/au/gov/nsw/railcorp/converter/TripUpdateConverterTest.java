// RailCorp 2013

package au.gov.nsw.railcorp.converter;

import static org.mockito.Mockito.mock;

import au.gov.nsw.railcorp.gtfs.converter.TripUpdateConverter;
import au.gov.nsw.railcorp.gtfs.converter.VehiclePositionCsvConverter;
import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.helper.ChangedTrips;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbActivity;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbStopStatus;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.PbTripSource;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripListMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripModelEntityMessage;
import au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.Tripmodel.TripNodeMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Test Class for TripUpdateConverter
 * @author paritosh
 */
public class TripUpdateConverterTest extends TestCase {

    @InjectMocks
    TripUpdateConverter converter;

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        converter = new TripUpdateConverter();
        VehiclePositionCsvConverter protostorage = new VehiclePositionCsvConverter();
        protostorage.setGenerator(mock(ActiveTrips.class));
        ChangedTrips changedTrips = new ChangedTrips();
        ActiveTrips activeTrips = mock(ActiveTrips.class);
        converter.setProtoStorage(protostorage);
        converter.setChangedTrips(changedTrips);
        converter.setActiveTrips(activeTrips);

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

    /**
     * Test method for
     * {@link au.gov.nsw.railcorp.gtfs.converter.TripUpdateConverter#processLoadTripUpdates(au.gov.nsw.transport.rtta.intf.tripmodel.pb.generated.TripModelEntityMessage)}
     * .
     */
    @Test
    public void testTripCancellationMessage() {

        TripMessage.Builder tripMessage = TripMessage.newBuilder();
        tripMessage.setTripId("101A");
        tripMessage.setRouteId("IWL_2c");
        tripMessage.setServiceId("999");
        tripMessage.setBlockId(119);
        tripMessage.setBundleId(99);
        tripMessage.setTripInstance(1);
        tripMessage.setCurrentActivity(PbActivity.AC_CANCEL);
        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        builder.addTripMsgs(tripMessage);
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(1, mesg.getEntityCount());

        FeedEntity e = mesg.getEntity(0);
        assertTrue(e.hasId());
        assertFalse(e.hasIsDeleted());
        assertTrue(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertFalse(e.hasVehicle());

        TripUpdate tripUpdate = e.getTripUpdate();
        assertTrue(tripUpdate.hasTimestamp());
        assertTrue(tripUpdate.hasTrip());

        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        assertTrue(tripDescriptor.hasTripId());
        assertTrue(tripDescriptor.hasRouteId());
        assertTrue(tripDescriptor.hasScheduleRelationship());
        assertEquals("101A", tripDescriptor.getTripId());
        assertEquals("IWL_2c", tripDescriptor.getRouteId());
        assertEquals(ScheduleRelationship.CANCELED, tripDescriptor.getScheduleRelationship());
    }

    @Test
    public void testTimetableTripUpdateMessage() {

        TripMessage.Builder tripMessage = TripMessage.newBuilder();
        tripMessage.setTripId("101A");
        tripMessage.setRouteId("IWL_2c");
        tripMessage.setServiceId("999");
        tripMessage.setBlockId(119);
        tripMessage.setBundleId(99);
        tripMessage.setTripInstance(1);
        tripMessage.setTripSource(PbTripSource.TC_TIMETABLE);

        // Add Stops to Message

        for (int i = 1; i <= 5; i++) {
            TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
            tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
            tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
            tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
            tripNodeMessage.setStopSequence(i);
            tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
            tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
            tripMessage.addTripNodeMsgs(tripNodeMessage);
        }

        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        builder.addTripMsgs(tripMessage);
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(1, mesg.getEntityCount());

        FeedEntity e = mesg.getEntity(0);
        assertTrue(e.hasId());
        assertEquals("101A", e.getId());
        assertFalse(e.hasIsDeleted());
        assertTrue(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertFalse(e.hasVehicle());

        TripUpdate tripUpdate = e.getTripUpdate();
        assertTrue(tripUpdate.hasTimestamp());
        assertTrue(tripUpdate.hasTrip());

        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        assertTrue(tripDescriptor.hasTripId());
        assertTrue(tripDescriptor.hasRouteId());
        assertTrue(tripDescriptor.hasScheduleRelationship());
        assertEquals("101A", tripDescriptor.getTripId());
        assertEquals("IWL_2c", tripDescriptor.getRouteId());
        assertEquals(ScheduleRelationship.REPLACEMENT, tripDescriptor.getScheduleRelationship());

    }

    @Test
    public void testMultipleTimetableTripUpdateMessage() {

        // Number of Trip Insert Messages
        int numberTripInserts = 10;
        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);

        for (int k = 1; k <= numberTripInserts; k++) {
            TripMessage.Builder tripMessage = TripMessage.newBuilder();
            tripMessage.setTripId("A--" + String.valueOf(k));
            tripMessage.setRouteId("IWL_2c");
            tripMessage.setServiceId("999");
            tripMessage.setBlockId(119);
            tripMessage.setBundleId(99);
            tripMessage.setTripInstance(1);
            tripMessage.setTripSource(PbTripSource.TC_TIMETABLE);

            // Add Stops to Message

            for (int i = 1; i <= 5; i++) {
                TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
                tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
                tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
                tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
                tripNodeMessage.setStopSequence(i);
                tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
                tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
                tripMessage.addTripNodeMsgs(tripNodeMessage);
            }

            builder.addTripMsgs(tripMessage);
        }
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(numberTripInserts, mesg.getEntityCount());

        for (int j = 0; j < numberTripInserts; j++) {
            FeedEntity e = mesg.getEntity(j);
            assertTrue(e.hasId());
            int x = j + 1;
            String id = "A--" + String.valueOf(x);
            assertEquals(id, e.getId());
            assertFalse(e.hasIsDeleted());
            assertTrue(e.hasTripUpdate());
            assertFalse(e.hasAlert());
            assertFalse(e.hasVehicle());

            TripUpdate tripUpdate = e.getTripUpdate();
            assertTrue(tripUpdate.hasTimestamp());
            assertTrue(tripUpdate.hasTrip());

            TripDescriptor tripDescriptor = tripUpdate.getTrip();
            assertTrue(tripDescriptor.hasTripId());
            assertTrue(tripDescriptor.hasRouteId());
            assertTrue(tripDescriptor.hasScheduleRelationship());
            assertEquals("A--" + String.valueOf(x), tripDescriptor.getTripId());
            assertEquals("IWL_2c", tripDescriptor.getRouteId());
            assertEquals(ScheduleRelationship.REPLACEMENT, tripDescriptor.getScheduleRelationship());
        }

    }

    @Test
    public void testTimetableTripUpdateWithVehiclePostion() {

        TripMessage.Builder tripMessage = TripMessage.newBuilder();
        tripMessage.setTripId("101A");
        tripMessage.setRouteId("IWL_2c");
        tripMessage.setServiceId("999");
        tripMessage.setBlockId(119);
        tripMessage.setBundleId(99);
        tripMessage.setTripInstance(1);
        tripMessage.setTripSource(PbTripSource.TC_TIMETABLE);

        // Add Stops to Message

        for (int i = 1; i <= 5; i++) {
            TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
            tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
            tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
            tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
            tripNodeMessage.setStopSequence(i);
            tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
            tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
            tripMessage.addTripNodeMsgs(tripNodeMessage);
        }

        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        builder.addTripMsgs(tripMessage);
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        String csvData = "101A,IWL_2c,11:30:00,20131205,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());

        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(1, mesg.getEntityCount());

        FeedEntity e = mesg.getEntity(0);
        assertTrue(e.hasId());
        assertEquals("101A", e.getId());
        assertFalse(e.hasIsDeleted());
        assertTrue(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertFalse(e.hasVehicle());

        TripUpdate tripUpdate = e.getTripUpdate();
        assertTrue(tripUpdate.hasTimestamp());
        assertTrue(tripUpdate.hasTrip());

        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        assertTrue(tripDescriptor.hasTripId());
        assertTrue(tripDescriptor.hasRouteId());
        assertTrue(tripDescriptor.hasScheduleRelationship());
        assertEquals("101A", tripDescriptor.getTripId());
        assertEquals("IWL_2c", tripDescriptor.getRouteId());
        assertEquals(ScheduleRelationship.REPLACEMENT, tripDescriptor.getScheduleRelationship());

    }

    @Test
    public void testMultipleTimetableTripUpdateWithVehiclePosition() {

        // Number of Trip Insert Messages
        int numberTripInserts = 10;
        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        String csvData = "";
        for (int k = 1; k <= numberTripInserts; k++) {
            TripMessage.Builder tripMessage = TripMessage.newBuilder();
            tripMessage.setTripId("A--" + String.valueOf(k));
            tripMessage.setRouteId("IWL_2c");
            tripMessage.setServiceId("999");
            tripMessage.setBlockId(119);
            tripMessage.setBundleId(99);
            tripMessage.setTripInstance(1);
            tripMessage.setTripSource(PbTripSource.TC_TIMETABLE);
            csvData += "A--" + String.valueOf(k)
            + ",IWL_2c,11:30:00," + RandomStringUtils.randomNumeric(5) + ",1," + RandomStringUtils.randomNumeric(3)
            + ",Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";

            // Add Stops to Message

            for (int i = 1; i <= 5; i++) {
                TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
                tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
                tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
                tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
                tripNodeMessage.setStopSequence(i);
                tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
                tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
                tripMessage.addTripNodeMsgs(tripNodeMessage);
            }

            builder.addTripMsgs(tripMessage);
        }
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(numberTripInserts, mesg.getEntityCount());

        for (int j = 0; j < numberTripInserts; j++) {
            FeedEntity e = mesg.getEntity(j);
            assertTrue(e.hasId());
            int x = j + 1;
            String id = "A--" + String.valueOf(x);
            assertEquals(id, e.getId());
            assertFalse(e.hasIsDeleted());
            assertTrue(e.hasTripUpdate());
            assertFalse(e.hasAlert());
            assertFalse(e.hasVehicle());

            TripUpdate tripUpdate = e.getTripUpdate();
            assertTrue(tripUpdate.hasTimestamp());
            assertTrue(tripUpdate.hasTrip());

            TripDescriptor tripDescriptor = tripUpdate.getTrip();
            assertTrue(tripDescriptor.hasTripId());
            assertTrue(tripDescriptor.hasRouteId());
            assertTrue(tripDescriptor.hasScheduleRelationship());
            assertEquals("A--" + String.valueOf(x), tripDescriptor.getTripId());
            assertEquals("IWL_2c", tripDescriptor.getRouteId());
            assertEquals(ScheduleRelationship.REPLACEMENT, tripDescriptor.getScheduleRelationship());
        }

    }

    @Test
    public void testInsertTripMessage() {

        TripMessage.Builder tripMessage = TripMessage.newBuilder();
        tripMessage.setTripId("101A");
        tripMessage.setRouteId("IWL_2c");
        tripMessage.setServiceId("999");
        tripMessage.setBlockId(119);
        tripMessage.setBundleId(99);
        tripMessage.setTripInstance(1);
        tripMessage.setTripSource(PbTripSource.TC_INSERTED);

        // Add Stops to Message

        for (int i = 1; i <= 5; i++) {
            TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
            tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
            tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
            tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
            tripNodeMessage.setStopSequence(i);
            tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
            tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
            tripMessage.addTripNodeMsgs(tripNodeMessage);
        }

        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        builder.addTripMsgs(tripMessage);
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(1, mesg.getEntityCount());

        FeedEntity e = mesg.getEntity(0);
        assertTrue(e.hasId());
        assertEquals("101A", e.getId());
        assertFalse(e.hasIsDeleted());
        assertTrue(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertFalse(e.hasVehicle());

        TripUpdate tripUpdate = e.getTripUpdate();
        assertTrue(tripUpdate.hasTimestamp());
        assertTrue(tripUpdate.hasTrip());

        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        assertTrue(tripDescriptor.hasTripId());
        assertTrue(tripDescriptor.hasRouteId());
        assertTrue(tripDescriptor.hasScheduleRelationship());
        assertEquals("101A", tripDescriptor.getTripId());
        assertEquals("IWL_2c", tripDescriptor.getRouteId());
        assertEquals(ScheduleRelationship.ADDED, tripDescriptor.getScheduleRelationship());
    }

    @Test
    public void testMultipleInsertTripMessage() {

        // Number of Trip Insert Messages
        int numberTripInserts = 10;
        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);

        for (int k = 1; k <= numberTripInserts; k++) {
            TripMessage.Builder tripMessage = TripMessage.newBuilder();
            tripMessage.setTripId("A--" + String.valueOf(k));
            tripMessage.setRouteId("IWL_2c");
            tripMessage.setServiceId("999");
            tripMessage.setBlockId(119);
            tripMessage.setBundleId(99);
            tripMessage.setTripInstance(1);
            tripMessage.setTripSource(PbTripSource.TC_INSERTED);

            // Add Stops to Message

            for (int i = 1; i <= 5; i++) {
                TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
                tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
                tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
                tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
                tripNodeMessage.setStopSequence(i);
                tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
                tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
                tripMessage.addTripNodeMsgs(tripNodeMessage);
            }

            builder.addTripMsgs(tripMessage);
        }
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(numberTripInserts, mesg.getEntityCount());

        for (int j = 0; j < numberTripInserts; j++) {
            FeedEntity e = mesg.getEntity(j);
            assertTrue(e.hasId());
            int x = j + 1;
            String id = "A--" + String.valueOf(x);
            assertEquals(id, e.getId());
            assertFalse(e.hasIsDeleted());
            assertTrue(e.hasTripUpdate());
            assertFalse(e.hasAlert());
            assertFalse(e.hasVehicle());

            TripUpdate tripUpdate = e.getTripUpdate();
            assertTrue(tripUpdate.hasTimestamp());
            assertTrue(tripUpdate.hasTrip());

            TripDescriptor tripDescriptor = tripUpdate.getTrip();
            assertTrue(tripDescriptor.hasTripId());
            assertTrue(tripDescriptor.hasRouteId());
            assertTrue(tripDescriptor.hasScheduleRelationship());
            assertEquals("A--" + String.valueOf(x), tripDescriptor.getTripId());
            assertEquals("IWL_2c", tripDescriptor.getRouteId());
            assertEquals(ScheduleRelationship.ADDED, tripDescriptor.getScheduleRelationship());
        }
    }

    @Test
    public void testTripInsertWithVehiclePosition() {

        TripMessage.Builder tripMessage = TripMessage.newBuilder();
        tripMessage.setTripId("101A");
        tripMessage.setRouteId("IWL_2c");
        tripMessage.setServiceId("999");
        tripMessage.setBlockId(119);
        tripMessage.setBundleId(99);
        tripMessage.setTripInstance(1);
        tripMessage.setTripSource(PbTripSource.TC_INSERTED);

        // Add Stops to Message

        for (int i = 1; i <= 5; i++) {
            TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
            tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
            tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
            tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
            tripNodeMessage.setStopSequence(i);
            tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
            tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
            tripMessage.addTripNodeMsgs(tripNodeMessage);
        }

        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        builder.addTripMsgs(tripMessage);
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        String csvData = "101A,IWL_2c,11:30:00,20131205,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());

        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(1, mesg.getEntityCount());

        FeedEntity e = mesg.getEntity(0);
        assertTrue(e.hasId());
        assertEquals("101A", e.getId());
        assertFalse(e.hasIsDeleted());
        assertTrue(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertFalse(e.hasVehicle());

        TripUpdate tripUpdate = e.getTripUpdate();
        assertTrue(tripUpdate.hasTimestamp());
        assertTrue(tripUpdate.hasTrip());

        TripDescriptor tripDescriptor = tripUpdate.getTrip();
        assertTrue(tripDescriptor.hasTripId());
        assertTrue(tripDescriptor.hasRouteId());
        assertTrue(tripDescriptor.hasScheduleRelationship());
        assertEquals("101A", tripDescriptor.getTripId());
        assertEquals("IWL_2c", tripDescriptor.getRouteId());
        assertEquals(ScheduleRelationship.ADDED, tripDescriptor.getScheduleRelationship());

    }

    @Test
    public void testMultipleTripInsertsWithVehiclePosition() {

        // Number of Trip Insert Messages
        int numberTripInserts = 10;
        TripListMessage.Builder builder = TripListMessage.newBuilder();
        builder.setMsgTimestamp(System.currentTimeMillis() / 1000L);
        String csvData = "";
        for (int k = 1; k <= numberTripInserts; k++) {
            TripMessage.Builder tripMessage = TripMessage.newBuilder();
            tripMessage.setTripId("A--" + String.valueOf(k));
            tripMessage.setRouteId("IWL_2c");
            tripMessage.setServiceId("999");
            tripMessage.setBlockId(119);
            tripMessage.setBundleId(99);
            tripMessage.setTripInstance(1);
            tripMessage.setTripSource(PbTripSource.TC_INSERTED);
            csvData += "A--" + String.valueOf(k)
            + ",IWL_2c,11:30:00," + RandomStringUtils.randomNumeric(5) + ",1," + RandomStringUtils.randomNumeric(3)
            + ",Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";

            // Add Stops to Message

            for (int i = 1; i <= 5; i++) {
                TripNodeMessage.Builder tripNodeMessage = TripNodeMessage.newBuilder();
                tripNodeMessage.setArrivalTime(System.currentTimeMillis() / 1000L + i);
                tripNodeMessage.setDepartureTime(System.currentTimeMillis() / 1000L + 30 + i);
                tripNodeMessage.setStopId(RandomStringUtils.randomNumeric(5));
                tripNodeMessage.setStopSequence(i);
                tripNodeMessage.setStopStatus(PbStopStatus.SS_PICKUP);
                tripNodeMessage.setNodeName(RandomStringUtils.randomAlphanumeric(3).toUpperCase());
                tripMessage.addTripNodeMsgs(tripNodeMessage);
            }

            builder.addTripMsgs(tripMessage);
        }
        TripModelEntityMessage.Builder tripModelEntityMessage = TripModelEntityMessage.newBuilder();
        tripModelEntityMessage.setTimeStamp(System.currentTimeMillis() / 1000L);
        tripModelEntityMessage.setActiveTrips(builder);
        TripModelEntityMessage message = tripModelEntityMessage.build();

        StringReader reader = new StringReader(csvData);
        converter.getProtoStorage().convertAndStoreCsv(reader);

        assertTrue(converter.processLoadTripUpdates(message));
        assertTrue(converter.generateTripUpdates());
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());

        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals("1.0", mesg.getHeader().getGtfsRealtimeVersion());
        assertTrue(mesg.getHeader().getIncrementality().equals(Incrementality.FULL_DATASET));
        assertTrue(mesg.getHeader().hasTimestamp());
        assertEquals(numberTripInserts, mesg.getEntityCount());

        for (int j = 0; j < numberTripInserts; j++) {
            FeedEntity e = mesg.getEntity(j);
            assertTrue(e.hasId());
            int x = j + 1;
            String id = "A--" + String.valueOf(x);
            assertEquals(id, e.getId());
            assertFalse(e.hasIsDeleted());
            assertTrue(e.hasTripUpdate());
            assertFalse(e.hasAlert());
            assertFalse(e.hasVehicle());

            TripUpdate tripUpdate = e.getTripUpdate();
            assertTrue(tripUpdate.hasTimestamp());
            assertTrue(tripUpdate.hasTrip());

            TripDescriptor tripDescriptor = tripUpdate.getTrip();
            assertTrue(tripDescriptor.hasTripId());
            assertTrue(tripDescriptor.hasRouteId());
            assertTrue(tripDescriptor.hasScheduleRelationship());
            assertEquals("A--" + String.valueOf(x), tripDescriptor.getTripId());
            assertEquals("IWL_2c", tripDescriptor.getRouteId());
            assertEquals(ScheduleRelationship.ADDED, tripDescriptor.getScheduleRelationship());
        }

    }

}
