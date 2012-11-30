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
    // wrong data type for column
    // duplicate trip ids in csv
    // empty csv
    // empty csv row
    // large content
    //
    // READING:
    // - process & read
    // process read, process read checking update
    // read null buffer
    // verify debug results
    
    @Test
    public void testCSVBasicConvertAndVerify() {

        String csvData = "123.23.trip,testRoute,11:30:00,20121210,1,A27,Some trip,None,30.76864309,-150.3478953,35.4312,12334.321,20.23,4,stop1,1,167293089032,1\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
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
    
    
}
