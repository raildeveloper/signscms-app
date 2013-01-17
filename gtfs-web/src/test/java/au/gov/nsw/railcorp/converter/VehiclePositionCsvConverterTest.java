// RailCorp 2012
package au.gov.nsw.railcorp.converter;

import au.gov.nsw.railcorp.gtfs.converter.VehiclePositionCsvConverter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.CongestionLevel;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;

import java.io.StringReader;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author John
 *
 */
public class VehiclePositionCsvConverterTest extends TestCase {

    VehiclePositionCsvConverter converter;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        converter = new VehiclePositionCsvConverter();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        converter = null;
    }

    // Test cases:
    // CONVERSION:
    // - basic content
    // - missing csv optional values
    // - missing csv required values (if any)
    // - wrong data type for column
    // - duplicate trip ids in csv
    // - empty csv
    // - empty csv row
    // - large content
    //
    // READING:
    // - process & read
    // - process read, process read checking update
    // - read null buffer
    // - verify debug results
    
    @Test
    public void testCSVBasicConvertAndVerify() {

        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
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
        assertFalse(e.hasTripUpdate());
        assertFalse(e.hasAlert());
        assertTrue(e.hasVehicle());
        
        VehiclePosition v = e.getVehicle();
        assertTrue(v.hasCurrentStatus());
        assertTrue(v.hasCongestionLevel());
        assertTrue(v.hasTimestamp());
        assertTrue(v.hasStopId());
        assertTrue(v.getCurrentStatus().equals(VehicleStopStatus.STOPPED_AT));
        assertTrue(v.getCongestionLevel().equals(CongestionLevel.RUNNING_SMOOTHLY));
        assertEquals(167293089032L, v.getTimestamp());
        assertEquals("stop1", v.getStopId());
        
        assertTrue(v.hasTrip());
        TripDescriptor trip = v.getTrip();
        assertTrue(trip.hasTripId());
        assertTrue(trip.hasRouteId());
        assertTrue(trip.hasStartDate());
        assertTrue(trip.hasStartTime());
        assertTrue(trip.hasScheduleRelationship());
        assertEquals("123.23.trip", trip.getTripId());
        assertEquals("testRoute", trip.getRouteId());
        assertEquals("11:30:00", trip.getStartTime());
        assertEquals("20121210", trip.getStartDate());
        assertTrue(trip.getScheduleRelationship().equals(ScheduleRelationship.ADDED));
        
        assertTrue(v.hasPosition());
        Position pos = v.getPosition();
        assertTrue(pos.hasBearing());
        assertTrue(pos.hasLatitude());
        assertTrue(pos.hasLongitude());
        assertTrue(pos.hasOdometer());
        assertTrue(pos.hasSpeed());
        assertEquals((float)35.4312, pos.getBearing());
        assertEquals((float)30.76864309, pos.getLatitude());
        assertEquals((float)-150.3478953, pos.getLongitude());
        assertEquals(12334.321, pos.getOdometer());
        assertEquals((float)20.23, pos.getSpeed());
        
        assertTrue(v.hasVehicle());
        VehicleDescriptor desc = v.getVehicle();
        assertTrue(desc.hasId());
        assertTrue(desc.hasLabel());
        assertTrue(desc.hasLicensePlate());
        assertEquals("A27", desc.getId());
        assertEquals("Some trip", desc.getLabel());
        assertEquals("None", desc.getLicensePlate());
            
    }
    
    @Test
    public void testMissingOptionalTripId() {
        String csvData = ",testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalRouteId() {
        String csvData = "123.23.trip,,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalStartTime() {
        String csvData = "123.23.trip,testRoute,,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalStartDate() {
        String csvData = "123.23.trip,testRoute,11:30:00,,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalScheduleRelationship() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalVehicleId() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalVehicleLabel() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalVehicleLicencePlate() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalBearing() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    public void testMissingOptionalOdometer() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalSpeed() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalCurrentStopSequence() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalStopId() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalCurrentStatus() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalTimestamp() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testMissingOptionalCongestionLevel() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }
    
    @Test
    public void testMissingRequiredLatitude() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertFalse(mesg.getEntity(0).getVehicle().hasPosition());

    }
    @Test
    public void testMissingRequiredLongitude() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertFalse(mesg.getEntity(0).getVehicle().hasPosition());
    }

    /**
     * should be impossible to have duplicate trip ID's in a single vehicle position feed - can't be in 2 places at once.
     * However, there are a few cases where trips can be renamed and different instances can exist (eg. if a service breaks down etc)
     * or for non timetabled trips (eg. U001) across network - there could be a few instances, so this test has been changed to allow
     * duplicate trip ids when the source systems report them rather than invalidate the entire feed.
     */
    @Test
    public void testDuplicateTripIds() {
        String csvData =
                "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n" +
        		"123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        assertNotNull(null, converter.getCurrentProtoBuf());
    }
    
    @Test
    public void testUninitialisedConverterBuffer() {
        assertEquals(null, converter.getCurrentProtoBuf());
        assertEquals("", converter.getCurrentProtoBufDebug());
    }
    
    @Test
    public void testVerifyDebugReadResults() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        String expectedDebug =
                "header {\n" +
                "  gtfs_realtime_version: \"1.0\"\n" +
                "  incrementality: FULL_DATASET\n" +
             // "  timestamp: 1354485021\n" +  Removed so that comparisons can be made
                "}\n" +
                "entity {\n" +
                "  id: \"1\"\n" +
                "  vehicle {\n" +
                "    trip {\n" +
                "      trip_id: \"123.23.trip\"\n" +
                "      start_time: \"11:30:00\"\n" +
                "      start_date: \"20121210\"\n" +
                "      schedule_relationship: ADDED\n" +
                "      route_id: \"testRoute\"\n" +
                "    }\n" +
                "    position {\n" +
                "      latitude: 30.768642\n" +
                "      longitude: -150.3479\n" +
                "      bearing: 35.4312\n" +
                "      odometer: 12334.321\n" +
                "      speed: 20.23\n" +
                "    }\n" +
                "    current_stop_sequence: 4\n" +
                "    current_status: STOPPED_AT\n" +
                "    timestamp: 167293089032\n" +
                "    congestion_level: RUNNING_SMOOTHLY\n" +
                "    stop_id: \"stop1\"\n" +
                "    vehicle {\n" +
                "      id: \"A27\"\n" +
                "      label: \"Some trip\"\n" +
                "      license_plate: \"None\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        String actualDebug = converter.getCurrentProtoBufDebug();
        // Strip out the timestamp (4th) line, as it changes with each execution.
        String[] splitActual = actualDebug.split("\n");
        actualDebug = "";
        for (int i = 0; i < splitActual.length; ++i)
        {
            if (i != 3) {
                actualDebug += splitActual[i] + "\n";
            }
        }
        assertEquals(expectedDebug, actualDebug);
    }
    
    @Test
    public void testProcessCSVUpdateFlush() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,31.986422,-152.9845953,90.412,12450.421,20.23,4,stop3,1,167293092032,1\n";
        reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(1, mesg.getEntityCount());
        assertEquals((float)31.986422, mesg.getEntity(0).getVehicle().getPosition().getLatitude());
        assertEquals((float)-152.9845953, mesg.getEntity(0).getVehicle().getPosition().getLongitude());
        assertEquals((float)90.412, mesg.getEntity(0).getVehicle().getPosition().getBearing());
        assertEquals((double)12450.421, mesg.getEntity(0).getVehicle().getPosition().getOdometer());
        assertEquals("stop3", mesg.getEntity(0).getVehicle().getStopId());
        assertEquals(167293092032L, mesg.getEntity(0).getVehicle().getTimestamp());
    }
    
    @Test
    public void testProcessCSVUpdateFlush2() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,31.986422,-152.9845953,90.412,12450.421,20.23,4,stop3,1,167293092032,1\n" +
                "223.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,31.986422,-152.9845953,90.412,12450.421,20.23,4,stop3,1,167293092032,1\n" +
                "323.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,31.986422,-152.9845953,90.412,12450.421,20.23,4,stop3,1,167293092032,1\n";
        reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(3, mesg.getEntityCount());
    }
    
    @Test
    public void testEmptyCSVFile() {
        String csvData = "\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        // Shouldn't have any entities, but will have FeedMessage & FeedHeader
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals(0, mesg.getEntityCount());
    }
    
    @Test
    public void testEmptyCSVRow() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n" +
        		",,,,,,,,,,,,,,,,,\n" +
        		"223.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        // Shouldn't have entities for empty line
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg.hasHeader());
        assertEquals(2, mesg.getEntityCount());
    }
    
    @Test
    public void testWrongColumnDataTypeScheduleRelationship() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,12,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        // For an 'out of range' enum value, an error will be logged, and the value omitted, but will not fail the whole conversion
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertFalse(mesg.getEntity(0).getVehicle().getTrip().hasScheduleRelationship());
    }
    @Test
    public void testWrongColumnDataTypeScheduleRelationship2() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,ADDED,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeLatitude() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,L30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeLongitude() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,A-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeBearing() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,A35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeOdometer() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,S12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeSpeed() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,Fast - 20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeCurrentStopSequence() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,Full4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeCurrentStatus() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,12,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        // For an 'out of range' enum value, an error will be logged, and the value omitted, but will not fail the whole conversion
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertFalse(mesg.getEntity(0).getVehicle().hasCurrentStatus());
    }
    @Test
    public void testWrongColumnDataTypeCurrentStatus2() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,STOPPED_AT,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeCongestionLevel() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,12\n";
        StringReader reader = new StringReader(csvData);
        // For an 'out of range' enum value, an error will be logged, and the value omitted, but will not fail the whole conversion
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertFalse(mesg.getEntity(0).getVehicle().hasCongestionLevel());
    }
    @Test
    public void testWrongColumnDataTypeCongestionLevel2() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,RUNNING_SMOOTHLY\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeTimestamp() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,11:30 July 1,1\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    
    @Test
    public void testQuotesAroundDecimals() {
        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,\"30.76864309\",-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals((float)30.76864309, mesg.getEntity(0).getVehicle().getPosition().getLatitude());
    }
    
    @Test
    public void testLargeCSVData() {
        StringReader reader = new StringReader(getLargeCSVData());
        assertTrue(converter.convertAndStoreCsv(reader));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(1372, mesg.getEntityCount());
    }

    private String getLargeCSVData() {
     StringBuffer buffer = new StringBuffer();
     buffer.append("A101,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n" );
        buffer.append("A102,testRoute,12:30:00,20121210,1,A28,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop2,1,167293089032,1\n" );
        buffer.append("A103,testRoute,13:30:00,20121210,1,A29,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop3,1,167293089032,1\n" );
        buffer.append("A104,testRoute,14:30:00,20121210,1,A30,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop4,1,167293089032,1\n" );
        buffer.append("A105,testRoute,15:30:00,20121210,1,A31,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop5,1,167293089032,1\n" );
        buffer.append("A106,testRoute,16:30:00,20121210,1,A32,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop6,1,167293089032,1\n" );
        buffer.append("A107,testRoute,17:30:00,20121210,1,A33,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop7,1,167293089032,1\n" );
        buffer.append("A108,testRoute,18:30:00,20121210,1,A34,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop8,1,167293089032,1\n" );
        buffer.append("A109,testRoute,19:30:00,20121210,1,A35,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop9,1,167293089032,1\n" );
        buffer.append("A110,testRoute,20:30:00,20121210,1,A36,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop10,1,167293089032,1\n" );
        buffer.append("A111,testRoute,21:30:00,20121210,1,A37,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop11,1,167293089032,1\n" );
        buffer.append("A112,testRoute,22:30:00,20121210,1,A38,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop12,1,167293089032,1\n" );
        buffer.append("A113,testRoute,23:30:00,20121210,1,A39,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop13,1,167293089032,1\n" );
        buffer.append("A114,testRoute,0:30:00,20121210,1,A40,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop14,1,167293089032,1\n" );
        buffer.append("A115,testRoute,1:30:00,20121210,1,A41,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop15,1,167293089032,1\n" );
        buffer.append("A116,testRoute,2:30:00,20121210,1,A42,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop16,1,167293089032,1\n" );
        buffer.append("A117,testRoute,3:30:00,20121210,1,A43,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop17,1,167293089032,1\n" );
        buffer.append("A118,testRoute,4:30:00,20121210,1,A44,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop18,1,167293089032,1\n" );
        buffer.append("A119,testRoute,5:30:00,20121210,1,A45,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop19,1,167293089032,1\n" );
        buffer.append("A120,testRoute,6:30:00,20121210,1,A46,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop20,1,167293089032,1\n" );
        buffer.append("A121,testRoute,7:30:00,20121210,1,A47,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop21,1,167293089032,1\n" );
        buffer.append("A122,testRoute,8:30:00,20121210,1,A48,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop22,1,167293089032,1\n" );
        buffer.append("A123,testRoute,9:30:00,20121210,1,A49,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop23,1,167293089032,1\n" );
        buffer.append("A124,testRoute,10:30:00,20121210,1,A50,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop24,1,167293089032,1\n" );
        buffer.append("A125,testRoute,11:30:00,20121210,1,A51,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop25,1,167293089032,1\n" );
        buffer.append("A126,testRoute,12:30:00,20121210,1,A52,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop26,1,167293089032,1\n" );
        buffer.append("A127,testRoute,13:30:00,20121210,1,A53,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop27,1,167293089032,1\n" );
        buffer.append("A128,testRoute,14:30:00,20121210,1,A54,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop28,1,167293089032,1\n" );
        buffer.append("A129,testRoute,15:30:00,20121210,1,A55,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop29,1,167293089032,1\n" );
        buffer.append("A130,testRoute,16:30:00,20121210,1,A56,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop30,1,167293089032,1\n" );
        buffer.append("A131,testRoute,17:30:00,20121210,1,A57,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop31,1,167293089032,1\n" );
        buffer.append("A132,testRoute,18:30:00,20121210,1,A58,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop32,1,167293089032,1\n" );
        buffer.append("A133,testRoute,19:30:00,20121210,1,A59,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop33,1,167293089032,1\n" );
        buffer.append("A134,testRoute,20:30:00,20121210,1,A60,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop34,1,167293089032,1\n" );
        buffer.append("A135,testRoute,21:30:00,20121210,1,A61,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop35,1,167293089032,1\n" );
        buffer.append("A136,testRoute,22:30:00,20121210,1,A62,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop36,1,167293089032,1\n" );
        buffer.append("A137,testRoute,23:30:00,20121210,1,A63,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop37,1,167293089032,1\n" );
        buffer.append("A138,testRoute,0:30:00,20121210,1,A64,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop38,1,167293089032,1\n" );
        buffer.append("A139,testRoute,1:30:00,20121210,1,A65,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop39,1,167293089032,1\n" );
        buffer.append("A140,testRoute,2:30:00,20121210,1,A66,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop40,1,167293089032,1\n" );
        buffer.append("A141,testRoute,3:30:00,20121210,1,A67,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop41,1,167293089032,1\n" );
        buffer.append("A142,testRoute,4:30:00,20121210,1,A68,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop42,1,167293089032,1\n" );
        buffer.append("A143,testRoute,5:30:00,20121210,1,A69,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop43,1,167293089032,1\n" );
        buffer.append("A144,testRoute,6:30:00,20121210,1,A70,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop44,1,167293089032,1\n" );
        buffer.append("A145,testRoute,7:30:00,20121210,1,A71,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop45,1,167293089032,1\n" );
        buffer.append("A146,testRoute,8:30:00,20121210,1,A72,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop46,1,167293089032,1\n" );
        buffer.append("A147,testRoute,9:30:00,20121210,1,A73,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop47,1,167293089032,1\n" );
        buffer.append("A148,testRoute,10:30:00,20121210,1,A74,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop48,1,167293089032,1\n" );
        buffer.append("A149,testRoute,11:30:00,20121210,1,A75,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop49,1,167293089032,1\n" );
        buffer.append("A150,testRoute,12:30:00,20121210,1,A76,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop50,1,167293089032,1\n" );
        buffer.append("A151,testRoute,13:30:00,20121210,1,A77,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop51,1,167293089032,1\n" );
        buffer.append("A152,testRoute,14:30:00,20121210,1,A78,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop52,1,167293089032,1\n" );
        buffer.append("A153,testRoute,15:30:00,20121210,1,A79,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop53,1,167293089032,1\n" );
        buffer.append("A154,testRoute,16:30:00,20121210,1,A80,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop54,1,167293089032,1\n" );
        buffer.append("A155,testRoute,17:30:00,20121210,1,A81,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop55,1,167293089032,1\n" );
        buffer.append("A156,testRoute,18:30:00,20121210,1,A82,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop56,1,167293089032,1\n" );
        buffer.append("A157,testRoute,19:30:00,20121210,1,A83,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop57,1,167293089032,1\n" );
        buffer.append("A158,testRoute,20:30:00,20121210,1,A84,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop58,1,167293089032,1\n" );
        buffer.append("A159,testRoute,21:30:00,20121210,1,A85,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop59,1,167293089032,1\n" );
        buffer.append("A160,testRoute,22:30:00,20121210,1,A86,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop60,1,167293089032,1\n" );
        buffer.append("A161,testRoute,23:30:00,20121210,1,A87,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop61,1,167293089032,1\n" );
        buffer.append("A162,testRoute,0:30:00,20121210,1,A88,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop62,1,167293089032,1\n" );
        buffer.append("A163,testRoute,1:30:00,20121210,1,A89,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop63,1,167293089032,1\n" );
        buffer.append("A164,testRoute,2:30:00,20121210,1,A90,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop64,1,167293089032,1\n" );
        buffer.append("A165,testRoute,3:30:00,20121210,1,A91,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop65,1,167293089032,1\n" );
        buffer.append("A166,testRoute,4:30:00,20121210,1,A92,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop66,1,167293089032,1\n" );
        buffer.append("A167,testRoute,5:30:00,20121210,1,A93,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop67,1,167293089032,1\n" );
        buffer.append("A168,testRoute,6:30:00,20121210,1,A94,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop68,1,167293089032,1\n" );
        buffer.append("A169,testRoute,7:30:00,20121210,1,A95,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop69,1,167293089032,1\n" );
        buffer.append("A170,testRoute,8:30:00,20121210,1,A96,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop70,1,167293089032,1\n" );
        buffer.append("A171,testRoute,9:30:00,20121210,1,A97,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop71,1,167293089032,1\n" );
        buffer.append("A172,testRoute,10:30:00,20121210,1,A98,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop72,1,167293089032,1\n" );
        buffer.append("A173,testRoute,11:30:00,20121210,1,A99,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop73,1,167293089032,1\n" );
        buffer.append("A174,testRoute,12:30:00,20121210,1,A100,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop74,1,167293089032,1\n" );
        buffer.append("A175,testRoute,13:30:00,20121210,1,A101,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop75,1,167293089032,1\n" );
        buffer.append("A176,testRoute,14:30:00,20121210,1,A102,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop76,1,167293089032,1\n" );
        buffer.append("A177,testRoute,15:30:00,20121210,1,A103,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop77,1,167293089032,1\n" );
        buffer.append("A178,testRoute,16:30:00,20121210,1,A104,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop78,1,167293089032,1\n" );
        buffer.append("A179,testRoute,17:30:00,20121210,1,A105,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop79,1,167293089032,1\n" );
        buffer.append("A180,testRoute,18:30:00,20121210,1,A106,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop80,1,167293089032,1\n" );
        buffer.append("A181,testRoute,19:30:00,20121210,1,A107,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop81,1,167293089032,1\n" );
        buffer.append("A182,testRoute,20:30:00,20121210,1,A108,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop82,1,167293089032,1\n" );
        buffer.append("A183,testRoute,21:30:00,20121210,1,A109,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop83,1,167293089032,1\n" );
        buffer.append("A184,testRoute,22:30:00,20121210,1,A110,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop84,1,167293089032,1\n" );
        buffer.append("A185,testRoute,23:30:00,20121210,1,A111,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop85,1,167293089032,1\n" );
        buffer.append("A186,testRoute,0:30:00,20121210,1,A112,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop86,1,167293089032,1\n" );
        buffer.append("A187,testRoute,1:30:00,20121210,1,A113,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop87,1,167293089032,1\n" );
        buffer.append("A188,testRoute,2:30:00,20121210,1,A114,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop88,1,167293089032,1\n" );
        buffer.append("A189,testRoute,3:30:00,20121210,1,A115,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop89,1,167293089032,1\n" );
        buffer.append("A190,testRoute,4:30:00,20121210,1,A116,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop90,1,167293089032,1\n" );
        buffer.append("A191,testRoute,5:30:00,20121210,1,A117,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop91,1,167293089032,1\n" );
        buffer.append("A192,testRoute,6:30:00,20121210,1,A118,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop92,1,167293089032,1\n" );
        buffer.append("A193,testRoute,7:30:00,20121210,1,A119,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop93,1,167293089032,1\n" );
        buffer.append("A194,testRoute,8:30:00,20121210,1,A120,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop94,1,167293089032,1\n" );
        buffer.append("A195,testRoute,9:30:00,20121210,1,A121,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop95,1,167293089032,1\n" );
        buffer.append("A196,testRoute,10:30:00,20121210,1,A122,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop96,1,167293089032,1\n" );
        buffer.append("A197,testRoute,11:30:00,20121210,1,A123,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop97,1,167293089032,1\n" );
        buffer.append("A198,testRoute,12:30:00,20121210,1,A124,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop98,1,167293089032,1\n" );
        buffer.append("A199,testRoute,13:30:00,20121210,1,A125,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop99,1,167293089032,1\n" );
        buffer.append("A200,testRoute,14:30:00,20121210,1,A126,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop100,1,167293089032,1\n" );
        buffer.append("A201,testRoute,15:30:00,20121210,1,A127,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop101,1,167293089032,1\n" );
        buffer.append("A202,testRoute,16:30:00,20121210,1,A128,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop102,1,167293089032,1\n" );
        buffer.append("A203,testRoute,17:30:00,20121210,1,A129,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop103,1,167293089032,1\n" );
        buffer.append("A204,testRoute,18:30:00,20121210,1,A130,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop104,1,167293089032,1\n" );
        buffer.append("A205,testRoute,19:30:00,20121210,1,A131,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop105,1,167293089032,1\n" );
        buffer.append("A206,testRoute,20:30:00,20121210,1,A132,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop106,1,167293089032,1\n" );
        buffer.append("A207,testRoute,21:30:00,20121210,1,A133,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop107,1,167293089032,1\n" );
        buffer.append("A208,testRoute,22:30:00,20121210,1,A134,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop108,1,167293089032,1\n" );
        buffer.append("A209,testRoute,23:30:00,20121210,1,A135,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop109,1,167293089032,1\n" );
        buffer.append("A210,testRoute,0:30:00,20121210,1,A136,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop110,1,167293089032,1\n" );
        buffer.append("A211,testRoute,1:30:00,20121210,1,A137,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop111,1,167293089032,1\n" );
        buffer.append("A212,testRoute,2:30:00,20121210,1,A138,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop112,1,167293089032,1\n" );
        buffer.append("A213,testRoute,3:30:00,20121210,1,A139,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop113,1,167293089032,1\n" );
        buffer.append("A214,testRoute,4:30:00,20121210,1,A140,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop114,1,167293089032,1\n" );
        buffer.append("A215,testRoute,5:30:00,20121210,1,A141,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop115,1,167293089032,1\n" );
        buffer.append("A216,testRoute,6:30:00,20121210,1,A142,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop116,1,167293089032,1\n" );
        buffer.append("A217,testRoute,7:30:00,20121210,1,A143,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop117,1,167293089032,1\n" );
        buffer.append("A218,testRoute,8:30:00,20121210,1,A144,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop118,1,167293089032,1\n" );
        buffer.append("A219,testRoute,9:30:00,20121210,1,A145,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop119,1,167293089032,1\n" );
        buffer.append("A220,testRoute,10:30:00,20121210,1,A146,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop120,1,167293089032,1\n" );
        buffer.append("A221,testRoute,11:30:00,20121210,1,A147,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop121,1,167293089032,1\n" );
        buffer.append("A222,testRoute,12:30:00,20121210,1,A148,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop122,1,167293089032,1\n" );
        buffer.append("A223,testRoute,13:30:00,20121210,1,A149,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop123,1,167293089032,1\n" );
        buffer.append("A224,testRoute,14:30:00,20121210,1,A150,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop124,1,167293089032,1\n" );
        buffer.append("A225,testRoute,15:30:00,20121210,1,A151,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop125,1,167293089032,1\n" );
        buffer.append("A226,testRoute,16:30:00,20121210,1,A152,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop126,1,167293089032,1\n" );
        buffer.append("A227,testRoute,17:30:00,20121210,1,A153,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop127,1,167293089032,1\n" );
        buffer.append("A228,testRoute,18:30:00,20121210,1,A154,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop128,1,167293089032,1\n" );
        buffer.append("A229,testRoute,19:30:00,20121210,1,A155,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop129,1,167293089032,1\n" );
        buffer.append("A230,testRoute,20:30:00,20121210,1,A156,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop130,1,167293089032,1\n" );
        buffer.append("A231,testRoute,21:30:00,20121210,1,A157,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop131,1,167293089032,1\n" );
        buffer.append("A232,testRoute,22:30:00,20121210,1,A158,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop132,1,167293089032,1\n" );
        buffer.append("A233,testRoute,23:30:00,20121210,1,A159,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop133,1,167293089032,1\n" );
        buffer.append("A234,testRoute,0:30:00,20121210,1,A160,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop134,1,167293089032,1\n" );
        buffer.append("A235,testRoute,1:30:00,20121210,1,A161,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop135,1,167293089032,1\n" );
        buffer.append("A236,testRoute,2:30:00,20121210,1,A162,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop136,1,167293089032,1\n" );
        buffer.append("A237,testRoute,3:30:00,20121210,1,A163,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop137,1,167293089032,1\n" );
        buffer.append("A238,testRoute,4:30:00,20121210,1,A164,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop138,1,167293089032,1\n" );
        buffer.append("A239,testRoute,5:30:00,20121210,1,A165,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop139,1,167293089032,1\n" );
        buffer.append("A240,testRoute,6:30:00,20121210,1,A166,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop140,1,167293089032,1\n" );
        buffer.append("A241,testRoute,7:30:00,20121210,1,A167,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop141,1,167293089032,1\n" );
        buffer.append("A242,testRoute,8:30:00,20121210,1,A168,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop142,1,167293089032,1\n" );
        buffer.append("A243,testRoute,9:30:00,20121210,1,A169,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop143,1,167293089032,1\n" );
        buffer.append("A244,testRoute,10:30:00,20121210,1,A170,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop144,1,167293089032,1\n" );
        buffer.append("A245,testRoute,11:30:00,20121210,1,A171,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop145,1,167293089032,1\n" );
        buffer.append("A246,testRoute,12:30:00,20121210,1,A172,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop146,1,167293089032,1\n" );
        buffer.append("A247,testRoute,13:30:00,20121210,1,A173,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop147,1,167293089032,1\n" );
        buffer.append("A248,testRoute,14:30:00,20121210,1,A174,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop148,1,167293089032,1\n" );
        buffer.append("A249,testRoute,15:30:00,20121210,1,A175,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop149,1,167293089032,1\n" );
        buffer.append("A250,testRoute,16:30:00,20121210,1,A176,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop150,1,167293089032,1\n" );
        buffer.append("A251,testRoute,17:30:00,20121210,1,A177,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop151,1,167293089032,1\n" );
        buffer.append("A252,testRoute,18:30:00,20121210,1,A178,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop152,1,167293089032,1\n" );
        buffer.append("A253,testRoute,19:30:00,20121210,1,A179,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop153,1,167293089032,1\n" );
        buffer.append("A254,testRoute,20:30:00,20121210,1,A180,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop154,1,167293089032,1\n" );
        buffer.append("A255,testRoute,21:30:00,20121210,1,A181,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop155,1,167293089032,1\n" );
        buffer.append("A256,testRoute,22:30:00,20121210,1,A182,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop156,1,167293089032,1\n" );
        buffer.append("A257,testRoute,23:30:00,20121210,1,A183,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop157,1,167293089032,1\n" );
        buffer.append("A258,testRoute,0:30:00,20121210,1,A184,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop158,1,167293089032,1\n" );
        buffer.append("A259,testRoute,1:30:00,20121210,1,A185,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop159,1,167293089032,1\n" );
        buffer.append("A260,testRoute,2:30:00,20121210,1,A186,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop160,1,167293089032,1\n" );
        buffer.append("A261,testRoute,3:30:00,20121210,1,A187,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop161,1,167293089032,1\n" );
        buffer.append("A262,testRoute,4:30:00,20121210,1,A188,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop162,1,167293089032,1\n" );
        buffer.append("A263,testRoute,5:30:00,20121210,1,A189,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop163,1,167293089032,1\n" );
        buffer.append("A264,testRoute,6:30:00,20121210,1,A190,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop164,1,167293089032,1\n" );
        buffer.append("A265,testRoute,7:30:00,20121210,1,A191,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop165,1,167293089032,1\n" );
        buffer.append("A266,testRoute,8:30:00,20121210,1,A192,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop166,1,167293089032,1\n" );
        buffer.append("A267,testRoute,9:30:00,20121210,1,A193,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop167,1,167293089032,1\n" );
        buffer.append("A268,testRoute,10:30:00,20121210,1,A194,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop168,1,167293089032,1\n" );
        buffer.append("A269,testRoute,11:30:00,20121210,1,A195,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop169,1,167293089032,1\n" );
        buffer.append("A270,testRoute,12:30:00,20121210,1,A196,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop170,1,167293089032,1\n" );
        buffer.append("A271,testRoute,13:30:00,20121210,1,A197,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop171,1,167293089032,1\n" );
        buffer.append("A272,testRoute,14:30:00,20121210,1,A198,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop172,1,167293089032,1\n" );
        buffer.append("A273,testRoute,15:30:00,20121210,1,A199,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop173,1,167293089032,1\n" );
        buffer.append("A274,testRoute,16:30:00,20121210,1,A200,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop174,1,167293089032,1\n" );
        buffer.append("A275,testRoute,17:30:00,20121210,1,A201,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop175,1,167293089032,1\n" );
        buffer.append("A276,testRoute,18:30:00,20121210,1,A202,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop176,1,167293089032,1\n" );
        buffer.append("A277,testRoute,19:30:00,20121210,1,A203,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop177,1,167293089032,1\n" );
        buffer.append("A278,testRoute,20:30:00,20121210,1,A204,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop178,1,167293089032,1\n" );
        buffer.append("A279,testRoute,21:30:00,20121210,1,A205,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop179,1,167293089032,1\n" );
        buffer.append("A280,testRoute,22:30:00,20121210,1,A206,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop180,1,167293089032,1\n" );
        buffer.append("A281,testRoute,23:30:00,20121210,1,A207,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop181,1,167293089032,1\n" );
        buffer.append("A282,testRoute,0:30:00,20121210,1,A208,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop182,1,167293089032,1\n" );
        buffer.append("A283,testRoute,1:30:00,20121210,1,A209,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop183,1,167293089032,1\n" );
        buffer.append("A284,testRoute,2:30:00,20121210,1,A210,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop184,1,167293089032,1\n" );
        buffer.append("A285,testRoute,3:30:00,20121210,1,A211,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop185,1,167293089032,1\n" );
        buffer.append("A286,testRoute,4:30:00,20121210,1,A212,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop186,1,167293089032,1\n" );
        buffer.append("A287,testRoute,5:30:00,20121210,1,A213,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop187,1,167293089032,1\n" );
        buffer.append("A288,testRoute,6:30:00,20121210,1,A214,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop188,1,167293089032,1\n" );
        buffer.append("A289,testRoute,7:30:00,20121210,1,A215,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop189,1,167293089032,1\n" );
        buffer.append("A290,testRoute,8:30:00,20121210,1,A216,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop190,1,167293089032,1\n" );
        buffer.append("A291,testRoute,9:30:00,20121210,1,A217,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop191,1,167293089032,1\n" );
        buffer.append("A292,testRoute,10:30:00,20121210,1,A218,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop192,1,167293089032,1\n" );
        buffer.append("A293,testRoute,11:30:00,20121210,1,A219,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop193,1,167293089032,1\n" );
        buffer.append("A294,testRoute,12:30:00,20121210,1,A220,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop194,1,167293089032,1\n" );
        buffer.append("A295,testRoute,13:30:00,20121210,1,A221,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop195,1,167293089032,1\n" );
        buffer.append("A296,testRoute,14:30:00,20121210,1,A222,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop196,1,167293089032,1\n" );
        buffer.append("A297,testRoute,15:30:00,20121210,1,A223,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop197,1,167293089032,1\n" );
        buffer.append("A298,testRoute,16:30:00,20121210,1,A224,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop198,1,167293089032,1\n" );
        buffer.append("A299,testRoute,17:30:00,20121210,1,A225,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop199,1,167293089032,1\n" );
        buffer.append("A300,testRoute,18:30:00,20121210,1,A226,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop200,1,167293089032,1\n" );
        buffer.append("A301,testRoute,19:30:00,20121210,1,A227,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop201,1,167293089032,1\n" );
        buffer.append("A302,testRoute,20:30:00,20121210,1,A228,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop202,1,167293089032,1\n" );
        buffer.append("A303,testRoute,21:30:00,20121210,1,A229,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop203,1,167293089032,1\n" );
        buffer.append("A304,testRoute,22:30:00,20121210,1,A230,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop204,1,167293089032,1\n" );
        buffer.append("A305,testRoute,23:30:00,20121210,1,A231,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop205,1,167293089032,1\n" );
        buffer.append("A306,testRoute,0:30:00,20121210,1,A232,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop206,1,167293089032,1\n" );
        buffer.append("A307,testRoute,1:30:00,20121210,1,A233,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop207,1,167293089032,1\n" );
        buffer.append("A308,testRoute,2:30:00,20121210,1,A234,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop208,1,167293089032,1\n" );
        buffer.append("A309,testRoute,3:30:00,20121210,1,A235,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop209,1,167293089032,1\n" );
        buffer.append("A310,testRoute,4:30:00,20121210,1,A236,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop210,1,167293089032,1\n" );
        buffer.append("A311,testRoute,5:30:00,20121210,1,A237,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop211,1,167293089032,1\n" );
        buffer.append("A312,testRoute,6:30:00,20121210,1,A238,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop212,1,167293089032,1\n" );
        buffer.append("A313,testRoute,7:30:00,20121210,1,A239,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop213,1,167293089032,1\n" );
        buffer.append("A314,testRoute,8:30:00,20121210,1,A240,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop214,1,167293089032,1\n" );
        buffer.append("A315,testRoute,9:30:00,20121210,1,A241,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop215,1,167293089032,1\n" );
        buffer.append("A316,testRoute,10:30:00,20121210,1,A242,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop216,1,167293089032,1\n" );
        buffer.append("A317,testRoute,11:30:00,20121210,1,A243,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop217,1,167293089032,1\n" );
        buffer.append("A318,testRoute,12:30:00,20121210,1,A244,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop218,1,167293089032,1\n" );
        buffer.append("A319,testRoute,13:30:00,20121210,1,A245,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop219,1,167293089032,1\n" );
        buffer.append("A320,testRoute,14:30:00,20121210,1,A246,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop220,1,167293089032,1\n" );
        buffer.append("A321,testRoute,15:30:00,20121210,1,A247,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop221,1,167293089032,1\n" );
        buffer.append("A322,testRoute,16:30:00,20121210,1,A248,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop222,1,167293089032,1\n" );
        buffer.append("A323,testRoute,17:30:00,20121210,1,A249,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop223,1,167293089032,1\n" );
        buffer.append("A324,testRoute,18:30:00,20121210,1,A250,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop224,1,167293089032,1\n" );
        buffer.append("A325,testRoute,19:30:00,20121210,1,A251,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop225,1,167293089032,1\n" );
        buffer.append("A326,testRoute,20:30:00,20121210,1,A252,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop226,1,167293089032,1\n" );
        buffer.append("A327,testRoute,21:30:00,20121210,1,A253,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop227,1,167293089032,1\n" );
        buffer.append("A328,testRoute,22:30:00,20121210,1,A254,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop228,1,167293089032,1\n" );
        buffer.append("A329,testRoute,23:30:00,20121210,1,A255,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop229,1,167293089032,1\n" );
        buffer.append("A330,testRoute,0:30:00,20121210,1,A256,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop230,1,167293089032,1\n" );
        buffer.append("A331,testRoute,1:30:00,20121210,1,A257,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop231,1,167293089032,1\n" );
        buffer.append("A332,testRoute,2:30:00,20121210,1,A258,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop232,1,167293089032,1\n" );
        buffer.append("A333,testRoute,3:30:00,20121210,1,A259,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop233,1,167293089032,1\n" );
        buffer.append("A334,testRoute,4:30:00,20121210,1,A260,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop234,1,167293089032,1\n" );
        buffer.append("A335,testRoute,5:30:00,20121210,1,A261,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop235,1,167293089032,1\n" );
        buffer.append("A336,testRoute,6:30:00,20121210,1,A262,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop236,1,167293089032,1\n" );
        buffer.append("A337,testRoute,7:30:00,20121210,1,A263,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop237,1,167293089032,1\n" );
        buffer.append("A338,testRoute,8:30:00,20121210,1,A264,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop238,1,167293089032,1\n" );
        buffer.append("A339,testRoute,9:30:00,20121210,1,A265,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop239,1,167293089032,1\n" );
        buffer.append("A340,testRoute,10:30:00,20121210,1,A266,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop240,1,167293089032,1\n" );
        buffer.append("A341,testRoute,11:30:00,20121210,1,A267,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop241,1,167293089032,1\n" );
        buffer.append("A342,testRoute,12:30:00,20121210,1,A268,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop242,1,167293089032,1\n" );
        buffer.append("A343,testRoute,13:30:00,20121210,1,A269,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop243,1,167293089032,1\n" );
        buffer.append("A344,testRoute,14:30:00,20121210,1,A270,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop244,1,167293089032,1\n" );
        buffer.append("A345,testRoute,15:30:00,20121210,1,A271,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop245,1,167293089032,1\n" );
        buffer.append("A346,testRoute,16:30:00,20121210,1,A272,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop246,1,167293089032,1\n" );
        buffer.append("A347,testRoute,17:30:00,20121210,1,A273,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop247,1,167293089032,1\n" );
        buffer.append("A348,testRoute,18:30:00,20121210,1,A274,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop248,1,167293089032,1\n" );
        buffer.append("A349,testRoute,19:30:00,20121210,1,A275,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop249,1,167293089032,1\n" );
        buffer.append("A350,testRoute,20:30:00,20121210,1,A276,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop250,1,167293089032,1\n" );
        buffer.append("A351,testRoute,21:30:00,20121210,1,A277,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop251,1,167293089032,1\n" );
        buffer.append("A352,testRoute,22:30:00,20121210,1,A278,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop252,1,167293089032,1\n" );
        buffer.append("A353,testRoute,23:30:00,20121210,1,A279,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop253,1,167293089032,1\n" );
        buffer.append("A354,testRoute,0:30:00,20121210,1,A280,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop254,1,167293089032,1\n" );
        buffer.append("A355,testRoute,1:30:00,20121210,1,A281,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop255,1,167293089032,1\n" );
        buffer.append("A356,testRoute,2:30:00,20121210,1,A282,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop256,1,167293089032,1\n" );
        buffer.append("A357,testRoute,3:30:00,20121210,1,A283,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop257,1,167293089032,1\n" );
        buffer.append("A358,testRoute,4:30:00,20121210,1,A284,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop258,1,167293089032,1\n" );
        buffer.append("A359,testRoute,5:30:00,20121210,1,A285,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop259,1,167293089032,1\n" );
        buffer.append("A360,testRoute,6:30:00,20121210,1,A286,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop260,1,167293089032,1\n" );
        buffer.append("A361,testRoute,7:30:00,20121210,1,A287,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop261,1,167293089032,1\n" );
        buffer.append("A362,testRoute,8:30:00,20121210,1,A288,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop262,1,167293089032,1\n" );
        buffer.append("A363,testRoute,9:30:00,20121210,1,A289,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop263,1,167293089032,1\n" );
        buffer.append("A364,testRoute,10:30:00,20121210,1,A290,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop264,1,167293089032,1\n" );
        buffer.append("A365,testRoute,11:30:00,20121210,1,A291,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop265,1,167293089032,1\n" );
        buffer.append("A366,testRoute,12:30:00,20121210,1,A292,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop266,1,167293089032,1\n" );
        buffer.append("A367,testRoute,13:30:00,20121210,1,A293,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop267,1,167293089032,1\n" );
        buffer.append("A368,testRoute,14:30:00,20121210,1,A294,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop268,1,167293089032,1\n" );
        buffer.append("A369,testRoute,15:30:00,20121210,1,A295,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop269,1,167293089032,1\n" );
        buffer.append("A370,testRoute,16:30:00,20121210,1,A296,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop270,1,167293089032,1\n" );
        buffer.append("A371,testRoute,17:30:00,20121210,1,A297,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop271,1,167293089032,1\n" );
        buffer.append("A372,testRoute,18:30:00,20121210,1,A298,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop272,1,167293089032,1\n" );
        buffer.append("A373,testRoute,19:30:00,20121210,1,A299,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop273,1,167293089032,1\n" );
        buffer.append("A374,testRoute,20:30:00,20121210,1,A300,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop274,1,167293089032,1\n" );
        buffer.append("A375,testRoute,21:30:00,20121210,1,A301,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop275,1,167293089032,1\n" );
        buffer.append("A376,testRoute,22:30:00,20121210,1,A302,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop276,1,167293089032,1\n" );
        buffer.append("A377,testRoute,23:30:00,20121210,1,A303,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop277,1,167293089032,1\n" );
        buffer.append("A378,testRoute,0:30:00,20121210,1,A304,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop278,1,167293089032,1\n" );
        buffer.append("A379,testRoute,1:30:00,20121210,1,A305,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop279,1,167293089032,1\n" );
        buffer.append("A380,testRoute,2:30:00,20121210,1,A306,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop280,1,167293089032,1\n" );
        buffer.append("A381,testRoute,3:30:00,20121210,1,A307,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop281,1,167293089032,1\n" );
        buffer.append("A382,testRoute,4:30:00,20121210,1,A308,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop282,1,167293089032,1\n" );
        buffer.append("A383,testRoute,5:30:00,20121210,1,A309,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop283,1,167293089032,1\n" );
        buffer.append("A384,testRoute,6:30:00,20121210,1,A310,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop284,1,167293089032,1\n" );
        buffer.append("A385,testRoute,7:30:00,20121210,1,A311,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop285,1,167293089032,1\n" );
        buffer.append("A386,testRoute,8:30:00,20121210,1,A312,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop286,1,167293089032,1\n" );
        buffer.append("A387,testRoute,9:30:00,20121210,1,A313,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop287,1,167293089032,1\n" );
        buffer.append("A388,testRoute,10:30:00,20121210,1,A314,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop288,1,167293089032,1\n" );
        buffer.append("A389,testRoute,11:30:00,20121210,1,A315,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop289,1,167293089032,1\n" );
        buffer.append("A390,testRoute,12:30:00,20121210,1,A316,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop290,1,167293089032,1\n" );
        buffer.append("A391,testRoute,13:30:00,20121210,1,A317,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop291,1,167293089032,1\n" );
        buffer.append("A392,testRoute,14:30:00,20121210,1,A318,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop292,1,167293089032,1\n" );
        buffer.append("A393,testRoute,15:30:00,20121210,1,A319,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop293,1,167293089032,1\n" );
        buffer.append("A394,testRoute,16:30:00,20121210,1,A320,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop294,1,167293089032,1\n" );
        buffer.append("A395,testRoute,17:30:00,20121210,1,A321,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop295,1,167293089032,1\n" );
        buffer.append("A396,testRoute,18:30:00,20121210,1,A322,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop296,1,167293089032,1\n" );
        buffer.append("A397,testRoute,19:30:00,20121210,1,A323,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop297,1,167293089032,1\n" );
        buffer.append("A398,testRoute,20:30:00,20121210,1,A324,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop298,1,167293089032,1\n" );
        buffer.append("A399,testRoute,21:30:00,20121210,1,A325,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop299,1,167293089032,1\n" );
        buffer.append("A400,testRoute,22:30:00,20121210,1,A326,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop300,1,167293089032,1\n" );
        buffer.append("A401,testRoute,23:30:00,20121210,1,A327,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop301,1,167293089032,1\n" );
        buffer.append("A402,testRoute,0:30:00,20121210,1,A328,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop302,1,167293089032,1\n" );
        buffer.append("A403,testRoute,1:30:00,20121210,1,A329,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop303,1,167293089032,1\n" );
        buffer.append("A404,testRoute,2:30:00,20121210,1,A330,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop304,1,167293089032,1\n" );
        buffer.append("A405,testRoute,3:30:00,20121210,1,A331,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop305,1,167293089032,1\n" );
        buffer.append("A406,testRoute,4:30:00,20121210,1,A332,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop306,1,167293089032,1\n" );
        buffer.append("A407,testRoute,5:30:00,20121210,1,A333,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop307,1,167293089032,1\n" );
        buffer.append("A408,testRoute,6:30:00,20121210,1,A334,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop308,1,167293089032,1\n" );
        buffer.append("A409,testRoute,7:30:00,20121210,1,A335,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop309,1,167293089032,1\n" );
        buffer.append("A410,testRoute,8:30:00,20121210,1,A336,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop310,1,167293089032,1\n" );
        buffer.append("A411,testRoute,9:30:00,20121210,1,A337,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop311,1,167293089032,1\n" );
        buffer.append("A412,testRoute,10:30:00,20121210,1,A338,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop312,1,167293089032,1\n" );
        buffer.append("A413,testRoute,11:30:00,20121210,1,A339,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop313,1,167293089032,1\n" );
        buffer.append("A414,testRoute,12:30:00,20121210,1,A340,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop314,1,167293089032,1\n" );
        buffer.append("A415,testRoute,13:30:00,20121210,1,A341,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop315,1,167293089032,1\n" );
        buffer.append("A416,testRoute,14:30:00,20121210,1,A342,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop316,1,167293089032,1\n" );
        buffer.append("A417,testRoute,15:30:00,20121210,1,A343,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop317,1,167293089032,1\n" );
        buffer.append("A418,testRoute,16:30:00,20121210,1,A344,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop318,1,167293089032,1\n" );
        buffer.append("A419,testRoute,17:30:00,20121210,1,A345,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop319,1,167293089032,1\n" );
        buffer.append("A420,testRoute,18:30:00,20121210,1,A346,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop320,1,167293089032,1\n" );
        buffer.append("A421,testRoute,19:30:00,20121210,1,A347,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop321,1,167293089032,1\n" );
        buffer.append("A422,testRoute,20:30:00,20121210,1,A348,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop322,1,167293089032,1\n" );
        buffer.append("A423,testRoute,21:30:00,20121210,1,A349,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop323,1,167293089032,1\n" );
        buffer.append("A424,testRoute,22:30:00,20121210,1,A350,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop324,1,167293089032,1\n" );
        buffer.append("A425,testRoute,23:30:00,20121210,1,A351,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop325,1,167293089032,1\n" );
        buffer.append("A426,testRoute,0:30:00,20121210,1,A352,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop326,1,167293089032,1\n" );
        buffer.append("A427,testRoute,1:30:00,20121210,1,A353,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop327,1,167293089032,1\n" );
        buffer.append("A428,testRoute,2:30:00,20121210,1,A354,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop328,1,167293089032,1\n" );
        buffer.append("A429,testRoute,3:30:00,20121210,1,A355,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop329,1,167293089032,1\n" );
        buffer.append("A430,testRoute,4:30:00,20121210,1,A356,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop330,1,167293089032,1\n" );
        buffer.append("A431,testRoute,5:30:00,20121210,1,A357,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop331,1,167293089032,1\n" );
        buffer.append("A432,testRoute,6:30:00,20121210,1,A358,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop332,1,167293089032,1\n" );
        buffer.append("A433,testRoute,7:30:00,20121210,1,A359,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop333,1,167293089032,1\n" );
        buffer.append("A434,testRoute,8:30:00,20121210,1,A360,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop334,1,167293089032,1\n" );
        buffer.append("A435,testRoute,9:30:00,20121210,1,A361,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop335,1,167293089032,1\n" );
        buffer.append("A436,testRoute,10:30:00,20121210,1,A362,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop336,1,167293089032,1\n" );
        buffer.append("A437,testRoute,11:30:00,20121210,1,A363,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop337,1,167293089032,1\n" );
        buffer.append("A438,testRoute,12:30:00,20121210,1,A364,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop338,1,167293089032,1\n" );
        buffer.append("A439,testRoute,13:30:00,20121210,1,A365,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop339,1,167293089032,1\n" );
        buffer.append("A440,testRoute,14:30:00,20121210,1,A366,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop340,1,167293089032,1\n" );
        buffer.append("A441,testRoute,15:30:00,20121210,1,A367,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop341,1,167293089032,1\n" );
        buffer.append("A442,testRoute,16:30:00,20121210,1,A368,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop342,1,167293089032,1\n" );
        buffer.append("A443,testRoute,17:30:00,20121210,1,A369,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop343,1,167293089032,1\n" );
        buffer.append("A444,testRoute,18:30:00,20121210,1,A370,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop344,1,167293089032,1\n" );
        buffer.append("A445,testRoute,19:30:00,20121210,1,A371,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop345,1,167293089032,1\n" );
        buffer.append("A446,testRoute,20:30:00,20121210,1,A372,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop346,1,167293089032,1\n" );
        buffer.append("A447,testRoute,21:30:00,20121210,1,A373,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop347,1,167293089032,1\n" );
        buffer.append("A448,testRoute,22:30:00,20121210,1,A374,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop348,1,167293089032,1\n" );
        buffer.append("A449,testRoute,23:30:00,20121210,1,A375,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop349,1,167293089032,1\n" );
        buffer.append("A450,testRoute,0:30:00,20121210,1,A376,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop350,1,167293089032,1\n" );
        buffer.append("A451,testRoute,1:30:00,20121210,1,A377,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop351,1,167293089032,1\n" );
        buffer.append("A452,testRoute,2:30:00,20121210,1,A378,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop352,1,167293089032,1\n" );
        buffer.append("A453,testRoute,3:30:00,20121210,1,A379,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop353,1,167293089032,1\n" );
        buffer.append("A454,testRoute,4:30:00,20121210,1,A380,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop354,1,167293089032,1\n" );
        buffer.append("A455,testRoute,5:30:00,20121210,1,A381,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop355,1,167293089032,1\n" );
        buffer.append("A456,testRoute,6:30:00,20121210,1,A382,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop356,1,167293089032,1\n" );
        buffer.append("A457,testRoute,7:30:00,20121210,1,A383,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop357,1,167293089032,1\n" );
        buffer.append("A458,testRoute,8:30:00,20121210,1,A384,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop358,1,167293089032,1\n" );
        buffer.append("A459,testRoute,9:30:00,20121210,1,A385,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop359,1,167293089032,1\n" );
        buffer.append("A460,testRoute,10:30:00,20121210,1,A386,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop360,1,167293089032,1\n" );
        buffer.append("A461,testRoute,11:30:00,20121210,1,A387,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop361,1,167293089032,1\n" );
        buffer.append("A462,testRoute,12:30:00,20121210,1,A388,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop362,1,167293089032,1\n" );
        buffer.append("A463,testRoute,13:30:00,20121210,1,A389,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop363,1,167293089032,1\n" );
        buffer.append("A464,testRoute,14:30:00,20121210,1,A390,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop364,1,167293089032,1\n" );
        buffer.append("A465,testRoute,15:30:00,20121210,1,A391,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop365,1,167293089032,1\n" );
        buffer.append("A466,testRoute,16:30:00,20121210,1,A392,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop366,1,167293089032,1\n" );
        buffer.append("A467,testRoute,17:30:00,20121210,1,A393,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop367,1,167293089032,1\n" );
        buffer.append("A468,testRoute,18:30:00,20121210,1,A394,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop368,1,167293089032,1\n" );
        buffer.append("A469,testRoute,19:30:00,20121210,1,A395,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop369,1,167293089032,1\n" );
        buffer.append("A470,testRoute,20:30:00,20121210,1,A396,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop370,1,167293089032,1\n" );
        buffer.append("A471,testRoute,21:30:00,20121210,1,A397,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop371,1,167293089032,1\n" );
        buffer.append("A472,testRoute,22:30:00,20121210,1,A398,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop372,1,167293089032,1\n" );
        buffer.append("A473,testRoute,23:30:00,20121210,1,A399,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop373,1,167293089032,1\n" );
        buffer.append("A474,testRoute,0:30:00,20121210,1,A400,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop374,1,167293089032,1\n" );
        buffer.append("A475,testRoute,1:30:00,20121210,1,A401,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop375,1,167293089032,1\n" );
        buffer.append("A476,testRoute,2:30:00,20121210,1,A402,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop376,1,167293089032,1\n" );
        buffer.append("A477,testRoute,3:30:00,20121210,1,A403,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop377,1,167293089032,1\n" );
        buffer.append("A478,testRoute,4:30:00,20121210,1,A404,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop378,1,167293089032,1\n" );
        buffer.append("A479,testRoute,5:30:00,20121210,1,A405,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop379,1,167293089032,1\n" );
        buffer.append("A480,testRoute,6:30:00,20121210,1,A406,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop380,1,167293089032,1\n" );
        buffer.append("A481,testRoute,7:30:00,20121210,1,A407,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop381,1,167293089032,1\n" );
        buffer.append("A482,testRoute,8:30:00,20121210,1,A408,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop382,1,167293089032,1\n" );
        buffer.append("A483,testRoute,9:30:00,20121210,1,A409,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop383,1,167293089032,1\n" );
        buffer.append("A484,testRoute,10:30:00,20121210,1,A410,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop384,1,167293089032,1\n" );
        buffer.append("A485,testRoute,11:30:00,20121210,1,A411,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop385,1,167293089032,1\n" );
        buffer.append("A486,testRoute,12:30:00,20121210,1,A412,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop386,1,167293089032,1\n" );
        buffer.append("A487,testRoute,13:30:00,20121210,1,A413,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop387,1,167293089032,1\n" );
        buffer.append("A488,testRoute,14:30:00,20121210,1,A414,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop388,1,167293089032,1\n" );
        buffer.append("A489,testRoute,15:30:00,20121210,1,A415,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop389,1,167293089032,1\n" );
        buffer.append("A490,testRoute,16:30:00,20121210,1,A416,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop390,1,167293089032,1\n" );
        buffer.append("A491,testRoute,17:30:00,20121210,1,A417,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop391,1,167293089032,1\n" );
        buffer.append("A492,testRoute,18:30:00,20121210,1,A418,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop392,1,167293089032,1\n" );
        buffer.append("A493,testRoute,19:30:00,20121210,1,A419,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop393,1,167293089032,1\n" );
        buffer.append("A494,testRoute,20:30:00,20121210,1,A420,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop394,1,167293089032,1\n" );
        buffer.append("A495,testRoute,21:30:00,20121210,1,A421,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop395,1,167293089032,1\n" );
        buffer.append("A496,testRoute,22:30:00,20121210,1,A422,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop396,1,167293089032,1\n" );
        buffer.append("A497,testRoute,23:30:00,20121210,1,A423,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop397,1,167293089032,1\n" );
        buffer.append("A498,testRoute,0:30:00,20121210,1,A424,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop398,1,167293089032,1\n" );
        buffer.append("A499,testRoute,1:30:00,20121210,1,A425,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop399,1,167293089032,1\n" );
        buffer.append("A500,testRoute,2:30:00,20121210,1,A426,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop400,1,167293089032,1\n" );
        buffer.append("A501,testRoute,3:30:00,20121210,1,A427,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop401,1,167293089032,1\n" );
        buffer.append("A502,testRoute,4:30:00,20121210,1,A428,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop402,1,167293089032,1\n" );
        buffer.append("A503,testRoute,5:30:00,20121210,1,A429,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop403,1,167293089032,1\n" );
        buffer.append("A504,testRoute,6:30:00,20121210,1,A430,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop404,1,167293089032,1\n" );
        buffer.append("A505,testRoute,7:30:00,20121210,1,A431,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop405,1,167293089032,1\n" );
        buffer.append("A506,testRoute,8:30:00,20121210,1,A432,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop406,1,167293089032,1\n" );
        buffer.append("A507,testRoute,9:30:00,20121210,1,A433,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop407,1,167293089032,1\n" );
        buffer.append("A508,testRoute,10:30:00,20121210,1,A434,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop408,1,167293089032,1\n" );
        buffer.append("A509,testRoute,11:30:00,20121210,1,A435,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop409,1,167293089032,1\n" );
        buffer.append("A510,testRoute,12:30:00,20121210,1,A436,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop410,1,167293089032,1\n" );
        buffer.append("A511,testRoute,13:30:00,20121210,1,A437,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop411,1,167293089032,1\n" );
        buffer.append("A512,testRoute,14:30:00,20121210,1,A438,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop412,1,167293089032,1\n" );
        buffer.append("A513,testRoute,15:30:00,20121210,1,A439,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop413,1,167293089032,1\n" );
        buffer.append("A514,testRoute,16:30:00,20121210,1,A440,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop414,1,167293089032,1\n" );
        buffer.append("A515,testRoute,17:30:00,20121210,1,A441,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop415,1,167293089032,1\n" );
        buffer.append("A516,testRoute,18:30:00,20121210,1,A442,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop416,1,167293089032,1\n" );
        buffer.append("A517,testRoute,19:30:00,20121210,1,A443,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop417,1,167293089032,1\n" );
        buffer.append("A518,testRoute,20:30:00,20121210,1,A444,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop418,1,167293089032,1\n" );
        buffer.append("A519,testRoute,21:30:00,20121210,1,A445,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop419,1,167293089032,1\n" );
        buffer.append("A520,testRoute,22:30:00,20121210,1,A446,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop420,1,167293089032,1\n" );
        buffer.append("A521,testRoute,23:30:00,20121210,1,A447,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop421,1,167293089032,1\n" );
        buffer.append("A522,testRoute,0:30:00,20121210,1,A448,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop422,1,167293089032,1\n" );
        buffer.append("A523,testRoute,1:30:00,20121210,1,A449,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop423,1,167293089032,1\n" );
        buffer.append("A524,testRoute,2:30:00,20121210,1,A450,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop424,1,167293089032,1\n" );
        buffer.append("A525,testRoute,3:30:00,20121210,1,A451,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop425,1,167293089032,1\n" );
        buffer.append("A526,testRoute,4:30:00,20121210,1,A452,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop426,1,167293089032,1\n" );
        buffer.append("A527,testRoute,5:30:00,20121210,1,A453,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop427,1,167293089032,1\n" );
        buffer.append("A528,testRoute,6:30:00,20121210,1,A454,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop428,1,167293089032,1\n" );
        buffer.append("A529,testRoute,7:30:00,20121210,1,A455,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop429,1,167293089032,1\n" );
        buffer.append("A530,testRoute,8:30:00,20121210,1,A456,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop430,1,167293089032,1\n" );
        buffer.append("A531,testRoute,9:30:00,20121210,1,A457,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop431,1,167293089032,1\n" );
        buffer.append("A532,testRoute,10:30:00,20121210,1,A458,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop432,1,167293089032,1\n" );
        buffer.append("A533,testRoute,11:30:00,20121210,1,A459,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop433,1,167293089032,1\n" );
        buffer.append("A534,testRoute,12:30:00,20121210,1,A460,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop434,1,167293089032,1\n" );
        buffer.append("A535,testRoute,13:30:00,20121210,1,A461,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop435,1,167293089032,1\n" );
        buffer.append("A536,testRoute,14:30:00,20121210,1,A462,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop436,1,167293089032,1\n" );
        buffer.append("A537,testRoute,15:30:00,20121210,1,A463,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop437,1,167293089032,1\n" );
        buffer.append("A538,testRoute,16:30:00,20121210,1,A464,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop438,1,167293089032,1\n" );
        buffer.append("A539,testRoute,17:30:00,20121210,1,A465,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop439,1,167293089032,1\n" );
        buffer.append("A540,testRoute,18:30:00,20121210,1,A466,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop440,1,167293089032,1\n" );
        buffer.append("A541,testRoute,19:30:00,20121210,1,A467,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop441,1,167293089032,1\n" );
        buffer.append("A542,testRoute,20:30:00,20121210,1,A468,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop442,1,167293089032,1\n" );
        buffer.append("A543,testRoute,21:30:00,20121210,1,A469,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop443,1,167293089032,1\n" );
        buffer.append("A544,testRoute,22:30:00,20121210,1,A470,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop444,1,167293089032,1\n" );
        buffer.append("A545,testRoute,23:30:00,20121210,1,A471,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop445,1,167293089032,1\n" );
        buffer.append("A546,testRoute,0:30:00,20121210,1,A472,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop446,1,167293089032,1\n" );
        buffer.append("A547,testRoute,1:30:00,20121210,1,A473,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop447,1,167293089032,1\n" );
        buffer.append("A548,testRoute,2:30:00,20121210,1,A474,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop448,1,167293089032,1\n" );
        buffer.append("A549,testRoute,3:30:00,20121210,1,A475,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop449,1,167293089032,1\n" );
        buffer.append("A550,testRoute,4:30:00,20121210,1,A476,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop450,1,167293089032,1\n" );
        buffer.append("A551,testRoute,5:30:00,20121210,1,A477,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop451,1,167293089032,1\n" );
        buffer.append("A552,testRoute,6:30:00,20121210,1,A478,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop452,1,167293089032,1\n" );
        buffer.append("A553,testRoute,7:30:00,20121210,1,A479,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop453,1,167293089032,1\n" );
        buffer.append("A554,testRoute,8:30:00,20121210,1,A480,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop454,1,167293089032,1\n" );
        buffer.append("A555,testRoute,9:30:00,20121210,1,A481,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop455,1,167293089032,1\n" );
        buffer.append("A556,testRoute,10:30:00,20121210,1,A482,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop456,1,167293089032,1\n" );
        buffer.append("A557,testRoute,11:30:00,20121210,1,A483,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop457,1,167293089032,1\n" );
        buffer.append("A558,testRoute,12:30:00,20121210,1,A484,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop458,1,167293089032,1\n" );
        buffer.append("A559,testRoute,13:30:00,20121210,1,A485,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop459,1,167293089032,1\n" );
        buffer.append("A560,testRoute,14:30:00,20121210,1,A486,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop460,1,167293089032,1\n" );
        buffer.append("A561,testRoute,15:30:00,20121210,1,A487,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop461,1,167293089032,1\n" );
        buffer.append("A562,testRoute,16:30:00,20121210,1,A488,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop462,1,167293089032,1\n" );
        buffer.append("A563,testRoute,17:30:00,20121210,1,A489,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop463,1,167293089032,1\n" );
        buffer.append("A564,testRoute,18:30:00,20121210,1,A490,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop464,1,167293089032,1\n" );
        buffer.append("A565,testRoute,19:30:00,20121210,1,A491,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop465,1,167293089032,1\n" );
        buffer.append("A566,testRoute,20:30:00,20121210,1,A492,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop466,1,167293089032,1\n" );
        buffer.append("A567,testRoute,21:30:00,20121210,1,A493,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop467,1,167293089032,1\n" );
        buffer.append("A568,testRoute,22:30:00,20121210,1,A494,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop468,1,167293089032,1\n" );
        buffer.append("A569,testRoute,23:30:00,20121210,1,A495,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop469,1,167293089032,1\n" );
        buffer.append("A570,testRoute,0:30:00,20121210,1,A496,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop470,1,167293089032,1\n" );
        buffer.append("A571,testRoute,1:30:00,20121210,1,A497,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop471,1,167293089032,1\n" );
        buffer.append("A572,testRoute,2:30:00,20121210,1,A498,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop472,1,167293089032,1\n" );
        buffer.append("A573,testRoute,3:30:00,20121210,1,A499,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop473,1,167293089032,1\n" );
        buffer.append("A574,testRoute,4:30:00,20121210,1,A500,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop474,1,167293089032,1\n" );
        buffer.append("A575,testRoute,5:30:00,20121210,1,A501,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop475,1,167293089032,1\n" );
        buffer.append("A576,testRoute,6:30:00,20121210,1,A502,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop476,1,167293089032,1\n" );
        buffer.append("A577,testRoute,7:30:00,20121210,1,A503,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop477,1,167293089032,1\n" );
        buffer.append("A578,testRoute,8:30:00,20121210,1,A504,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop478,1,167293089032,1\n" );
        buffer.append("A579,testRoute,9:30:00,20121210,1,A505,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop479,1,167293089032,1\n" );
        buffer.append("A580,testRoute,10:30:00,20121210,1,A506,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop480,1,167293089032,1\n" );
        buffer.append("A581,testRoute,11:30:00,20121210,1,A507,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop481,1,167293089032,1\n" );
        buffer.append("A582,testRoute,12:30:00,20121210,1,A508,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop482,1,167293089032,1\n" );
        buffer.append("A583,testRoute,13:30:00,20121210,1,A509,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop483,1,167293089032,1\n" );
        buffer.append("A584,testRoute,14:30:00,20121210,1,A510,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop484,1,167293089032,1\n" );
        buffer.append("A585,testRoute,15:30:00,20121210,1,A511,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop485,1,167293089032,1\n" );
        buffer.append("A586,testRoute,16:30:00,20121210,1,A512,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop486,1,167293089032,1\n" );
        buffer.append("A587,testRoute,17:30:00,20121210,1,A513,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop487,1,167293089032,1\n" );
        buffer.append("A588,testRoute,18:30:00,20121210,1,A514,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop488,1,167293089032,1\n" );
        buffer.append("A589,testRoute,19:30:00,20121210,1,A515,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop489,1,167293089032,1\n" );
        buffer.append("A590,testRoute,20:30:00,20121210,1,A516,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop490,1,167293089032,1\n" );
        buffer.append("A591,testRoute,21:30:00,20121210,1,A517,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop491,1,167293089032,1\n" );
        buffer.append("A592,testRoute,22:30:00,20121210,1,A518,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop492,1,167293089032,1\n" );
        buffer.append("A593,testRoute,23:30:00,20121210,1,A519,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop493,1,167293089032,1\n" );
        buffer.append("A594,testRoute,0:30:00,20121210,1,A520,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop494,1,167293089032,1\n" );
        buffer.append("A595,testRoute,1:30:00,20121210,1,A521,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop495,1,167293089032,1\n" );
        buffer.append("A596,testRoute,2:30:00,20121210,1,A522,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop496,1,167293089032,1\n" );
        buffer.append("A597,testRoute,3:30:00,20121210,1,A523,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop497,1,167293089032,1\n" );
        buffer.append("A598,testRoute,4:30:00,20121210,1,A524,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop498,1,167293089032,1\n" );
        buffer.append("A599,testRoute,5:30:00,20121210,1,A525,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop499,1,167293089032,1\n" );
        buffer.append("A600,testRoute,6:30:00,20121210,1,A526,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop500,1,167293089032,1\n" );
        buffer.append("A601,testRoute,7:30:00,20121210,1,A527,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop501,1,167293089032,1\n" );
        buffer.append("A602,testRoute,8:30:00,20121210,1,A528,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop502,1,167293089032,1\n" );
        buffer.append("A603,testRoute,9:30:00,20121210,1,A529,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop503,1,167293089032,1\n" );
        buffer.append("A604,testRoute,10:30:00,20121210,1,A530,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop504,1,167293089032,1\n" );
        buffer.append("A605,testRoute,11:30:00,20121210,1,A531,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop505,1,167293089032,1\n" );
        buffer.append("A606,testRoute,12:30:00,20121210,1,A532,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop506,1,167293089032,1\n" );
        buffer.append("A607,testRoute,13:30:00,20121210,1,A533,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop507,1,167293089032,1\n" );
        buffer.append("A608,testRoute,14:30:00,20121210,1,A534,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop508,1,167293089032,1\n" );
        buffer.append("A609,testRoute,15:30:00,20121210,1,A535,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop509,1,167293089032,1\n" );
        buffer.append("A610,testRoute,16:30:00,20121210,1,A536,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop510,1,167293089032,1\n" );
        buffer.append("A611,testRoute,17:30:00,20121210,1,A537,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop511,1,167293089032,1\n" );
        buffer.append("A612,testRoute,18:30:00,20121210,1,A538,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop512,1,167293089032,1\n" );
        buffer.append("A613,testRoute,19:30:00,20121210,1,A539,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop513,1,167293089032,1\n" );
        buffer.append("A614,testRoute,20:30:00,20121210,1,A540,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop514,1,167293089032,1\n" );
        buffer.append("A615,testRoute,21:30:00,20121210,1,A541,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop515,1,167293089032,1\n" );
        buffer.append("A616,testRoute,22:30:00,20121210,1,A542,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop516,1,167293089032,1\n" );
        buffer.append("A617,testRoute,23:30:00,20121210,1,A543,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop517,1,167293089032,1\n" );
        buffer.append("A618,testRoute,0:30:00,20121210,1,A544,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop518,1,167293089032,1\n" );
        buffer.append("A619,testRoute,1:30:00,20121210,1,A545,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop519,1,167293089032,1\n" );
        buffer.append("A620,testRoute,2:30:00,20121210,1,A546,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop520,1,167293089032,1\n" );
        buffer.append("A621,testRoute,3:30:00,20121210,1,A547,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop521,1,167293089032,1\n" );
        buffer.append("A622,testRoute,4:30:00,20121210,1,A548,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop522,1,167293089032,1\n" );
        buffer.append("A623,testRoute,5:30:00,20121210,1,A549,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop523,1,167293089032,1\n" );
        buffer.append("A624,testRoute,6:30:00,20121210,1,A550,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop524,1,167293089032,1\n" );
        buffer.append("A625,testRoute,7:30:00,20121210,1,A551,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop525,1,167293089032,1\n" );
        buffer.append("A626,testRoute,8:30:00,20121210,1,A552,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop526,1,167293089032,1\n" );
        buffer.append("A627,testRoute,9:30:00,20121210,1,A553,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop527,1,167293089032,1\n" );
        buffer.append("A628,testRoute,10:30:00,20121210,1,A554,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop528,1,167293089032,1\n" );
        buffer.append("A629,testRoute,11:30:00,20121210,1,A555,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop529,1,167293089032,1\n" );
        buffer.append("A630,testRoute,12:30:00,20121210,1,A556,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop530,1,167293089032,1\n" );
        buffer.append("A631,testRoute,13:30:00,20121210,1,A557,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop531,1,167293089032,1\n" );
        buffer.append("A632,testRoute,14:30:00,20121210,1,A558,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop532,1,167293089032,1\n" );
        buffer.append("A633,testRoute,15:30:00,20121210,1,A559,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop533,1,167293089032,1\n" );
        buffer.append("A634,testRoute,16:30:00,20121210,1,A560,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop534,1,167293089032,1\n" );
        buffer.append("A635,testRoute,17:30:00,20121210,1,A561,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop535,1,167293089032,1\n" );
        buffer.append("A636,testRoute,18:30:00,20121210,1,A562,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop536,1,167293089032,1\n" );
        buffer.append("A637,testRoute,19:30:00,20121210,1,A563,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop537,1,167293089032,1\n" );
        buffer.append("A638,testRoute,20:30:00,20121210,1,A564,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop538,1,167293089032,1\n" );
        buffer.append("A639,testRoute,21:30:00,20121210,1,A565,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop539,1,167293089032,1\n" );
        buffer.append("A640,testRoute,22:30:00,20121210,1,A566,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop540,1,167293089032,1\n" );
        buffer.append("A641,testRoute,23:30:00,20121210,1,A567,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop541,1,167293089032,1\n" );
        buffer.append("A642,testRoute,0:30:00,20121210,1,A568,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop542,1,167293089032,1\n" );
        buffer.append("A643,testRoute,1:30:00,20121210,1,A569,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop543,1,167293089032,1\n" );
        buffer.append("A644,testRoute,2:30:00,20121210,1,A570,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop544,1,167293089032,1\n" );
        buffer.append("A645,testRoute,3:30:00,20121210,1,A571,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop545,1,167293089032,1\n" );
        buffer.append("A646,testRoute,4:30:00,20121210,1,A572,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop546,1,167293089032,1\n" );
        buffer.append("A647,testRoute,5:30:00,20121210,1,A573,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop547,1,167293089032,1\n" );
        buffer.append("A648,testRoute,6:30:00,20121210,1,A574,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop548,1,167293089032,1\n" );
        buffer.append("A649,testRoute,7:30:00,20121210,1,A575,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop549,1,167293089032,1\n" );
        buffer.append("A650,testRoute,8:30:00,20121210,1,A576,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop550,1,167293089032,1\n" );
        buffer.append("A651,testRoute,9:30:00,20121210,1,A577,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop551,1,167293089032,1\n" );
        buffer.append("A652,testRoute,10:30:00,20121210,1,A578,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop552,1,167293089032,1\n" );
        buffer.append("A653,testRoute,11:30:00,20121210,1,A579,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop553,1,167293089032,1\n" );
        buffer.append("A654,testRoute,12:30:00,20121210,1,A580,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop554,1,167293089032,1\n" );
        buffer.append("A655,testRoute,13:30:00,20121210,1,A581,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop555,1,167293089032,1\n" );
        buffer.append("A656,testRoute,14:30:00,20121210,1,A582,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop556,1,167293089032,1\n" );
        buffer.append("A657,testRoute,15:30:00,20121210,1,A583,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop557,1,167293089032,1\n" );
        buffer.append("A658,testRoute,16:30:00,20121210,1,A584,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop558,1,167293089032,1\n" );
        buffer.append("A659,testRoute,17:30:00,20121210,1,A585,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop559,1,167293089032,1\n" );
        buffer.append("A660,testRoute,18:30:00,20121210,1,A586,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop560,1,167293089032,1\n" );
        buffer.append("A661,testRoute,19:30:00,20121210,1,A587,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop561,1,167293089032,1\n" );
        buffer.append("A662,testRoute,20:30:00,20121210,1,A588,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop562,1,167293089032,1\n" );
        buffer.append("A663,testRoute,21:30:00,20121210,1,A589,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop563,1,167293089032,1\n" );
        buffer.append("A664,testRoute,22:30:00,20121210,1,A590,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop564,1,167293089032,1\n" );
        buffer.append("A665,testRoute,23:30:00,20121210,1,A591,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop565,1,167293089032,1\n" );
        buffer.append("A666,testRoute,0:30:00,20121210,1,A592,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop566,1,167293089032,1\n" );
        buffer.append("A667,testRoute,1:30:00,20121210,1,A593,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop567,1,167293089032,1\n" );
        buffer.append("A668,testRoute,2:30:00,20121210,1,A594,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop568,1,167293089032,1\n" );
        buffer.append("A669,testRoute,3:30:00,20121210,1,A595,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop569,1,167293089032,1\n" );
        buffer.append("A670,testRoute,4:30:00,20121210,1,A596,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop570,1,167293089032,1\n" );
        buffer.append("A671,testRoute,5:30:00,20121210,1,A597,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop571,1,167293089032,1\n" );
        buffer.append("A672,testRoute,6:30:00,20121210,1,A598,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop572,1,167293089032,1\n" );
        buffer.append("A673,testRoute,7:30:00,20121210,1,A599,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop573,1,167293089032,1\n" );
        buffer.append("A674,testRoute,8:30:00,20121210,1,A600,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop574,1,167293089032,1\n" );
        buffer.append("A675,testRoute,9:30:00,20121210,1,A601,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop575,1,167293089032,1\n" );
        buffer.append("A676,testRoute,10:30:00,20121210,1,A602,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop576,1,167293089032,1\n" );
        buffer.append("A677,testRoute,11:30:00,20121210,1,A603,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop577,1,167293089032,1\n" );
        buffer.append("A678,testRoute,12:30:00,20121210,1,A604,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop578,1,167293089032,1\n" );
        buffer.append("A679,testRoute,13:30:00,20121210,1,A605,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop579,1,167293089032,1\n" );
        buffer.append("A680,testRoute,14:30:00,20121210,1,A606,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop580,1,167293089032,1\n" );
        buffer.append("A681,testRoute,15:30:00,20121210,1,A607,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop581,1,167293089032,1\n" );
        buffer.append("A682,testRoute,16:30:00,20121210,1,A608,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop582,1,167293089032,1\n" );
        buffer.append("A683,testRoute,17:30:00,20121210,1,A609,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop583,1,167293089032,1\n" );
        buffer.append("A684,testRoute,18:30:00,20121210,1,A610,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop584,1,167293089032,1\n" );
        buffer.append("A685,testRoute,19:30:00,20121210,1,A611,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop585,1,167293089032,1\n" );
        buffer.append("A686,testRoute,20:30:00,20121210,1,A612,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop586,1,167293089032,1\n" );
        buffer.append("A687,testRoute,21:30:00,20121210,1,A613,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop587,1,167293089032,1\n" );
        buffer.append("A688,testRoute,22:30:00,20121210,1,A614,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop588,1,167293089032,1\n" );
        buffer.append("A689,testRoute,23:30:00,20121210,1,A615,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop589,1,167293089032,1\n" );
        buffer.append("A690,testRoute,0:30:00,20121210,1,A616,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop590,1,167293089032,1\n" );
        buffer.append("A691,testRoute,1:30:00,20121210,1,A617,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop591,1,167293089032,1\n" );
        buffer.append("A692,testRoute,2:30:00,20121210,1,A618,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop592,1,167293089032,1\n" );
        buffer.append("A693,testRoute,3:30:00,20121210,1,A619,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop593,1,167293089032,1\n" );
        buffer.append("A694,testRoute,4:30:00,20121210,1,A620,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop594,1,167293089032,1\n" );
        buffer.append("A695,testRoute,5:30:00,20121210,1,A621,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop595,1,167293089032,1\n" );
        buffer.append("A696,testRoute,6:30:00,20121210,1,A622,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop596,1,167293089032,1\n" );
        buffer.append("A697,testRoute,7:30:00,20121210,1,A623,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop597,1,167293089032,1\n" );
        buffer.append("A698,testRoute,8:30:00,20121210,1,A624,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop598,1,167293089032,1\n" );
        buffer.append("A699,testRoute,9:30:00,20121210,1,A625,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop599,1,167293089032,1\n" );
        buffer.append("A700,testRoute,10:30:00,20121210,1,A626,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop600,1,167293089032,1\n" );
        buffer.append("A701,testRoute,11:30:00,20121210,1,A627,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop601,1,167293089032,1\n" );
        buffer.append("A702,testRoute,12:30:00,20121210,1,A628,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop602,1,167293089032,1\n" );
        buffer.append("A703,testRoute,13:30:00,20121210,1,A629,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop603,1,167293089032,1\n" );
        buffer.append("A704,testRoute,14:30:00,20121210,1,A630,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop604,1,167293089032,1\n" );
        buffer.append("A705,testRoute,15:30:00,20121210,1,A631,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop605,1,167293089032,1\n" );
        buffer.append("A706,testRoute,16:30:00,20121210,1,A632,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop606,1,167293089032,1\n" );
        buffer.append("A707,testRoute,17:30:00,20121210,1,A633,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop607,1,167293089032,1\n" );
        buffer.append("A708,testRoute,18:30:00,20121210,1,A634,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop608,1,167293089032,1\n" );
        buffer.append("A709,testRoute,19:30:00,20121210,1,A635,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop609,1,167293089032,1\n" );
        buffer.append("A710,testRoute,20:30:00,20121210,1,A636,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop610,1,167293089032,1\n" );
        buffer.append("A711,testRoute,21:30:00,20121210,1,A637,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop611,1,167293089032,1\n" );
        buffer.append("A712,testRoute,22:30:00,20121210,1,A638,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop612,1,167293089032,1\n" );
        buffer.append("A713,testRoute,23:30:00,20121210,1,A639,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop613,1,167293089032,1\n" );
        buffer.append("A714,testRoute,0:30:00,20121210,1,A640,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop614,1,167293089032,1\n" );
        buffer.append("A715,testRoute,1:30:00,20121210,1,A641,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop615,1,167293089032,1\n" );
        buffer.append("A716,testRoute,2:30:00,20121210,1,A642,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop616,1,167293089032,1\n" );
        buffer.append("A717,testRoute,3:30:00,20121210,1,A643,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop617,1,167293089032,1\n" );
        buffer.append("A718,testRoute,4:30:00,20121210,1,A644,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop618,1,167293089032,1\n" );
        buffer.append("A719,testRoute,5:30:00,20121210,1,A645,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop619,1,167293089032,1\n" );
        buffer.append("A720,testRoute,6:30:00,20121210,1,A646,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop620,1,167293089032,1\n" );
        buffer.append("A721,testRoute,7:30:00,20121210,1,A647,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop621,1,167293089032,1\n" );
        buffer.append("A722,testRoute,8:30:00,20121210,1,A648,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop622,1,167293089032,1\n" );
        buffer.append("A723,testRoute,9:30:00,20121210,1,A649,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop623,1,167293089032,1\n" );
        buffer.append("A724,testRoute,10:30:00,20121210,1,A650,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop624,1,167293089032,1\n" );
        buffer.append("A725,testRoute,11:30:00,20121210,1,A651,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop625,1,167293089032,1\n" );
        buffer.append("A726,testRoute,12:30:00,20121210,1,A652,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop626,1,167293089032,1\n" );
        buffer.append("A727,testRoute,13:30:00,20121210,1,A653,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop627,1,167293089032,1\n" );
        buffer.append("A728,testRoute,14:30:00,20121210,1,A654,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop628,1,167293089032,1\n" );
        buffer.append("A729,testRoute,15:30:00,20121210,1,A655,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop629,1,167293089032,1\n" );
        buffer.append("A730,testRoute,16:30:00,20121210,1,A656,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop630,1,167293089032,1\n" );
        buffer.append("A731,testRoute,17:30:00,20121210,1,A657,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop631,1,167293089032,1\n" );
        buffer.append("A732,testRoute,18:30:00,20121210,1,A658,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop632,1,167293089032,1\n" );
        buffer.append("A733,testRoute,19:30:00,20121210,1,A659,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop633,1,167293089032,1\n" );
        buffer.append("A734,testRoute,20:30:00,20121210,1,A660,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop634,1,167293089032,1\n" );
        buffer.append("A735,testRoute,21:30:00,20121210,1,A661,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop635,1,167293089032,1\n" );
        buffer.append("A736,testRoute,22:30:00,20121210,1,A662,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop636,1,167293089032,1\n" );
        buffer.append("A737,testRoute,23:30:00,20121210,1,A663,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop637,1,167293089032,1\n" );
        buffer.append("A738,testRoute,0:30:00,20121210,1,A664,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop638,1,167293089032,1\n" );
        buffer.append("A739,testRoute,1:30:00,20121210,1,A665,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop639,1,167293089032,1\n" );
        buffer.append("A740,testRoute,2:30:00,20121210,1,A666,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop640,1,167293089032,1\n" );
        buffer.append("A741,testRoute,3:30:00,20121210,1,A667,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop641,1,167293089032,1\n" );
        buffer.append("A742,testRoute,4:30:00,20121210,1,A668,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop642,1,167293089032,1\n" );
        buffer.append("A743,testRoute,5:30:00,20121210,1,A669,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop643,1,167293089032,1\n" );
        buffer.append("A744,testRoute,6:30:00,20121210,1,A670,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop644,1,167293089032,1\n" );
        buffer.append("A745,testRoute,7:30:00,20121210,1,A671,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop645,1,167293089032,1\n" );
        buffer.append("A746,testRoute,8:30:00,20121210,1,A672,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop646,1,167293089032,1\n" );
        buffer.append("A747,testRoute,9:30:00,20121210,1,A673,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop647,1,167293089032,1\n" );
        buffer.append("A748,testRoute,10:30:00,20121210,1,A674,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop648,1,167293089032,1\n" );
        buffer.append("A749,testRoute,11:30:00,20121210,1,A675,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop649,1,167293089032,1\n" );
        buffer.append("A750,testRoute,12:30:00,20121210,1,A676,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop650,1,167293089032,1\n" );
        buffer.append("A751,testRoute,13:30:00,20121210,1,A677,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop651,1,167293089032,1\n" );
        buffer.append("A752,testRoute,14:30:00,20121210,1,A678,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop652,1,167293089032,1\n" );
        buffer.append("A753,testRoute,15:30:00,20121210,1,A679,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop653,1,167293089032,1\n" );
        buffer.append("A754,testRoute,16:30:00,20121210,1,A680,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop654,1,167293089032,1\n" );
        buffer.append("A755,testRoute,17:30:00,20121210,1,A681,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop655,1,167293089032,1\n" );
        buffer.append("A756,testRoute,18:30:00,20121210,1,A682,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop656,1,167293089032,1\n" );
        buffer.append("A757,testRoute,19:30:00,20121210,1,A683,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop657,1,167293089032,1\n" );
        buffer.append("A758,testRoute,20:30:00,20121210,1,A684,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop658,1,167293089032,1\n" );
        buffer.append("A759,testRoute,21:30:00,20121210,1,A685,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop659,1,167293089032,1\n" );
        buffer.append("A760,testRoute,22:30:00,20121210,1,A686,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop660,1,167293089032,1\n" );
        buffer.append("A761,testRoute,23:30:00,20121210,1,A687,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop661,1,167293089032,1\n" );
        buffer.append("A762,testRoute,0:30:00,20121210,1,A688,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop662,1,167293089032,1\n" );
        buffer.append("A763,testRoute,1:30:00,20121210,1,A689,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop663,1,167293089032,1\n" );
        buffer.append("A764,testRoute,2:30:00,20121210,1,A690,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop664,1,167293089032,1\n" );
        buffer.append("A765,testRoute,3:30:00,20121210,1,A691,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop665,1,167293089032,1\n" );
        buffer.append("A766,testRoute,4:30:00,20121210,1,A692,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop666,1,167293089032,1\n" );
        buffer.append("A767,testRoute,5:30:00,20121210,1,A693,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop667,1,167293089032,1\n" );
        buffer.append("A768,testRoute,6:30:00,20121210,1,A694,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop668,1,167293089032,1\n" );
        buffer.append("A769,testRoute,7:30:00,20121210,1,A695,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop669,1,167293089032,1\n" );
        buffer.append("A770,testRoute,8:30:00,20121210,1,A696,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop670,1,167293089032,1\n" );
        buffer.append("A771,testRoute,9:30:00,20121210,1,A697,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop671,1,167293089032,1\n" );
        buffer.append("A772,testRoute,10:30:00,20121210,1,A698,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop672,1,167293089032,1\n" );
        buffer.append("A773,testRoute,11:30:00,20121210,1,A699,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop673,1,167293089032,1\n" );
        buffer.append("A774,testRoute,12:30:00,20121210,1,A700,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop674,1,167293089032,1\n" );
        buffer.append("A775,testRoute,13:30:00,20121210,1,A701,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop675,1,167293089032,1\n" );
        buffer.append("A776,testRoute,14:30:00,20121210,1,A702,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop676,1,167293089032,1\n" );
        buffer.append("A777,testRoute,15:30:00,20121210,1,A703,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop677,1,167293089032,1\n" );
        buffer.append("A778,testRoute,16:30:00,20121210,1,A704,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop678,1,167293089032,1\n" );
        buffer.append("A779,testRoute,17:30:00,20121210,1,A705,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop679,1,167293089032,1\n" );
        buffer.append("A780,testRoute,18:30:00,20121210,1,A706,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop680,1,167293089032,1\n" );
        buffer.append("A781,testRoute,19:30:00,20121210,1,A707,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop681,1,167293089032,1\n" );
        buffer.append("A782,testRoute,20:30:00,20121210,1,A708,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop682,1,167293089032,1\n" );
        buffer.append("A783,testRoute,21:30:00,20121210,1,A709,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop683,1,167293089032,1\n" );
        buffer.append("A784,testRoute,22:30:00,20121210,1,A710,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop684,1,167293089032,1\n" );
        buffer.append("A785,testRoute,23:30:00,20121210,1,A711,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop685,1,167293089032,1\n" );
        buffer.append("A786,testRoute,0:30:00,20121210,1,A712,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop686,1,167293089032,1\n" );
        buffer.append("A787,testRoute,1:30:00,20121210,1,A713,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop687,1,167293089032,1\n" );
        buffer.append("A788,testRoute,2:30:00,20121210,1,A714,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop688,1,167293089032,1\n" );
        buffer.append("A789,testRoute,3:30:00,20121210,1,A715,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop689,1,167293089032,1\n" );
        buffer.append("A790,testRoute,4:30:00,20121210,1,A716,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop690,1,167293089032,1\n" );
        buffer.append("A791,testRoute,5:30:00,20121210,1,A717,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop691,1,167293089032,1\n" );
        buffer.append("A792,testRoute,6:30:00,20121210,1,A718,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop692,1,167293089032,1\n" );
        buffer.append("A793,testRoute,7:30:00,20121210,1,A719,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop693,1,167293089032,1\n" );
        buffer.append("A794,testRoute,8:30:00,20121210,1,A720,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop694,1,167293089032,1\n" );
        buffer.append("A795,testRoute,9:30:00,20121210,1,A721,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop695,1,167293089032,1\n" );
        buffer.append("A796,testRoute,10:30:00,20121210,1,A722,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop696,1,167293089032,1\n" );
        buffer.append("A797,testRoute,11:30:00,20121210,1,A723,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop697,1,167293089032,1\n" );
        buffer.append("A798,testRoute,12:30:00,20121210,1,A724,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop698,1,167293089032,1\n" );
        buffer.append("A799,testRoute,13:30:00,20121210,1,A725,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop699,1,167293089032,1\n" );
        buffer.append("A800,testRoute,14:30:00,20121210,1,A726,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop700,1,167293089032,1\n" );
        buffer.append("A801,testRoute,15:30:00,20121210,1,A727,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop701,1,167293089032,1\n" );
        buffer.append("A802,testRoute,16:30:00,20121210,1,A728,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop702,1,167293089032,1\n" );
        buffer.append("A803,testRoute,17:30:00,20121210,1,A729,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop703,1,167293089032,1\n" );
        buffer.append("A804,testRoute,18:30:00,20121210,1,A730,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop704,1,167293089032,1\n" );
        buffer.append("A805,testRoute,19:30:00,20121210,1,A731,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop705,1,167293089032,1\n" );
        buffer.append("A806,testRoute,20:30:00,20121210,1,A732,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop706,1,167293089032,1\n" );
        buffer.append("A807,testRoute,21:30:00,20121210,1,A733,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop707,1,167293089032,1\n" );
        buffer.append("A808,testRoute,22:30:00,20121210,1,A734,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop708,1,167293089032,1\n" );
        buffer.append("A809,testRoute,23:30:00,20121210,1,A735,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop709,1,167293089032,1\n" );
        buffer.append("A810,testRoute,0:30:00,20121210,1,A736,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop710,1,167293089032,1\n" );
        buffer.append("A811,testRoute,1:30:00,20121210,1,A737,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop711,1,167293089032,1\n" );
        buffer.append("A812,testRoute,2:30:00,20121210,1,A738,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop712,1,167293089032,1\n" );
        buffer.append("A813,testRoute,3:30:00,20121210,1,A739,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop713,1,167293089032,1\n" );
        buffer.append("A814,testRoute,4:30:00,20121210,1,A740,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop714,1,167293089032,1\n" );
        buffer.append("A815,testRoute,5:30:00,20121210,1,A741,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop715,1,167293089032,1\n" );
        buffer.append("A816,testRoute,6:30:00,20121210,1,A742,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop716,1,167293089032,1\n" );
        buffer.append("A817,testRoute,7:30:00,20121210,1,A743,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop717,1,167293089032,1\n" );
        buffer.append("A818,testRoute,8:30:00,20121210,1,A744,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop718,1,167293089032,1\n" );
        buffer.append("A819,testRoute,9:30:00,20121210,1,A745,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop719,1,167293089032,1\n" );
        buffer.append("A820,testRoute,10:30:00,20121210,1,A746,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop720,1,167293089032,1\n" );
        buffer.append("A821,testRoute,11:30:00,20121210,1,A747,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop721,1,167293089032,1\n" );
        buffer.append("A822,testRoute,12:30:00,20121210,1,A748,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop722,1,167293089032,1\n" );
        buffer.append("A823,testRoute,13:30:00,20121210,1,A749,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop723,1,167293089032,1\n" );
        buffer.append("A824,testRoute,14:30:00,20121210,1,A750,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop724,1,167293089032,1\n" );
        buffer.append("A825,testRoute,15:30:00,20121210,1,A751,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop725,1,167293089032,1\n" );
        buffer.append("A826,testRoute,16:30:00,20121210,1,A752,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop726,1,167293089032,1\n" );
        buffer.append("A827,testRoute,17:30:00,20121210,1,A753,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop727,1,167293089032,1\n" );
        buffer.append("A828,testRoute,18:30:00,20121210,1,A754,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop728,1,167293089032,1\n" );
        buffer.append("A829,testRoute,19:30:00,20121210,1,A755,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop729,1,167293089032,1\n" );
        buffer.append("A830,testRoute,20:30:00,20121210,1,A756,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop730,1,167293089032,1\n" );
        buffer.append("A831,testRoute,21:30:00,20121210,1,A757,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop731,1,167293089032,1\n" );
        buffer.append("A832,testRoute,22:30:00,20121210,1,A758,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop732,1,167293089032,1\n" );
        buffer.append("A833,testRoute,23:30:00,20121210,1,A759,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop733,1,167293089032,1\n" );
        buffer.append("A834,testRoute,0:30:00,20121210,1,A760,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop734,1,167293089032,1\n" );
        buffer.append("A835,testRoute,1:30:00,20121210,1,A761,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop735,1,167293089032,1\n" );
        buffer.append("A836,testRoute,2:30:00,20121210,1,A762,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop736,1,167293089032,1\n" );
        buffer.append("A837,testRoute,3:30:00,20121210,1,A763,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop737,1,167293089032,1\n" );
        buffer.append("A838,testRoute,4:30:00,20121210,1,A764,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop738,1,167293089032,1\n" );
        buffer.append("A839,testRoute,5:30:00,20121210,1,A765,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop739,1,167293089032,1\n" );
        buffer.append("A840,testRoute,6:30:00,20121210,1,A766,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop740,1,167293089032,1\n" );
        buffer.append("A841,testRoute,7:30:00,20121210,1,A767,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop741,1,167293089032,1\n" );
        buffer.append("A842,testRoute,8:30:00,20121210,1,A768,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop742,1,167293089032,1\n" );
        buffer.append("A843,testRoute,9:30:00,20121210,1,A769,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop743,1,167293089032,1\n" );
        buffer.append("A844,testRoute,10:30:00,20121210,1,A770,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop744,1,167293089032,1\n" );
        buffer.append("A845,testRoute,11:30:00,20121210,1,A771,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop745,1,167293089032,1\n" );
        buffer.append("A846,testRoute,12:30:00,20121210,1,A772,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop746,1,167293089032,1\n" );
        buffer.append("A847,testRoute,13:30:00,20121210,1,A773,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop747,1,167293089032,1\n" );
        buffer.append("A848,testRoute,14:30:00,20121210,1,A774,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop748,1,167293089032,1\n" );
        buffer.append("A849,testRoute,15:30:00,20121210,1,A775,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop749,1,167293089032,1\n" );
        buffer.append("A850,testRoute,16:30:00,20121210,1,A776,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop750,1,167293089032,1\n" );
        buffer.append("A851,testRoute,17:30:00,20121210,1,A777,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop751,1,167293089032,1\n" );
        buffer.append("A852,testRoute,18:30:00,20121210,1,A778,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop752,1,167293089032,1\n" );
        buffer.append("A853,testRoute,19:30:00,20121210,1,A779,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop753,1,167293089032,1\n" );
        buffer.append("A854,testRoute,20:30:00,20121210,1,A780,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop754,1,167293089032,1\n" );
        buffer.append("A855,testRoute,21:30:00,20121210,1,A781,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop755,1,167293089032,1\n" );
        buffer.append("A856,testRoute,22:30:00,20121210,1,A782,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop756,1,167293089032,1\n" );
        buffer.append("A857,testRoute,23:30:00,20121210,1,A783,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop757,1,167293089032,1\n" );
        buffer.append("A858,testRoute,0:30:00,20121210,1,A784,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop758,1,167293089032,1\n" );
        buffer.append("A859,testRoute,1:30:00,20121210,1,A785,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop759,1,167293089032,1\n" );
        buffer.append("A860,testRoute,2:30:00,20121210,1,A786,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop760,1,167293089032,1\n" );
        buffer.append("A861,testRoute,3:30:00,20121210,1,A787,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop761,1,167293089032,1\n" );
        buffer.append("A862,testRoute,4:30:00,20121210,1,A788,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop762,1,167293089032,1\n" );
        buffer.append("A863,testRoute,5:30:00,20121210,1,A789,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop763,1,167293089032,1\n" );
        buffer.append("A864,testRoute,6:30:00,20121210,1,A790,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop764,1,167293089032,1\n" );
        buffer.append("A865,testRoute,7:30:00,20121210,1,A791,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop765,1,167293089032,1\n" );
        buffer.append("A866,testRoute,8:30:00,20121210,1,A792,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop766,1,167293089032,1\n" );
        buffer.append("A867,testRoute,9:30:00,20121210,1,A793,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop767,1,167293089032,1\n" );
        buffer.append("A868,testRoute,10:30:00,20121210,1,A794,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop768,1,167293089032,1\n" );
        buffer.append("A869,testRoute,11:30:00,20121210,1,A795,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop769,1,167293089032,1\n" );
        buffer.append("A870,testRoute,12:30:00,20121210,1,A796,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop770,1,167293089032,1\n" );
        buffer.append("A871,testRoute,13:30:00,20121210,1,A797,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop771,1,167293089032,1\n" );
        buffer.append("A872,testRoute,14:30:00,20121210,1,A798,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop772,1,167293089032,1\n" );
        buffer.append("A873,testRoute,15:30:00,20121210,1,A799,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop773,1,167293089032,1\n" );
        buffer.append("A874,testRoute,16:30:00,20121210,1,A800,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop774,1,167293089032,1\n" );
        buffer.append("A875,testRoute,17:30:00,20121210,1,A801,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop775,1,167293089032,1\n" );
        buffer.append("A876,testRoute,18:30:00,20121210,1,A802,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop776,1,167293089032,1\n" );
        buffer.append("A877,testRoute,19:30:00,20121210,1,A803,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop777,1,167293089032,1\n" );
        buffer.append("A878,testRoute,20:30:00,20121210,1,A804,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop778,1,167293089032,1\n" );
        buffer.append("A879,testRoute,21:30:00,20121210,1,A805,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop779,1,167293089032,1\n" );
        buffer.append("A880,testRoute,22:30:00,20121210,1,A806,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop780,1,167293089032,1\n" );
        buffer.append("A881,testRoute,23:30:00,20121210,1,A807,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop781,1,167293089032,1\n" );
        buffer.append("A882,testRoute,0:30:00,20121210,1,A808,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop782,1,167293089032,1\n" );
        buffer.append("A883,testRoute,1:30:00,20121210,1,A809,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop783,1,167293089032,1\n" );
        buffer.append("A884,testRoute,2:30:00,20121210,1,A810,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop784,1,167293089032,1\n" );
        buffer.append("A885,testRoute,3:30:00,20121210,1,A811,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop785,1,167293089032,1\n" );
        buffer.append("A886,testRoute,4:30:00,20121210,1,A812,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop786,1,167293089032,1\n" );
        buffer.append("A887,testRoute,5:30:00,20121210,1,A813,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop787,1,167293089032,1\n" );
        buffer.append("A888,testRoute,6:30:00,20121210,1,A814,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop788,1,167293089032,1\n" );
        buffer.append("A889,testRoute,7:30:00,20121210,1,A815,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop789,1,167293089032,1\n" );
        buffer.append("A890,testRoute,8:30:00,20121210,1,A816,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop790,1,167293089032,1\n" );
        buffer.append("A891,testRoute,9:30:00,20121210,1,A817,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop791,1,167293089032,1\n" );
        buffer.append("A892,testRoute,10:30:00,20121210,1,A818,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop792,1,167293089032,1\n" );
        buffer.append("A893,testRoute,11:30:00,20121210,1,A819,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop793,1,167293089032,1\n" );
        buffer.append("A894,testRoute,12:30:00,20121210,1,A820,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop794,1,167293089032,1\n" );
        buffer.append("A895,testRoute,13:30:00,20121210,1,A821,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop795,1,167293089032,1\n" );
        buffer.append("A896,testRoute,14:30:00,20121210,1,A822,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop796,1,167293089032,1\n" );
        buffer.append("A897,testRoute,15:30:00,20121210,1,A823,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop797,1,167293089032,1\n" );
        buffer.append("A898,testRoute,16:30:00,20121210,1,A824,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop798,1,167293089032,1\n" );
        buffer.append("A899,testRoute,17:30:00,20121210,1,A825,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop799,1,167293089032,1\n" );
        buffer.append("A900,testRoute,18:30:00,20121210,1,A826,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop800,1,167293089032,1\n" );
        buffer.append("A901,testRoute,19:30:00,20121210,1,A827,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop801,1,167293089032,1\n" );
        buffer.append("A902,testRoute,20:30:00,20121210,1,A828,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop802,1,167293089032,1\n" );
        buffer.append("A903,testRoute,21:30:00,20121210,1,A829,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop803,1,167293089032,1\n" );
        buffer.append("A904,testRoute,22:30:00,20121210,1,A830,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop804,1,167293089032,1\n" );
        buffer.append("A905,testRoute,23:30:00,20121210,1,A831,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop805,1,167293089032,1\n" );
        buffer.append("A906,testRoute,0:30:00,20121210,1,A832,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop806,1,167293089032,1\n" );
        buffer.append("A907,testRoute,1:30:00,20121210,1,A833,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop807,1,167293089032,1\n" );
        buffer.append("A908,testRoute,2:30:00,20121210,1,A834,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop808,1,167293089032,1\n" );
        buffer.append("A909,testRoute,3:30:00,20121210,1,A835,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop809,1,167293089032,1\n" );
        buffer.append("A910,testRoute,4:30:00,20121210,1,A836,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop810,1,167293089032,1\n" );
        buffer.append("A911,testRoute,5:30:00,20121210,1,A837,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop811,1,167293089032,1\n" );
        buffer.append("A912,testRoute,6:30:00,20121210,1,A838,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop812,1,167293089032,1\n" );
        buffer.append("A913,testRoute,7:30:00,20121210,1,A839,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop813,1,167293089032,1\n" );
        buffer.append("A914,testRoute,8:30:00,20121210,1,A840,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop814,1,167293089032,1\n" );
        buffer.append("A915,testRoute,9:30:00,20121210,1,A841,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop815,1,167293089032,1\n" );
        buffer.append("A916,testRoute,10:30:00,20121210,1,A842,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop816,1,167293089032,1\n" );
        buffer.append("A917,testRoute,11:30:00,20121210,1,A843,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop817,1,167293089032,1\n" );
        buffer.append("A918,testRoute,12:30:00,20121210,1,A844,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop818,1,167293089032,1\n" );
        buffer.append("A919,testRoute,13:30:00,20121210,1,A845,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop819,1,167293089032,1\n" );
        buffer.append("A920,testRoute,14:30:00,20121210,1,A846,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop820,1,167293089032,1\n" );
        buffer.append("A921,testRoute,15:30:00,20121210,1,A847,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop821,1,167293089032,1\n" );
        buffer.append("A922,testRoute,16:30:00,20121210,1,A848,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop822,1,167293089032,1\n" );
        buffer.append("A923,testRoute,17:30:00,20121210,1,A849,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop823,1,167293089032,1\n" );
        buffer.append("A924,testRoute,18:30:00,20121210,1,A850,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop824,1,167293089032,1\n" );
        buffer.append("A925,testRoute,19:30:00,20121210,1,A851,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop825,1,167293089032,1\n" );
        buffer.append("A926,testRoute,20:30:00,20121210,1,A852,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop826,1,167293089032,1\n" );
        buffer.append("A927,testRoute,21:30:00,20121210,1,A853,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop827,1,167293089032,1\n" );
        buffer.append("A928,testRoute,22:30:00,20121210,1,A854,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop828,1,167293089032,1\n" );
        buffer.append("A929,testRoute,23:30:00,20121210,1,A855,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop829,1,167293089032,1\n" );
        buffer.append("A930,testRoute,0:30:00,20121210,1,A856,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop830,1,167293089032,1\n" );
        buffer.append("A931,testRoute,1:30:00,20121210,1,A857,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop831,1,167293089032,1\n" );
        buffer.append("A932,testRoute,2:30:00,20121210,1,A858,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop832,1,167293089032,1\n" );
        buffer.append("A933,testRoute,3:30:00,20121210,1,A859,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop833,1,167293089032,1\n" );
        buffer.append("A934,testRoute,4:30:00,20121210,1,A860,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop834,1,167293089032,1\n" );
        buffer.append("A935,testRoute,5:30:00,20121210,1,A861,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop835,1,167293089032,1\n" );
        buffer.append("A936,testRoute,6:30:00,20121210,1,A862,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop836,1,167293089032,1\n" );
        buffer.append("A937,testRoute,7:30:00,20121210,1,A863,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop837,1,167293089032,1\n" );
        buffer.append("A938,testRoute,8:30:00,20121210,1,A864,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop838,1,167293089032,1\n" );
        buffer.append("A939,testRoute,9:30:00,20121210,1,A865,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop839,1,167293089032,1\n" );
        buffer.append("A940,testRoute,10:30:00,20121210,1,A866,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop840,1,167293089032,1\n" );
        buffer.append("A941,testRoute,11:30:00,20121210,1,A867,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop841,1,167293089032,1\n" );
        buffer.append("A942,testRoute,12:30:00,20121210,1,A868,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop842,1,167293089032,1\n" );
        buffer.append("A943,testRoute,13:30:00,20121210,1,A869,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop843,1,167293089032,1\n" );
        buffer.append("A944,testRoute,14:30:00,20121210,1,A870,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop844,1,167293089032,1\n" );
        buffer.append("A945,testRoute,15:30:00,20121210,1,A871,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop845,1,167293089032,1\n" );
        buffer.append("A946,testRoute,16:30:00,20121210,1,A872,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop846,1,167293089032,1\n" );
        buffer.append("A947,testRoute,17:30:00,20121210,1,A873,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop847,1,167293089032,1\n" );
        buffer.append("A948,testRoute,18:30:00,20121210,1,A874,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop848,1,167293089032,1\n" );
        buffer.append("A949,testRoute,19:30:00,20121210,1,A875,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop849,1,167293089032,1\n" );
        buffer.append("A950,testRoute,20:30:00,20121210,1,A876,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop850,1,167293089032,1\n" );
        buffer.append("A951,testRoute,21:30:00,20121210,1,A877,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop851,1,167293089032,1\n" );
        buffer.append("A952,testRoute,22:30:00,20121210,1,A878,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop852,1,167293089032,1\n" );
        buffer.append("A953,testRoute,23:30:00,20121210,1,A879,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop853,1,167293089032,1\n" );
        buffer.append("A954,testRoute,0:30:00,20121210,1,A880,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop854,1,167293089032,1\n" );
        buffer.append("A955,testRoute,1:30:00,20121210,1,A881,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop855,1,167293089032,1\n" );
        buffer.append("A956,testRoute,2:30:00,20121210,1,A882,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop856,1,167293089032,1\n" );
        buffer.append("A957,testRoute,3:30:00,20121210,1,A883,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop857,1,167293089032,1\n" );
        buffer.append("A958,testRoute,4:30:00,20121210,1,A884,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop858,1,167293089032,1\n" );
        buffer.append("A959,testRoute,5:30:00,20121210,1,A885,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop859,1,167293089032,1\n" );
        buffer.append("A960,testRoute,6:30:00,20121210,1,A886,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop860,1,167293089032,1\n" );
        buffer.append("A961,testRoute,7:30:00,20121210,1,A887,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop861,1,167293089032,1\n" );
        buffer.append("A962,testRoute,8:30:00,20121210,1,A888,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop862,1,167293089032,1\n" );
        buffer.append("A963,testRoute,9:30:00,20121210,1,A889,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop863,1,167293089032,1\n" );
        buffer.append("A964,testRoute,10:30:00,20121210,1,A890,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop864,1,167293089032,1\n" );
        buffer.append("A965,testRoute,11:30:00,20121210,1,A891,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop865,1,167293089032,1\n" );
        buffer.append("A966,testRoute,12:30:00,20121210,1,A892,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop866,1,167293089032,1\n" );
        buffer.append("A967,testRoute,13:30:00,20121210,1,A893,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop867,1,167293089032,1\n" );
        buffer.append("A968,testRoute,14:30:00,20121210,1,A894,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop868,1,167293089032,1\n" );
        buffer.append("A969,testRoute,15:30:00,20121210,1,A895,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop869,1,167293089032,1\n" );
        buffer.append("A970,testRoute,16:30:00,20121210,1,A896,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop870,1,167293089032,1\n" );
        buffer.append("A971,testRoute,17:30:00,20121210,1,A897,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop871,1,167293089032,1\n" );
        buffer.append("A972,testRoute,18:30:00,20121210,1,A898,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop872,1,167293089032,1\n" );
        buffer.append("A973,testRoute,19:30:00,20121210,1,A899,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop873,1,167293089032,1\n" );
        buffer.append("A974,testRoute,20:30:00,20121210,1,A900,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop874,1,167293089032,1\n" );
        buffer.append("A975,testRoute,21:30:00,20121210,1,A901,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop875,1,167293089032,1\n" );
        buffer.append("A976,testRoute,22:30:00,20121210,1,A902,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop876,1,167293089032,1\n" );
        buffer.append("A977,testRoute,23:30:00,20121210,1,A903,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop877,1,167293089032,1\n" );
        buffer.append("A978,testRoute,0:30:00,20121210,1,A904,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop878,1,167293089032,1\n" );
        buffer.append("A979,testRoute,1:30:00,20121210,1,A905,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop879,1,167293089032,1\n" );
        buffer.append("A980,testRoute,2:30:00,20121210,1,A906,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop880,1,167293089032,1\n" );
        buffer.append("A981,testRoute,3:30:00,20121210,1,A907,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop881,1,167293089032,1\n" );
        buffer.append("A982,testRoute,4:30:00,20121210,1,A908,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop882,1,167293089032,1\n" );
        buffer.append("A983,testRoute,5:30:00,20121210,1,A909,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop883,1,167293089032,1\n" );
        buffer.append("A984,testRoute,6:30:00,20121210,1,A910,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop884,1,167293089032,1\n" );
        buffer.append("A985,testRoute,7:30:00,20121210,1,A911,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop885,1,167293089032,1\n" );
        buffer.append("A986,testRoute,8:30:00,20121210,1,A912,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop886,1,167293089032,1\n" );
        buffer.append("A987,testRoute,9:30:00,20121210,1,A913,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop887,1,167293089032,1\n" );
        buffer.append("A988,testRoute,10:30:00,20121210,1,A914,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop888,1,167293089032,1\n" );
        buffer.append("A989,testRoute,11:30:00,20121210,1,A915,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop889,1,167293089032,1\n" );
        buffer.append("A990,testRoute,12:30:00,20121210,1,A916,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop890,1,167293089032,1\n" );
        buffer.append("A991,testRoute,13:30:00,20121210,1,A917,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop891,1,167293089032,1\n" );
        buffer.append("A992,testRoute,14:30:00,20121210,1,A918,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop892,1,167293089032,1\n" );
        buffer.append("A993,testRoute,15:30:00,20121210,1,A919,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop893,1,167293089032,1\n" );
        buffer.append("A994,testRoute,16:30:00,20121210,1,A920,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop894,1,167293089032,1\n" );
        buffer.append("A995,testRoute,17:30:00,20121210,1,A921,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop895,1,167293089032,1\n" );
        buffer.append("A996,testRoute,18:30:00,20121210,1,A922,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop896,1,167293089032,1\n" );
        buffer.append("A997,testRoute,19:30:00,20121210,1,A923,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop897,1,167293089032,1\n" );
        buffer.append("A998,testRoute,20:30:00,20121210,1,A924,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop898,1,167293089032,1\n" );
        buffer.append("A999,testRoute,21:30:00,20121210,1,A925,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop899,1,167293089032,1\n" );
        buffer.append("A1000,testRoute,22:30:00,20121210,1,A926,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop900,1,167293089032,1\n" );
        buffer.append("A1001,testRoute,23:30:00,20121210,1,A927,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop901,1,167293089032,1\n" );
        buffer.append("A1002,testRoute,0:30:00,20121210,1,A928,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop902,1,167293089032,1\n" );
        buffer.append("A1003,testRoute,1:30:00,20121210,1,A929,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop903,1,167293089032,1\n" );
        buffer.append("A1004,testRoute,2:30:00,20121210,1,A930,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop904,1,167293089032,1\n" );
        buffer.append("A1005,testRoute,3:30:00,20121210,1,A931,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop905,1,167293089032,1\n" );
        buffer.append("A1006,testRoute,4:30:00,20121210,1,A932,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop906,1,167293089032,1\n" );
        buffer.append("A1007,testRoute,5:30:00,20121210,1,A933,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop907,1,167293089032,1\n" );
        buffer.append("A1008,testRoute,6:30:00,20121210,1,A934,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop908,1,167293089032,1\n" );
        buffer.append("A1009,testRoute,7:30:00,20121210,1,A935,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop909,1,167293089032,1\n" );
        buffer.append("A1010,testRoute,8:30:00,20121210,1,A936,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop910,1,167293089032,1\n" );
        buffer.append("A1011,testRoute,9:30:00,20121210,1,A937,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop911,1,167293089032,1\n" );
        buffer.append("A1012,testRoute,10:30:00,20121210,1,A938,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop912,1,167293089032,1\n" );
        buffer.append("A1013,testRoute,11:30:00,20121210,1,A939,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop913,1,167293089032,1\n" );
        buffer.append("A1014,testRoute,12:30:00,20121210,1,A940,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop914,1,167293089032,1\n" );
        buffer.append("A1015,testRoute,13:30:00,20121210,1,A941,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop915,1,167293089032,1\n" );
        buffer.append("A1016,testRoute,14:30:00,20121210,1,A942,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop916,1,167293089032,1\n" );
        buffer.append("A1017,testRoute,15:30:00,20121210,1,A943,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop917,1,167293089032,1\n" );
        buffer.append("A1018,testRoute,16:30:00,20121210,1,A944,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop918,1,167293089032,1\n" );
        buffer.append("A1019,testRoute,17:30:00,20121210,1,A945,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop919,1,167293089032,1\n" );
        buffer.append("A1020,testRoute,18:30:00,20121210,1,A946,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop920,1,167293089032,1\n" );
        buffer.append("A1021,testRoute,19:30:00,20121210,1,A947,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop921,1,167293089032,1\n" );
        buffer.append("A1022,testRoute,20:30:00,20121210,1,A948,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop922,1,167293089032,1\n" );
        buffer.append("A1023,testRoute,21:30:00,20121210,1,A949,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop923,1,167293089032,1\n" );
        buffer.append("A1024,testRoute,22:30:00,20121210,1,A950,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop924,1,167293089032,1\n" );
        buffer.append("A1025,testRoute,23:30:00,20121210,1,A951,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop925,1,167293089032,1\n" );
        buffer.append("A1026,testRoute,0:30:00,20121210,1,A952,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop926,1,167293089032,1\n" );
        buffer.append("A1027,testRoute,1:30:00,20121210,1,A953,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop927,1,167293089032,1\n" );
        buffer.append("A1028,testRoute,2:30:00,20121210,1,A954,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop928,1,167293089032,1\n" );
        buffer.append("A1029,testRoute,3:30:00,20121210,1,A955,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop929,1,167293089032,1\n" );
        buffer.append("A1030,testRoute,4:30:00,20121210,1,A956,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop930,1,167293089032,1\n" );
        buffer.append("A1031,testRoute,5:30:00,20121210,1,A957,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop931,1,167293089032,1\n" );
        buffer.append("A1032,testRoute,6:30:00,20121210,1,A958,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop932,1,167293089032,1\n" );
        buffer.append("A1033,testRoute,7:30:00,20121210,1,A959,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop933,1,167293089032,1\n" );
        buffer.append("A1034,testRoute,8:30:00,20121210,1,A960,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop934,1,167293089032,1\n" );
        buffer.append("A1035,testRoute,9:30:00,20121210,1,A961,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop935,1,167293089032,1\n" );
        buffer.append("A1036,testRoute,10:30:00,20121210,1,A962,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop936,1,167293089032,1\n" );
        buffer.append("A1037,testRoute,11:30:00,20121210,1,A963,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop937,1,167293089032,1\n" );
        buffer.append("A1038,testRoute,12:30:00,20121210,1,A964,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop938,1,167293089032,1\n" );
        buffer.append("A1039,testRoute,13:30:00,20121210,1,A965,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop939,1,167293089032,1\n" );
        buffer.append("A1040,testRoute,14:30:00,20121210,1,A966,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop940,1,167293089032,1\n" );
        buffer.append("A1041,testRoute,15:30:00,20121210,1,A967,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop941,1,167293089032,1\n" );
        buffer.append("A1042,testRoute,16:30:00,20121210,1,A968,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop942,1,167293089032,1\n" );
        buffer.append("A1043,testRoute,17:30:00,20121210,1,A969,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop943,1,167293089032,1\n" );
        buffer.append("A1044,testRoute,18:30:00,20121210,1,A970,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop944,1,167293089032,1\n" );
        buffer.append("A1045,testRoute,19:30:00,20121210,1,A971,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop945,1,167293089032,1\n" );
        buffer.append("A1046,testRoute,20:30:00,20121210,1,A972,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop946,1,167293089032,1\n" );
        buffer.append("A1047,testRoute,21:30:00,20121210,1,A973,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop947,1,167293089032,1\n" );
        buffer.append("A1048,testRoute,22:30:00,20121210,1,A974,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop948,1,167293089032,1\n" );
        buffer.append("A1049,testRoute,23:30:00,20121210,1,A975,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop949,1,167293089032,1\n" );
        buffer.append("A1050,testRoute,0:30:00,20121210,1,A976,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop950,1,167293089032,1\n" );
        buffer.append("A1051,testRoute,1:30:00,20121210,1,A977,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop951,1,167293089032,1\n" );
        buffer.append("A1052,testRoute,2:30:00,20121210,1,A978,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop952,1,167293089032,1\n" );
        buffer.append("A1053,testRoute,3:30:00,20121210,1,A979,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop953,1,167293089032,1\n" );
        buffer.append("A1054,testRoute,4:30:00,20121210,1,A980,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop954,1,167293089032,1\n" );
        buffer.append("A1055,testRoute,5:30:00,20121210,1,A981,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop955,1,167293089032,1\n" );
        buffer.append("A1056,testRoute,6:30:00,20121210,1,A982,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop956,1,167293089032,1\n" );
        buffer.append("A1057,testRoute,7:30:00,20121210,1,A983,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop957,1,167293089032,1\n" );
        buffer.append("A1058,testRoute,8:30:00,20121210,1,A984,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop958,1,167293089032,1\n" );
        buffer.append("A1059,testRoute,9:30:00,20121210,1,A985,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop959,1,167293089032,1\n" );
        buffer.append("A1060,testRoute,10:30:00,20121210,1,A986,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop960,1,167293089032,1\n" );
        buffer.append("A1061,testRoute,11:30:00,20121210,1,A987,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop961,1,167293089032,1\n" );
        buffer.append("A1062,testRoute,12:30:00,20121210,1,A988,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop962,1,167293089032,1\n" );
        buffer.append("A1063,testRoute,13:30:00,20121210,1,A989,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop963,1,167293089032,1\n" );
        buffer.append("A1064,testRoute,14:30:00,20121210,1,A990,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop964,1,167293089032,1\n" );
        buffer.append("A1065,testRoute,15:30:00,20121210,1,A991,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop965,1,167293089032,1\n" );
        buffer.append("A1066,testRoute,16:30:00,20121210,1,A992,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop966,1,167293089032,1\n" );
        buffer.append("A1067,testRoute,17:30:00,20121210,1,A993,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop967,1,167293089032,1\n" );
        buffer.append("A1068,testRoute,18:30:00,20121210,1,A994,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop968,1,167293089032,1\n" );
        buffer.append("A1069,testRoute,19:30:00,20121210,1,A995,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop969,1,167293089032,1\n" );
        buffer.append("A1070,testRoute,20:30:00,20121210,1,A996,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop970,1,167293089032,1\n" );
        buffer.append("A1071,testRoute,21:30:00,20121210,1,A997,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop971,1,167293089032,1\n" );
        buffer.append("A1072,testRoute,22:30:00,20121210,1,A998,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop972,1,167293089032,1\n" );
        buffer.append("A1073,testRoute,23:30:00,20121210,1,A999,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop973,1,167293089032,1\n" );
        buffer.append("A1074,testRoute,0:30:00,20121210,1,A1000,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop974,1,167293089032,1\n" );
        buffer.append("A1075,testRoute,1:30:00,20121210,1,A1001,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop975,1,167293089032,1\n" );
        buffer.append("A1076,testRoute,2:30:00,20121210,1,A1002,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop976,1,167293089032,1\n" );
        buffer.append("A1077,testRoute,3:30:00,20121210,1,A1003,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop977,1,167293089032,1\n" );
        buffer.append("A1078,testRoute,4:30:00,20121210,1,A1004,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop978,1,167293089032,1\n" );
        buffer.append("A1079,testRoute,5:30:00,20121210,1,A1005,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop979,1,167293089032,1\n" );
        buffer.append("A1080,testRoute,6:30:00,20121210,1,A1006,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop980,1,167293089032,1\n" );
        buffer.append("A1081,testRoute,7:30:00,20121210,1,A1007,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop981,1,167293089032,1\n" );
        buffer.append("A1082,testRoute,8:30:00,20121210,1,A1008,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop982,1,167293089032,1\n" );
        buffer.append("A1083,testRoute,9:30:00,20121210,1,A1009,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop983,1,167293089032,1\n" );
        buffer.append("A1084,testRoute,10:30:00,20121210,1,A1010,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop984,1,167293089032,1\n" );
        buffer.append("A1085,testRoute,11:30:00,20121210,1,A1011,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop985,1,167293089032,1\n" );
        buffer.append("A1086,testRoute,12:30:00,20121210,1,A1012,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop986,1,167293089032,1\n" );
        buffer.append("A1087,testRoute,13:30:00,20121210,1,A1013,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop987,1,167293089032,1\n" );
        buffer.append("A1088,testRoute,14:30:00,20121210,1,A1014,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop988,1,167293089032,1\n" );
        buffer.append("A1089,testRoute,15:30:00,20121210,1,A1015,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop989,1,167293089032,1\n" );
        buffer.append("A1090,testRoute,16:30:00,20121210,1,A1016,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop990,1,167293089032,1\n" );
        buffer.append("A1091,testRoute,17:30:00,20121210,1,A1017,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop991,1,167293089032,1\n" );
        buffer.append("A1092,testRoute,18:30:00,20121210,1,A1018,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop992,1,167293089032,1\n" );
        buffer.append("A1093,testRoute,19:30:00,20121210,1,A1019,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop993,1,167293089032,1\n" );
        buffer.append("A1094,testRoute,20:30:00,20121210,1,A1020,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop994,1,167293089032,1\n" );
        buffer.append("A1095,testRoute,21:30:00,20121210,1,A1021,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop995,1,167293089032,1\n" );
        buffer.append("A1096,testRoute,22:30:00,20121210,1,A1022,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop996,1,167293089032,1\n" );
        buffer.append("A1097,testRoute,23:30:00,20121210,1,A1023,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop997,1,167293089032,1\n" );
        buffer.append("A1098,testRoute,0:30:00,20121210,1,A1024,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop998,1,167293089032,1\n" );
        buffer.append("A1099,testRoute,1:30:00,20121210,1,A1025,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop999,1,167293089032,1\n" );
        buffer.append("A1100,testRoute,2:30:00,20121210,1,A1026,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1000,1,167293089032,1\n" );
        buffer.append("A1101,testRoute,3:30:00,20121210,1,A1027,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1001,1,167293089032,1\n" );
        buffer.append("A1102,testRoute,4:30:00,20121210,1,A1028,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1002,1,167293089032,1\n" );
        buffer.append("A1103,testRoute,5:30:00,20121210,1,A1029,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1003,1,167293089032,1\n" );
        buffer.append("A1104,testRoute,6:30:00,20121210,1,A1030,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1004,1,167293089032,1\n" );
        buffer.append("A1105,testRoute,7:30:00,20121210,1,A1031,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1005,1,167293089032,1\n" );
        buffer.append("A1106,testRoute,8:30:00,20121210,1,A1032,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1006,1,167293089032,1\n" );
        buffer.append("A1107,testRoute,9:30:00,20121210,1,A1033,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1007,1,167293089032,1\n" );
        buffer.append("A1108,testRoute,10:30:00,20121210,1,A1034,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1008,1,167293089032,1\n" );
        buffer.append("A1109,testRoute,11:30:00,20121210,1,A1035,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1009,1,167293089032,1\n" );
        buffer.append("A1110,testRoute,12:30:00,20121210,1,A1036,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1010,1,167293089032,1\n" );
        buffer.append("A1111,testRoute,13:30:00,20121210,1,A1037,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1011,1,167293089032,1\n" );
        buffer.append("A1112,testRoute,14:30:00,20121210,1,A1038,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1012,1,167293089032,1\n" );
        buffer.append("A1113,testRoute,15:30:00,20121210,1,A1039,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1013,1,167293089032,1\n" );
        buffer.append("A1114,testRoute,16:30:00,20121210,1,A1040,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1014,1,167293089032,1\n" );
        buffer.append("A1115,testRoute,17:30:00,20121210,1,A1041,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1015,1,167293089032,1\n" );
        buffer.append("A1116,testRoute,18:30:00,20121210,1,A1042,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1016,1,167293089032,1\n" );
        buffer.append("A1117,testRoute,19:30:00,20121210,1,A1043,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1017,1,167293089032,1\n" );
        buffer.append("A1118,testRoute,20:30:00,20121210,1,A1044,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1018,1,167293089032,1\n" );
        buffer.append("A1119,testRoute,21:30:00,20121210,1,A1045,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1019,1,167293089032,1\n" );
        buffer.append("A1120,testRoute,22:30:00,20121210,1,A1046,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1020,1,167293089032,1\n" );
        buffer.append("A1121,testRoute,23:30:00,20121210,1,A1047,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1021,1,167293089032,1\n" );
        buffer.append("A1122,testRoute,0:30:00,20121210,1,A1048,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1022,1,167293089032,1\n" );
        buffer.append("A1123,testRoute,1:30:00,20121210,1,A1049,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1023,1,167293089032,1\n" );
        buffer.append("A1124,testRoute,2:30:00,20121210,1,A1050,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1024,1,167293089032,1\n" );
        buffer.append("A1125,testRoute,3:30:00,20121210,1,A1051,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1025,1,167293089032,1\n" );
        buffer.append("A1126,testRoute,4:30:00,20121210,1,A1052,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1026,1,167293089032,1\n" );
        buffer.append("A1127,testRoute,5:30:00,20121210,1,A1053,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1027,1,167293089032,1\n" );
        buffer.append("A1128,testRoute,6:30:00,20121210,1,A1054,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1028,1,167293089032,1\n" );
        buffer.append("A1129,testRoute,7:30:00,20121210,1,A1055,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1029,1,167293089032,1\n" );
        buffer.append("A1130,testRoute,8:30:00,20121210,1,A1056,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1030,1,167293089032,1\n" );
        buffer.append("A1131,testRoute,9:30:00,20121210,1,A1057,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1031,1,167293089032,1\n" );
        buffer.append("A1132,testRoute,10:30:00,20121210,1,A1058,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1032,1,167293089032,1\n" );
        buffer.append("A1133,testRoute,11:30:00,20121210,1,A1059,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1033,1,167293089032,1\n" );
        buffer.append("A1134,testRoute,12:30:00,20121210,1,A1060,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1034,1,167293089032,1\n" );
        buffer.append("A1135,testRoute,13:30:00,20121210,1,A1061,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1035,1,167293089032,1\n" );
        buffer.append("A1136,testRoute,14:30:00,20121210,1,A1062,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1036,1,167293089032,1\n" );
        buffer.append("A1137,testRoute,15:30:00,20121210,1,A1063,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1037,1,167293089032,1\n" );
        buffer.append("A1138,testRoute,16:30:00,20121210,1,A1064,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1038,1,167293089032,1\n" );
        buffer.append("A1139,testRoute,17:30:00,20121210,1,A1065,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1039,1,167293089032,1\n" );
        buffer.append("A1140,testRoute,18:30:00,20121210,1,A1066,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1040,1,167293089032,1\n" );
        buffer.append("A1141,testRoute,19:30:00,20121210,1,A1067,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1041,1,167293089032,1\n" );
        buffer.append("A1142,testRoute,20:30:00,20121210,1,A1068,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1042,1,167293089032,1\n" );
        buffer.append("A1143,testRoute,21:30:00,20121210,1,A1069,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1043,1,167293089032,1\n" );
        buffer.append("A1144,testRoute,22:30:00,20121210,1,A1070,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1044,1,167293089032,1\n" );
        buffer.append("A1145,testRoute,23:30:00,20121210,1,A1071,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1045,1,167293089032,1\n" );
        buffer.append("A1146,testRoute,0:30:00,20121210,1,A1072,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1046,1,167293089032,1\n" );
        buffer.append("A1147,testRoute,1:30:00,20121210,1,A1073,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1047,1,167293089032,1\n" );
        buffer.append("A1148,testRoute,2:30:00,20121210,1,A1074,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1048,1,167293089032,1\n" );
        buffer.append("A1149,testRoute,3:30:00,20121210,1,A1075,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1049,1,167293089032,1\n" );
        buffer.append("A1150,testRoute,4:30:00,20121210,1,A1076,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1050,1,167293089032,1\n" );
        buffer.append("A1151,testRoute,5:30:00,20121210,1,A1077,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1051,1,167293089032,1\n" );
        buffer.append("A1152,testRoute,6:30:00,20121210,1,A1078,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1052,1,167293089032,1\n" );
        buffer.append("A1153,testRoute,7:30:00,20121210,1,A1079,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1053,1,167293089032,1\n" );
        buffer.append("A1154,testRoute,8:30:00,20121210,1,A1080,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1054,1,167293089032,1\n" );
        buffer.append("A1155,testRoute,9:30:00,20121210,1,A1081,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1055,1,167293089032,1\n" );
        buffer.append("A1156,testRoute,10:30:00,20121210,1,A1082,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1056,1,167293089032,1\n" );
        buffer.append("A1157,testRoute,11:30:00,20121210,1,A1083,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1057,1,167293089032,1\n" );
        buffer.append("A1158,testRoute,12:30:00,20121210,1,A1084,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1058,1,167293089032,1\n" );
        buffer.append("A1159,testRoute,13:30:00,20121210,1,A1085,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1059,1,167293089032,1\n" );
        buffer.append("A1160,testRoute,14:30:00,20121210,1,A1086,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1060,1,167293089032,1\n" );
        buffer.append("A1161,testRoute,15:30:00,20121210,1,A1087,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1061,1,167293089032,1\n" );
        buffer.append("A1162,testRoute,16:30:00,20121210,1,A1088,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1062,1,167293089032,1\n" );
        buffer.append("A1163,testRoute,17:30:00,20121210,1,A1089,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1063,1,167293089032,1\n" );
        buffer.append("A1164,testRoute,18:30:00,20121210,1,A1090,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1064,1,167293089032,1\n" );
        buffer.append("A1165,testRoute,19:30:00,20121210,1,A1091,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1065,1,167293089032,1\n" );
        buffer.append("A1166,testRoute,20:30:00,20121210,1,A1092,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1066,1,167293089032,1\n" );
        buffer.append("A1167,testRoute,21:30:00,20121210,1,A1093,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1067,1,167293089032,1\n" );
        buffer.append("A1168,testRoute,22:30:00,20121210,1,A1094,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1068,1,167293089032,1\n" );
        buffer.append("A1169,testRoute,23:30:00,20121210,1,A1095,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1069,1,167293089032,1\n" );
        buffer.append("A1170,testRoute,0:30:00,20121210,1,A1096,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1070,1,167293089032,1\n" );
        buffer.append("A1171,testRoute,1:30:00,20121210,1,A1097,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1071,1,167293089032,1\n" );
        buffer.append("A1172,testRoute,2:30:00,20121210,1,A1098,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1072,1,167293089032,1\n" );
        buffer.append("A1173,testRoute,3:30:00,20121210,1,A1099,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1073,1,167293089032,1\n" );
        buffer.append("A1174,testRoute,4:30:00,20121210,1,A1100,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1074,1,167293089032,1\n" );
        buffer.append("A1175,testRoute,5:30:00,20121210,1,A1101,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1075,1,167293089032,1\n" );
        buffer.append("A1176,testRoute,6:30:00,20121210,1,A1102,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1076,1,167293089032,1\n" );
        buffer.append("A1177,testRoute,7:30:00,20121210,1,A1103,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1077,1,167293089032,1\n" );
        buffer.append("A1178,testRoute,8:30:00,20121210,1,A1104,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1078,1,167293089032,1\n" );
        buffer.append("A1179,testRoute,9:30:00,20121210,1,A1105,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1079,1,167293089032,1\n" );
        buffer.append("A1180,testRoute,10:30:00,20121210,1,A1106,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1080,1,167293089032,1\n" );
        buffer.append("A1181,testRoute,11:30:00,20121210,1,A1107,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1081,1,167293089032,1\n" );
        buffer.append("A1182,testRoute,12:30:00,20121210,1,A1108,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1082,1,167293089032,1\n" );
        buffer.append("A1183,testRoute,13:30:00,20121210,1,A1109,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1083,1,167293089032,1\n" );
        buffer.append("A1184,testRoute,14:30:00,20121210,1,A1110,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1084,1,167293089032,1\n" );
        buffer.append("A1185,testRoute,15:30:00,20121210,1,A1111,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1085,1,167293089032,1\n" );
        buffer.append("A1186,testRoute,16:30:00,20121210,1,A1112,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1086,1,167293089032,1\n" );
        buffer.append("A1187,testRoute,17:30:00,20121210,1,A1113,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1087,1,167293089032,1\n" );
        buffer.append("A1188,testRoute,18:30:00,20121210,1,A1114,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1088,1,167293089032,1\n" );
        buffer.append("A1189,testRoute,19:30:00,20121210,1,A1115,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1089,1,167293089032,1\n" );
        buffer.append("A1190,testRoute,20:30:00,20121210,1,A1116,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1090,1,167293089032,1\n" );
        buffer.append("A1191,testRoute,21:30:00,20121210,1,A1117,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1091,1,167293089032,1\n" );
        buffer.append("A1192,testRoute,22:30:00,20121210,1,A1118,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1092,1,167293089032,1\n" );
        buffer.append("A1193,testRoute,23:30:00,20121210,1,A1119,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1093,1,167293089032,1\n" );
        buffer.append("A1194,testRoute,0:30:00,20121210,1,A1120,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1094,1,167293089032,1\n" );
        buffer.append("A1195,testRoute,1:30:00,20121210,1,A1121,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1095,1,167293089032,1\n" );
        buffer.append("A1196,testRoute,2:30:00,20121210,1,A1122,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1096,1,167293089032,1\n" );
        buffer.append("A1197,testRoute,3:30:00,20121210,1,A1123,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1097,1,167293089032,1\n" );
        buffer.append("A1198,testRoute,4:30:00,20121210,1,A1124,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1098,1,167293089032,1\n" );
        buffer.append("A1199,testRoute,5:30:00,20121210,1,A1125,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1099,1,167293089032,1\n" );
        buffer.append("A1200,testRoute,6:30:00,20121210,1,A1126,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1100,1,167293089032,1\n" );
        buffer.append("A1201,testRoute,7:30:00,20121210,1,A1127,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1101,1,167293089032,1\n" );
        buffer.append("A1202,testRoute,8:30:00,20121210,1,A1128,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1102,1,167293089032,1\n" );
        buffer.append("A1203,testRoute,9:30:00,20121210,1,A1129,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1103,1,167293089032,1\n" );
        buffer.append("A1204,testRoute,10:30:00,20121210,1,A1130,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1104,1,167293089032,1\n" );
        buffer.append("A1205,testRoute,11:30:00,20121210,1,A1131,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1105,1,167293089032,1\n" );
        buffer.append("A1206,testRoute,12:30:00,20121210,1,A1132,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1106,1,167293089032,1\n" );
        buffer.append("A1207,testRoute,13:30:00,20121210,1,A1133,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1107,1,167293089032,1\n" );
        buffer.append("A1208,testRoute,14:30:00,20121210,1,A1134,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1108,1,167293089032,1\n" );
        buffer.append("A1209,testRoute,15:30:00,20121210,1,A1135,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1109,1,167293089032,1\n" );
        buffer.append("A1210,testRoute,16:30:00,20121210,1,A1136,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1110,1,167293089032,1\n" );
        buffer.append("A1211,testRoute,17:30:00,20121210,1,A1137,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1111,1,167293089032,1\n" );
        buffer.append("A1212,testRoute,18:30:00,20121210,1,A1138,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1112,1,167293089032,1\n" );
        buffer.append("A1213,testRoute,19:30:00,20121210,1,A1139,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1113,1,167293089032,1\n" );
        buffer.append("A1214,testRoute,20:30:00,20121210,1,A1140,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1114,1,167293089032,1\n" );
        buffer.append("A1215,testRoute,21:30:00,20121210,1,A1141,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1115,1,167293089032,1\n" );
        buffer.append("A1216,testRoute,22:30:00,20121210,1,A1142,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1116,1,167293089032,1\n" );
        buffer.append("A1217,testRoute,23:30:00,20121210,1,A1143,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1117,1,167293089032,1\n" );
        buffer.append("A1218,testRoute,0:30:00,20121210,1,A1144,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1118,1,167293089032,1\n" );
        buffer.append("A1219,testRoute,1:30:00,20121210,1,A1145,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1119,1,167293089032,1\n" );
        buffer.append("A1220,testRoute,2:30:00,20121210,1,A1146,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1120,1,167293089032,1\n" );
        buffer.append("A1221,testRoute,3:30:00,20121210,1,A1147,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1121,1,167293089032,1\n" );
        buffer.append("A1222,testRoute,4:30:00,20121210,1,A1148,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1122,1,167293089032,1\n" );
        buffer.append("A1223,testRoute,5:30:00,20121210,1,A1149,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1123,1,167293089032,1\n" );
        buffer.append("A1224,testRoute,6:30:00,20121210,1,A1150,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1124,1,167293089032,1\n" );
        buffer.append("A1225,testRoute,7:30:00,20121210,1,A1151,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1125,1,167293089032,1\n" );
        buffer.append("A1226,testRoute,8:30:00,20121210,1,A1152,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1126,1,167293089032,1\n" );
        buffer.append("A1227,testRoute,9:30:00,20121210,1,A1153,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1127,1,167293089032,1\n" );
        buffer.append("A1228,testRoute,10:30:00,20121210,1,A1154,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1128,1,167293089032,1\n" );
        buffer.append("A1229,testRoute,11:30:00,20121210,1,A1155,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1129,1,167293089032,1\n" );
        buffer.append("A1230,testRoute,12:30:00,20121210,1,A1156,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1130,1,167293089032,1\n" );
        buffer.append("A1231,testRoute,13:30:00,20121210,1,A1157,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1131,1,167293089032,1\n" );
        buffer.append("A1232,testRoute,14:30:00,20121210,1,A1158,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1132,1,167293089032,1\n" );
        buffer.append("A1233,testRoute,15:30:00,20121210,1,A1159,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1133,1,167293089032,1\n" );
        buffer.append("A1234,testRoute,16:30:00,20121210,1,A1160,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1134,1,167293089032,1\n" );
        buffer.append("A1235,testRoute,17:30:00,20121210,1,A1161,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1135,1,167293089032,1\n" );
        buffer.append("A1236,testRoute,18:30:00,20121210,1,A1162,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1136,1,167293089032,1\n" );
        buffer.append("A1237,testRoute,19:30:00,20121210,1,A1163,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1137,1,167293089032,1\n" );
        buffer.append("A1238,testRoute,20:30:00,20121210,1,A1164,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1138,1,167293089032,1\n" );
        buffer.append("A1239,testRoute,21:30:00,20121210,1,A1165,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1139,1,167293089032,1\n" );
        buffer.append("A1240,testRoute,22:30:00,20121210,1,A1166,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1140,1,167293089032,1\n" );
        buffer.append("A1241,testRoute,23:30:00,20121210,1,A1167,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1141,1,167293089032,1\n" );
        buffer.append("A1242,testRoute,0:30:00,20121210,1,A1168,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1142,1,167293089032,1\n" );
        buffer.append("A1243,testRoute,1:30:00,20121210,1,A1169,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1143,1,167293089032,1\n" );
        buffer.append("A1244,testRoute,2:30:00,20121210,1,A1170,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1144,1,167293089032,1\n" );
        buffer.append("A1245,testRoute,3:30:00,20121210,1,A1171,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1145,1,167293089032,1\n" );
        buffer.append("A1246,testRoute,4:30:00,20121210,1,A1172,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1146,1,167293089032,1\n" );
        buffer.append("A1247,testRoute,5:30:00,20121210,1,A1173,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1147,1,167293089032,1\n" );
        buffer.append("A1248,testRoute,6:30:00,20121210,1,A1174,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1148,1,167293089032,1\n" );
        buffer.append("A1249,testRoute,7:30:00,20121210,1,A1175,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1149,1,167293089032,1\n" );
        buffer.append("A1250,testRoute,8:30:00,20121210,1,A1176,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1150,1,167293089032,1\n" );
        buffer.append("A1251,testRoute,9:30:00,20121210,1,A1177,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1151,1,167293089032,1\n" );
        buffer.append("A1252,testRoute,10:30:00,20121210,1,A1178,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1152,1,167293089032,1\n" );
        buffer.append("A1253,testRoute,11:30:00,20121210,1,A1179,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1153,1,167293089032,1\n" );
        buffer.append("A1254,testRoute,12:30:00,20121210,1,A1180,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1154,1,167293089032,1\n" );
        buffer.append("A1255,testRoute,13:30:00,20121210,1,A1181,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1155,1,167293089032,1\n" );
        buffer.append("A1256,testRoute,14:30:00,20121210,1,A1182,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1156,1,167293089032,1\n" );
        buffer.append("A1257,testRoute,15:30:00,20121210,1,A1183,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1157,1,167293089032,1\n" );
        buffer.append("A1258,testRoute,16:30:00,20121210,1,A1184,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1158,1,167293089032,1\n" );
        buffer.append("A1259,testRoute,17:30:00,20121210,1,A1185,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1159,1,167293089032,1\n" );
        buffer.append("A1260,testRoute,18:30:00,20121210,1,A1186,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1160,1,167293089032,1\n" );
        buffer.append("A1261,testRoute,19:30:00,20121210,1,A1187,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1161,1,167293089032,1\n" );
        buffer.append("A1262,testRoute,20:30:00,20121210,1,A1188,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1162,1,167293089032,1\n" );
        buffer.append("A1263,testRoute,21:30:00,20121210,1,A1189,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1163,1,167293089032,1\n" );
        buffer.append("A1264,testRoute,22:30:00,20121210,1,A1190,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1164,1,167293089032,1\n" );
        buffer.append("A1265,testRoute,23:30:00,20121210,1,A1191,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1165,1,167293089032,1\n" );
        buffer.append("A1266,testRoute,0:30:00,20121210,1,A1192,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1166,1,167293089032,1\n" );
        buffer.append("A1267,testRoute,1:30:00,20121210,1,A1193,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1167,1,167293089032,1\n" );
        buffer.append("A1268,testRoute,2:30:00,20121210,1,A1194,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1168,1,167293089032,1\n" );
        buffer.append("A1269,testRoute,3:30:00,20121210,1,A1195,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1169,1,167293089032,1\n" );
        buffer.append("A1270,testRoute,4:30:00,20121210,1,A1196,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1170,1,167293089032,1\n" );
        buffer.append("A1271,testRoute,5:30:00,20121210,1,A1197,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1171,1,167293089032,1\n" );
        buffer.append("A1272,testRoute,6:30:00,20121210,1,A1198,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1172,1,167293089032,1\n" );
        buffer.append("A1273,testRoute,7:30:00,20121210,1,A1199,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1173,1,167293089032,1\n" );
        buffer.append("A1274,testRoute,8:30:00,20121210,1,A1200,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1174,1,167293089032,1\n" );
        buffer.append("A1275,testRoute,9:30:00,20121210,1,A1201,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1175,1,167293089032,1\n" );
        buffer.append("A1276,testRoute,10:30:00,20121210,1,A1202,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1176,1,167293089032,1\n" );
        buffer.append("A1277,testRoute,11:30:00,20121210,1,A1203,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1177,1,167293089032,1\n" );
        buffer.append("A1278,testRoute,12:30:00,20121210,1,A1204,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1178,1,167293089032,1\n" );
        buffer.append("A1279,testRoute,13:30:00,20121210,1,A1205,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1179,1,167293089032,1\n" );
        buffer.append("A1280,testRoute,14:30:00,20121210,1,A1206,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1180,1,167293089032,1\n" );
        buffer.append("A1281,testRoute,15:30:00,20121210,1,A1207,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1181,1,167293089032,1\n" );
        buffer.append("A1282,testRoute,16:30:00,20121210,1,A1208,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1182,1,167293089032,1\n" );
        buffer.append("A1283,testRoute,17:30:00,20121210,1,A1209,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1183,1,167293089032,1\n" );
        buffer.append("A1284,testRoute,18:30:00,20121210,1,A1210,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1184,1,167293089032,1\n" );
        buffer.append("A1285,testRoute,19:30:00,20121210,1,A1211,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1185,1,167293089032,1\n" );
        buffer.append("A1286,testRoute,20:30:00,20121210,1,A1212,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1186,1,167293089032,1\n" );
        buffer.append("A1287,testRoute,21:30:00,20121210,1,A1213,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1187,1,167293089032,1\n" );
        buffer.append("A1288,testRoute,22:30:00,20121210,1,A1214,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1188,1,167293089032,1\n" );
        buffer.append("A1289,testRoute,23:30:00,20121210,1,A1215,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1189,1,167293089032,1\n" );
        buffer.append("A1290,testRoute,0:30:00,20121210,1,A1216,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1190,1,167293089032,1\n" );
        buffer.append("A1291,testRoute,1:30:00,20121210,1,A1217,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1191,1,167293089032,1\n" );
        buffer.append("A1292,testRoute,2:30:00,20121210,1,A1218,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1192,1,167293089032,1\n" );
        buffer.append("A1293,testRoute,3:30:00,20121210,1,A1219,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1193,1,167293089032,1\n" );
        buffer.append("A1294,testRoute,4:30:00,20121210,1,A1220,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1194,1,167293089032,1\n" );
        buffer.append("A1295,testRoute,5:30:00,20121210,1,A1221,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1195,1,167293089032,1\n" );
        buffer.append("A1296,testRoute,6:30:00,20121210,1,A1222,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1196,1,167293089032,1\n" );
        buffer.append("A1297,testRoute,7:30:00,20121210,1,A1223,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1197,1,167293089032,1\n" );
        buffer.append("A1298,testRoute,8:30:00,20121210,1,A1224,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1198,1,167293089032,1\n" );
        buffer.append("A1299,testRoute,9:30:00,20121210,1,A1225,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1199,1,167293089032,1\n" );
        buffer.append("A1300,testRoute,10:30:00,20121210,1,A1226,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1200,1,167293089032,1\n" );
        buffer.append("A1301,testRoute,11:30:00,20121210,1,A1227,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1201,1,167293089032,1\n" );
        buffer.append("A1302,testRoute,12:30:00,20121210,1,A1228,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1202,1,167293089032,1\n" );
        buffer.append("A1303,testRoute,13:30:00,20121210,1,A1229,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1203,1,167293089032,1\n" );
        buffer.append("A1304,testRoute,14:30:00,20121210,1,A1230,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1204,1,167293089032,1\n" );
        buffer.append("A1305,testRoute,15:30:00,20121210,1,A1231,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1205,1,167293089032,1\n" );
        buffer.append("A1306,testRoute,16:30:00,20121210,1,A1232,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1206,1,167293089032,1\n" );
        buffer.append("A1307,testRoute,17:30:00,20121210,1,A1233,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1207,1,167293089032,1\n" );
        buffer.append("A1308,testRoute,18:30:00,20121210,1,A1234,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1208,1,167293089032,1\n" );
        buffer.append("A1309,testRoute,19:30:00,20121210,1,A1235,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1209,1,167293089032,1\n" );
        buffer.append("A1310,testRoute,20:30:00,20121210,1,A1236,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1210,1,167293089032,1\n" );
        buffer.append("A1311,testRoute,21:30:00,20121210,1,A1237,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1211,1,167293089032,1\n" );
        buffer.append("A1312,testRoute,22:30:00,20121210,1,A1238,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1212,1,167293089032,1\n" );
        buffer.append("A1313,testRoute,23:30:00,20121210,1,A1239,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1213,1,167293089032,1\n" );
        buffer.append("A1314,testRoute,0:30:00,20121210,1,A1240,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1214,1,167293089032,1\n" );
        buffer.append("A1315,testRoute,1:30:00,20121210,1,A1241,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1215,1,167293089032,1\n" );
        buffer.append("A1316,testRoute,2:30:00,20121210,1,A1242,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1216,1,167293089032,1\n" );
        buffer.append("A1317,testRoute,3:30:00,20121210,1,A1243,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1217,1,167293089032,1\n" );
        buffer.append("A1318,testRoute,4:30:00,20121210,1,A1244,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1218,1,167293089032,1\n" );
        buffer.append("A1319,testRoute,5:30:00,20121210,1,A1245,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1219,1,167293089032,1\n" );
        buffer.append("A1320,testRoute,6:30:00,20121210,1,A1246,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1220,1,167293089032,1\n" );
        buffer.append("A1321,testRoute,7:30:00,20121210,1,A1247,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1221,1,167293089032,1\n" );
        buffer.append("A1322,testRoute,8:30:00,20121210,1,A1248,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1222,1,167293089032,1\n" );
        buffer.append("A1323,testRoute,9:30:00,20121210,1,A1249,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1223,1,167293089032,1\n" );
        buffer.append("A1324,testRoute,10:30:00,20121210,1,A1250,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1224,1,167293089032,1\n" );
        buffer.append("A1325,testRoute,11:30:00,20121210,1,A1251,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1225,1,167293089032,1\n" );
        buffer.append("A1326,testRoute,12:30:00,20121210,1,A1252,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1226,1,167293089032,1\n" );
        buffer.append("A1327,testRoute,13:30:00,20121210,1,A1253,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1227,1,167293089032,1\n" );
        buffer.append("A1328,testRoute,14:30:00,20121210,1,A1254,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1228,1,167293089032,1\n" );
        buffer.append("A1329,testRoute,15:30:00,20121210,1,A1255,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1229,1,167293089032,1\n" );
        buffer.append("A1330,testRoute,16:30:00,20121210,1,A1256,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1230,1,167293089032,1\n" );
        buffer.append("A1331,testRoute,17:30:00,20121210,1,A1257,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1231,1,167293089032,1\n" );
        buffer.append("A1332,testRoute,18:30:00,20121210,1,A1258,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1232,1,167293089032,1\n" );
        buffer.append("A1333,testRoute,19:30:00,20121210,1,A1259,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1233,1,167293089032,1\n" );
        buffer.append("A1334,testRoute,20:30:00,20121210,1,A1260,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1234,1,167293089032,1\n" );
        buffer.append("A1335,testRoute,21:30:00,20121210,1,A1261,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1235,1,167293089032,1\n" );
        buffer.append("A1336,testRoute,22:30:00,20121210,1,A1262,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1236,1,167293089032,1\n" );
        buffer.append("A1337,testRoute,23:30:00,20121210,1,A1263,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1237,1,167293089032,1\n" );
        buffer.append("A1338,testRoute,0:30:00,20121210,1,A1264,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1238,1,167293089032,1\n" );
        buffer.append("A1339,testRoute,1:30:00,20121210,1,A1265,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1239,1,167293089032,1\n" );
        buffer.append("A1340,testRoute,2:30:00,20121210,1,A1266,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1240,1,167293089032,1\n" );
        buffer.append("A1341,testRoute,3:30:00,20121210,1,A1267,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1241,1,167293089032,1\n" );
        buffer.append("A1342,testRoute,4:30:00,20121210,1,A1268,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1242,1,167293089032,1\n" );
        buffer.append("A1343,testRoute,5:30:00,20121210,1,A1269,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1243,1,167293089032,1\n" );
        buffer.append("A1344,testRoute,6:30:00,20121210,1,A1270,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1244,1,167293089032,1\n" );
        buffer.append("A1345,testRoute,7:30:00,20121210,1,A1271,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1245,1,167293089032,1\n" );
        buffer.append("A1346,testRoute,8:30:00,20121210,1,A1272,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1246,1,167293089032,1\n" );
        buffer.append("A1347,testRoute,9:30:00,20121210,1,A1273,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1247,1,167293089032,1\n" );
        buffer.append("A1348,testRoute,10:30:00,20121210,1,A1274,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1248,1,167293089032,1\n" );
        buffer.append("A1349,testRoute,11:30:00,20121210,1,A1275,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1249,1,167293089032,1\n" );
        buffer.append("A1350,testRoute,12:30:00,20121210,1,A1276,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1250,1,167293089032,1\n" );
        buffer.append("A1351,testRoute,13:30:00,20121210,1,A1277,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1251,1,167293089032,1\n" );
        buffer.append("A1352,testRoute,14:30:00,20121210,1,A1278,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1252,1,167293089032,1\n" );
        buffer.append("A1353,testRoute,15:30:00,20121210,1,A1279,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1253,1,167293089032,1\n" );
        buffer.append("A1354,testRoute,16:30:00,20121210,1,A1280,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1254,1,167293089032,1\n" );
        buffer.append("A1355,testRoute,17:30:00,20121210,1,A1281,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1255,1,167293089032,1\n" );
        buffer.append("A1356,testRoute,18:30:00,20121210,1,A1282,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1256,1,167293089032,1\n" );
        buffer.append("A1357,testRoute,19:30:00,20121210,1,A1283,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1257,1,167293089032,1\n" );
        buffer.append("A1358,testRoute,20:30:00,20121210,1,A1284,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1258,1,167293089032,1\n" );
        buffer.append("A1359,testRoute,21:30:00,20121210,1,A1285,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1259,1,167293089032,1\n" );
        buffer.append("A1360,testRoute,22:30:00,20121210,1,A1286,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1260,1,167293089032,1\n" );
        buffer.append("A1361,testRoute,23:30:00,20121210,1,A1287,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1261,1,167293089032,1\n" );
        buffer.append("A1362,testRoute,0:30:00,20121210,1,A1288,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1262,1,167293089032,1\n" );
        buffer.append("A1363,testRoute,1:30:00,20121210,1,A1289,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1263,1,167293089032,1\n" );
        buffer.append("A1364,testRoute,2:30:00,20121210,1,A1290,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1264,1,167293089032,1\n" );
        buffer.append("A1365,testRoute,3:30:00,20121210,1,A1291,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1265,1,167293089032,1\n" );
        buffer.append("A1366,testRoute,4:30:00,20121210,1,A1292,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1266,1,167293089032,1\n" );
        buffer.append("A1367,testRoute,5:30:00,20121210,1,A1293,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1267,1,167293089032,1\n" );
        buffer.append("A1368,testRoute,6:30:00,20121210,1,A1294,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1268,1,167293089032,1\n" );
        buffer.append("A1369,testRoute,7:30:00,20121210,1,A1295,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1269,1,167293089032,1\n" );
        buffer.append("A1370,testRoute,8:30:00,20121210,1,A1296,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1270,1,167293089032,1\n" );
        buffer.append("A1371,testRoute,9:30:00,20121210,1,A1297,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1271,1,167293089032,1\n" );
        buffer.append("A1372,testRoute,10:30:00,20121210,1,A1298,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1272,1,167293089032,1\n" );
        buffer.append("A1373,testRoute,11:30:00,20121210,1,A1299,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1273,1,167293089032,1\n" );
        buffer.append("A1374,testRoute,12:30:00,20121210,1,A1300,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1274,1,167293089032,1\n" );
        buffer.append("A1375,testRoute,13:30:00,20121210,1,A1301,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1275,1,167293089032,1\n" );
        buffer.append("A1376,testRoute,14:30:00,20121210,1,A1302,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1276,1,167293089032,1\n" );
        buffer.append("A1377,testRoute,15:30:00,20121210,1,A1303,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1277,1,167293089032,1\n" );
        buffer.append("A1378,testRoute,16:30:00,20121210,1,A1304,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1278,1,167293089032,1\n" );
        buffer.append("A1379,testRoute,17:30:00,20121210,1,A1305,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1279,1,167293089032,1\n" );
        buffer.append("A1380,testRoute,18:30:00,20121210,1,A1306,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1280,1,167293089032,1\n" );
        buffer.append("A1381,testRoute,19:30:00,20121210,1,A1307,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1281,1,167293089032,1\n" );
        buffer.append("A1382,testRoute,20:30:00,20121210,1,A1308,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1282,1,167293089032,1\n" );
        buffer.append("A1383,testRoute,21:30:00,20121210,1,A1309,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1283,1,167293089032,1\n" );
        buffer.append("A1384,testRoute,22:30:00,20121210,1,A1310,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1284,1,167293089032,1\n" );
        buffer.append("A1385,testRoute,23:30:00,20121210,1,A1311,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1285,1,167293089032,1\n" );
        buffer.append("A1386,testRoute,0:30:00,20121210,1,A1312,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1286,1,167293089032,1\n" );
        buffer.append("A1387,testRoute,1:30:00,20121210,1,A1313,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1287,1,167293089032,1\n" );
        buffer.append("A1388,testRoute,2:30:00,20121210,1,A1314,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1288,1,167293089032,1\n" );
        buffer.append("A1389,testRoute,3:30:00,20121210,1,A1315,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1289,1,167293089032,1\n" );
        buffer.append("A1390,testRoute,4:30:00,20121210,1,A1316,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1290,1,167293089032,1\n" );
        buffer.append("A1391,testRoute,5:30:00,20121210,1,A1317,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1291,1,167293089032,1\n" );
        buffer.append("A1392,testRoute,6:30:00,20121210,1,A1318,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1292,1,167293089032,1\n" );
        buffer.append("A1393,testRoute,7:30:00,20121210,1,A1319,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1293,1,167293089032,1\n" );
        buffer.append("A1394,testRoute,8:30:00,20121210,1,A1320,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1294,1,167293089032,1\n" );
        buffer.append("A1395,testRoute,9:30:00,20121210,1,A1321,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1295,1,167293089032,1\n" );
        buffer.append("A1396,testRoute,10:30:00,20121210,1,A1322,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1296,1,167293089032,1\n" );
        buffer.append("A1397,testRoute,11:30:00,20121210,1,A1323,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1297,1,167293089032,1\n" );
        buffer.append("A1398,testRoute,12:30:00,20121210,1,A1324,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1298,1,167293089032,1\n" );
        buffer.append("A1399,testRoute,13:30:00,20121210,1,A1325,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1299,1,167293089032,1\n" );
        buffer.append("A1400,testRoute,14:30:00,20121210,1,A1326,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1300,1,167293089032,1\n" );
        buffer.append("A1401,testRoute,15:30:00,20121210,1,A1327,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1301,1,167293089032,1\n" );
        buffer.append("A1402,testRoute,16:30:00,20121210,1,A1328,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1302,1,167293089032,1\n" );
        buffer.append("A1403,testRoute,17:30:00,20121210,1,A1329,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1303,1,167293089032,1\n" );
        buffer.append("A1404,testRoute,18:30:00,20121210,1,A1330,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1304,1,167293089032,1\n" );
        buffer.append("A1405,testRoute,19:30:00,20121210,1,A1331,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1305,1,167293089032,1\n" );
        buffer.append("A1406,testRoute,20:30:00,20121210,1,A1332,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1306,1,167293089032,1\n" );
        buffer.append("A1407,testRoute,21:30:00,20121210,1,A1333,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1307,1,167293089032,1\n" );
        buffer.append("A1408,testRoute,22:30:00,20121210,1,A1334,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1308,1,167293089032,1\n" );
        buffer.append("A1409,testRoute,23:30:00,20121210,1,A1335,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1309,1,167293089032,1\n" );
        buffer.append("A1410,testRoute,0:30:00,20121210,1,A1336,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1310,1,167293089032,1\n" );
        buffer.append("A1411,testRoute,1:30:00,20121210,1,A1337,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1311,1,167293089032,1\n" );
        buffer.append("A1412,testRoute,2:30:00,20121210,1,A1338,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1312,1,167293089032,1\n" );
        buffer.append("A1413,testRoute,3:30:00,20121210,1,A1339,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1313,1,167293089032,1\n" );
        buffer.append("A1414,testRoute,4:30:00,20121210,1,A1340,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1314,1,167293089032,1\n" );
        buffer.append("A1415,testRoute,5:30:00,20121210,1,A1341,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1315,1,167293089032,1\n" );
        buffer.append("A1416,testRoute,6:30:00,20121210,1,A1342,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1316,1,167293089032,1\n" );
        buffer.append("A1417,testRoute,7:30:00,20121210,1,A1343,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1317,1,167293089032,1\n" );
        buffer.append("A1418,testRoute,8:30:00,20121210,1,A1344,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1318,1,167293089032,1\n" );
        buffer.append("A1419,testRoute,9:30:00,20121210,1,A1345,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1319,1,167293089032,1\n" );
        buffer.append("A1420,testRoute,10:30:00,20121210,1,A1346,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1320,1,167293089032,1\n" );
        buffer.append("A1421,testRoute,11:30:00,20121210,1,A1347,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1321,1,167293089032,1\n" );
        buffer.append("A1422,testRoute,12:30:00,20121210,1,A1348,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1322,1,167293089032,1\n" );
        buffer.append("A1423,testRoute,13:30:00,20121210,1,A1349,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1323,1,167293089032,1\n" );
        buffer.append("A1424,testRoute,14:30:00,20121210,1,A1350,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1324,1,167293089032,1\n" );
        buffer.append("A1425,testRoute,15:30:00,20121210,1,A1351,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1325,1,167293089032,1\n" );
        buffer.append("A1426,testRoute,16:30:00,20121210,1,A1352,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1326,1,167293089032,1\n" );
        buffer.append("A1427,testRoute,17:30:00,20121210,1,A1353,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1327,1,167293089032,1\n" );
        buffer.append("A1428,testRoute,18:30:00,20121210,1,A1354,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1328,1,167293089032,1\n" );
        buffer.append("A1429,testRoute,19:30:00,20121210,1,A1355,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1329,1,167293089032,1\n" );
        buffer.append("A1430,testRoute,20:30:00,20121210,1,A1356,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1330,1,167293089032,1\n" );
        buffer.append("A1431,testRoute,21:30:00,20121210,1,A1357,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1331,1,167293089032,1\n" );
        buffer.append("A1432,testRoute,22:30:00,20121210,1,A1358,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1332,1,167293089032,1\n" );
        buffer.append("A1433,testRoute,23:30:00,20121210,1,A1359,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1333,1,167293089032,1\n" );
        buffer.append("A1434,testRoute,0:30:00,20121210,1,A1360,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1334,1,167293089032,1\n" );
        buffer.append("A1435,testRoute,1:30:00,20121210,1,A1361,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1335,1,167293089032,1\n" );
        buffer.append("A1436,testRoute,2:30:00,20121210,1,A1362,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1336,1,167293089032,1\n" );
        buffer.append("A1437,testRoute,3:30:00,20121210,1,A1363,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1337,1,167293089032,1\n" );
        buffer.append("A1438,testRoute,4:30:00,20121210,1,A1364,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1338,1,167293089032,1\n" );
        buffer.append("A1439,testRoute,5:30:00,20121210,1,A1365,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1339,1,167293089032,1\n" );
        buffer.append("A1440,testRoute,6:30:00,20121210,1,A1366,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1340,1,167293089032,1\n" );
        buffer.append("A1441,testRoute,7:30:00,20121210,1,A1367,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1341,1,167293089032,1\n" );
        buffer.append("A1442,testRoute,8:30:00,20121210,1,A1368,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1342,1,167293089032,1\n" );
        buffer.append("A1443,testRoute,9:30:00,20121210,1,A1369,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1343,1,167293089032,1\n" );
        buffer.append("A1444,testRoute,10:30:00,20121210,1,A1370,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1344,1,167293089032,1\n" );
        buffer.append("A1445,testRoute,11:30:00,20121210,1,A1371,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1345,1,167293089032,1\n" );
        buffer.append("A1446,testRoute,12:30:00,20121210,1,A1372,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1346,1,167293089032,1\n" );
        buffer.append("A1447,testRoute,13:30:00,20121210,1,A1373,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1347,1,167293089032,1\n" );
        buffer.append("A1448,testRoute,14:30:00,20121210,1,A1374,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1348,1,167293089032,1\n" );
        buffer.append("A1449,testRoute,15:30:00,20121210,1,A1375,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1349,1,167293089032,1\n" );
        buffer.append("A1450,testRoute,16:30:00,20121210,1,A1376,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1350,1,167293089032,1\n" );
        buffer.append("A1451,testRoute,17:30:00,20121210,1,A1377,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1351,1,167293089032,1\n" );
        buffer.append("A1452,testRoute,18:30:00,20121210,1,A1378,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1352,1,167293089032,1\n" );
        buffer.append("A1453,testRoute,19:30:00,20121210,1,A1379,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1353,1,167293089032,1\n" );
        buffer.append("A1454,testRoute,20:30:00,20121210,1,A1380,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1354,1,167293089032,1\n" );
        buffer.append("A1455,testRoute,21:30:00,20121210,1,A1381,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1355,1,167293089032,1\n" );
        buffer.append("A1456,testRoute,22:30:00,20121210,1,A1382,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1356,1,167293089032,1\n" );
        buffer.append("A1457,testRoute,23:30:00,20121210,1,A1383,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1357,1,167293089032,1\n" );
        buffer.append("A1458,testRoute,0:30:00,20121210,1,A1384,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1358,1,167293089032,1\n" );
        buffer.append("A1459,testRoute,1:30:00,20121210,1,A1385,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1359,1,167293089032,1\n" );
        buffer.append("A1460,testRoute,2:30:00,20121210,1,A1386,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1360,1,167293089032,1\n" );
        buffer.append("A1461,testRoute,3:30:00,20121210,1,A1387,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1361,1,167293089032,1\n" );
        buffer.append("A1462,testRoute,4:30:00,20121210,1,A1388,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1362,1,167293089032,1\n" );
        buffer.append("A1463,testRoute,5:30:00,20121210,1,A1389,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1363,1,167293089032,1\n" );
        buffer.append("A1464,testRoute,6:30:00,20121210,1,A1390,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1364,1,167293089032,1\n" );
        buffer.append("A1465,testRoute,7:30:00,20121210,1,A1391,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1365,1,167293089032,1\n" );
        buffer.append("A1466,testRoute,8:30:00,20121210,1,A1392,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1366,1,167293089032,1\n" );
        buffer.append("A1467,testRoute,9:30:00,20121210,1,A1393,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1367,1,167293089032,1\n" );
        buffer.append("A1468,testRoute,10:30:00,20121210,1,A1394,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1368,1,167293089032,1\n" );
        buffer.append("A1469,testRoute,11:30:00,20121210,1,A1395,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1369,1,167293089032,1\n" );
        buffer.append("A1470,testRoute,12:30:00,20121210,1,A1396,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1370,1,167293089032,1\n" );
        buffer.append("A1471,testRoute,13:30:00,20121210,1,A1397,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1371,1,167293089032,1\n" );
        buffer.append("A1472,testRoute,14:30:00,20121210,1,A1398,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1372,1,167293089032,1\n");
     return buffer.toString();
    }
    
}
