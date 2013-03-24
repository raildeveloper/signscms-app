// RailCorp 2012
package au.gov.nsw.railcorp.converter;


import au.gov.nsw.railcorp.gtfs.converter.ServiceAlertCsvConverter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.Alert.Cause;
import com.google.transit.realtime.GtfsRealtime.Alert.Effect;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.TimeRange;

import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


/**
 * @author John
 *
 */
public class ServiceAlertCsvConverterTest extends TestCase {

    ServiceAlertCsvConverter converter;
    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        converter = new ServiceAlertCsvConverter();
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

    // Test cases:
    // CONVERSION:
    // - basic content
    // - missing csv optional values
    // - missing csv required values (if any) -None for Service Alerts
    // - wrong data type for column
    // - empty csv
    // - empty csv row
    // large content
    //
    // READING:
    // - process & read
    // - process read, process read checking update
    // - read null buffer
    // - verify debug results
    
    
    /**
     * Test method for {@link au.gov.nsw.railcorp.gtfs.converter.GeneralCsvConverter#convertAndStoreCsv(java.io.Reader)}.
     */
    @Test
    public void testBasicConvertAndVerify() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
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
        assertTrue(e.hasAlert());
        assertFalse(e.hasVehicle());
        
        Alert a = e.getAlert();
        assertEquals(1, a.getActivePeriodCount());
        TimeRange t = a.getActivePeriod(0);
        assertEquals(1029783620, t.getStart());
        assertEquals(1029784000, t.getEnd());
        
        assertEquals(1, a.getInformedEntityCount());
        EntitySelector es = a.getInformedEntity(0);
        assertTrue(es.hasAgencyId());
        assertTrue(es.hasRouteId());
        assertTrue(es.hasRouteType());
        assertTrue(es.hasStopId());
        assertTrue(es.hasTrip());
        assertEquals("Rail1", es.getAgencyId());
        assertEquals("Ehls", es.getRouteId());
        assertEquals(2, es.getRouteType());
        assertEquals("stop1", es.getStopId());
        assertEquals("101A", es.getTrip().getTripId());
        
        assertTrue(a.hasCause());
        assertTrue(a.hasEffect());
        assertTrue(a.hasUrl());
        assertTrue(a.hasDescriptionText());
        assertTrue(a.hasDescriptionText());
        
        assertEquals(Cause.UNKNOWN_CAUSE, a.getCause());
        assertEquals(Effect.DETOUR, a.getEffect());
        assertEquals(1, a.getUrl().getTranslationCount());
        assertEquals("www.131500.com.au", a.getUrl().getTranslation(0).getText());
        assertEquals("en", a.getUrl().getTranslation(0).getLanguage());
        assertEquals(1, a.getHeaderText().getTranslationCount());
        assertEquals("Major Delays", a.getHeaderText().getTranslation(0).getText());
        assertEquals("en", a.getHeaderText().getTranslation(0).getLanguage());
        assertEquals(1, a.getDescriptionText().getTranslationCount());
        assertEquals("Train derailed at Revesby", a.getDescriptionText().getTranslation(0).getText());
        assertEquals("en", a.getDescriptionText().getTranslation(0).getLanguage());
    }

    @Test
    public void testMissingOptionalActivePeriodStart() {

        String csvData = ",1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalActivePeriodEnd() {

        String csvData = "1029783620,,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalAgencyId() {

        String csvData = "1029783620,1029784000,,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalRouteId() {

        String csvData = "1029783620,1029784000,Rail1,,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalRouteType() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalTripId() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalStopId() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalCause() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalEffect() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalUrl() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalHeaderText() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    @Test
    public void testMissingOptionalDescription() {

        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
    }    
    
    @Test
    public void testUninitialisedConverterBuffer() {
        assertEquals(null, converter.getCurrentProtoBuf());
        assertEquals("", converter.getCurrentProtoBufDebug());
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
    public void testProcessCSVUpdateFlush() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        csvData = "1029783000,1029784000,Rail1,Ehls,2,,Revesby,1,4,www.131500.com.au,Lift Outage,Lift Broken at Revesby\n";
        reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(1, mesg.getEntityCount());
        assertEquals(1029783000L, mesg.getEntity(0).getAlert().getActivePeriod(0).getStart());
        assertEquals("Revesby", mesg.getEntity(0).getAlert().getInformedEntity(0).getStopId());
        assertEquals("Lift Outage", mesg.getEntity(0).getAlert().getHeaderText().getTranslation(0).getText());
    }
    
    @Test
    public void testProcessCSVUpdateFlush2() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        converter.convertAndStoreCsv(reader);
        
        csvData = "1029783000,1029784000,Rail1,Ehls,2,,BeverlyHills,1,4,www.131500.com.au,Lift Outage,Lift1 Broken at Beverly Hills\n" +
                "1029783000,1029784000,Rail1,Ehls,2,,Revesby,1,4,www.131500.com.au,Lift Outage,Lift2 Broken at Revesby\n" +
                "1029783000,1029784000,Rail1,Ehls,2,,Revesby,1,4,www.131500.com.au,Lift Outage,Lift3 Broken at Revesby\n";
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
    public void testEmptyCSVRow() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n" +
                ",,,,,,,,,,,\n" +
                "1029783000,1029784000,Rail1,Ehls,2,,BeverlyHills,1,4,www.131500.com.au,Lift Outage,Lift Broken at Beverly Hills\n";
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
    public void testWrongColumnDataTypeActivePeriodStart() {
        String csvData = "Start,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeActivePeriodEnd() {
        String csvData = "1029783620,End,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeRouteType() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,RouteType,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeCause() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,OTHER_CAUSE,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    @Test
    public void testWrongColumnDataTypeEffect() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,REDUCED_SERVICE,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertFalse(converter.convertAndStoreCsv(reader));
    }
    
    @Test
    public void testVerifyDebugReadResults() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,Train derailed at Revesby\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        String expectedDebug =
                    "header {\n" +
                    "  gtfs_realtime_version: \"1.0\"\n" +
                    "  incrementality: FULL_DATASET\n" +
                    //"  timestamp: 1354514500\n" + // Removed so comparisons can be made
                    "}\n" +
                    "entity {\n" +
                    "  id: \"1\"\n" +
                    "  alert {\n" +
                    "    active_period {\n" +
                    "      start: 1029783620\n" +
                    "      end: 1029784000\n" +
                    "    }\n" +
                    "    informed_entity {\n" +
                    "      agency_id: \"Rail1\"\n" +
                    "      route_id: \"Ehls\"\n" +
                    "      route_type: 2\n" +
                    "      trip {\n" +
                    "        trip_id: \"101A\"\n" +
                    "      }\n" +
                    "      stop_id: \"stop1\"\n" +
                    "    }\n" +
                    "    cause: UNKNOWN_CAUSE\n" +
                    "    effect: DETOUR\n" +
                    "    url {\n" +
                    "      translation {\n" +
                    "        text: \"www.131500.com.au\"\n" +
                    "        language: \"en\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "    header_text {\n" +
                    "      translation {\n" +
                    "        text: \"Major Delays\"\n" +
                    "        language: \"en\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "    description_text {\n" +
                    "      translation {\n" +
                    "        text: \"Train derailed at Revesby\"\n" +
                    "        language: \"en\"\n" +
                    "      }\n" +
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
    public void testLargeDataSet() {        
        InputStreamReader file = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("servicealerts-largetestdata.csv"));
        assertTrue(converter.convertAndStoreCsv(file));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(1046, mesg.getEntityCount());
    }

    @Test
    public void testProductionFailingLogs() {        
        InputStreamReader file = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("servicealertstest-fails-production.csv"));
        assertTrue(converter.convertAndStoreCsv(file));
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertTrue(mesg != null);
    }
    
    
    @Test
    public void testCommaInText() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals("Train derailed, at Revesby", mesg.getEntity(0).getAlert().getDescriptionText().getTranslation(0).getText());

    }

    /**
     * Check for escaped quotes inside a quoted text field. Supported escaping is "" for a single double quote
     */
    @Test
    public void testCommaQuotesInText() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed\"\", \"\"at Revesby\"\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals("Train derailed\", \"at Revesby", mesg.getEntity(0).getAlert().getDescriptionText().getTranslation(0).getText());

    }
    
    public void testCommaSingleQuotesInText() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed', 'at Revesby\"\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals("Train derailed', 'at Revesby", mesg.getEntity(0).getAlert().getDescriptionText().getTranslation(0).getText());

    }

    public void testQuotesOnEnum() {
        String csvData = "1029783620,1029784000,Rail1,Ehls,2,101A,stop1,1,\"4\",www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(Effect.DETOUR, mesg.getEntity(0).getAlert().getEffect());

    }
    
    public void testDuplicateAlertMessageAggregation() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls3,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(1,mesg.getEntityCount());
        assertEquals(3, mesg.getEntity(0).getAlert().getInformedEntityCount());
        Alert a = mesg.getEntity(0).getAlert();
        assertEquals("Ehls1", a.getInformedEntity(0).getRouteId());
        assertEquals("Ehls2", a.getInformedEntity(1).getRouteId());
        assertEquals("Ehls3", a.getInformedEntity(2).getRouteId());
    }
    
    public void testDuplicateAlertMessageAggregation2() {
        String csvData = "1029783000,1029784000,Rail1,Ehls1,2,,Revesby,1,4,www.131500.com.au,Lift Outage,Lift Broken at Revesby\n" +
                         "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls3,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783000,1029784000,Rail1,Ehls2,2,,Revesby,1,4,www.131500.com.au,Lift Outage,Lift Broken at Revesby\n" +
                         "1029783000,1029784000,Rail1,Ehls3,2,,Revesby,1,4,www.131500.com.au,Trip Update,Trip XYZ is late 5 mins\n";
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(3,mesg.getEntityCount());
    }
    
    public void testAggregationAlertComparisonCause() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,2,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(2,mesg.getEntityCount());
    }

    public void testAggregationAlertComparisonEffect() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,3,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(2,mesg.getEntityCount());
    }
    
    public void testAggregationAlertComparisonUrl() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com,Major Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(2,mesg.getEntityCount());
    }
    
    public void testAggregationAlertComparisonHeading() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com.au,Minor Delays,\"Train derailed, at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(2,mesg.getEntityCount());
    }

    public void testAggregationAlertComparisonTextMessage() {
        String csvData = "1029783620,1029784000,Rail1,Ehls1,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed at Revesby\"\n" +
                         "1029783620,1029784000,Rail1,Ehls2,2,101A,stop1,1,4,www.131500.com.au,Major Delays,\"Train derailed, at Revesby\"\n";
        
        StringReader reader = new StringReader(csvData);
        assertTrue(converter.convertAndStoreCsv(reader));
        
        FeedMessage mesg = null;
        try {
            mesg = FeedMessage.parseFrom(converter.getCurrentProtoBuf());
        } catch (InvalidProtocolBufferException e) {
            fail("Invalid Protocol Buffer");
        }
        assertEquals(2,mesg.getEntityCount());
    }
    
}
