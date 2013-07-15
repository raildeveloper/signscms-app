// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.converter.TripUpdateConverter;
import au.gov.nsw.railcorp.gtfs.dao.TripDao;
import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.helper.H2DatabaseAccess;
import au.gov.nsw.railcorp.gtfs.model.Trip;
import au.gov.nsw.railcorp.gtfs.model.TripStop;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class TransitBundleListener. This class implements a
 * generic listener that receives incoming requests for GTFS Transit Data bundle
 * and serves the zip file.
 */
public class TripUpdateGenerator implements HttpRequestHandler {

    private static final Logger log = LoggerFactory
    .getLogger(TripUpdateGenerator.class);

    private static final String TEMP_DIRECTORY = "tempdb";

    /* Spring Injected Transit Bundle Bean */
    private ActiveTrips generator;

    private TripUpdateConverter protoStorage;

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException
    {

        final String transitBundleUrl = "http://jv7648.virtual.bitcloud.com.au/SydneyTrainsGTFS/GTFSRVehiclePosition";
        final URL bundleUrl = new URL(transitBundleUrl);
        final String useraccount = "sydneytrains:railcorp";
        final String basicAuth = "Basic "
        + javax.xml.bind.DatatypeConverter
        .printBase64Binary(useraccount.getBytes());

        final URLConnection uc = bundleUrl.openConnection();
        uc.setRequestProperty("Authorization", basicAuth);
        uc.setDoOutput(true);
        uc.setDoInput(true);
        uc.setRequestProperty("content-type", "binary/data");
        uc.connect();
        final InputStream inputStream = uc.getInputStream();

        final FeedMessage message = FeedMessage.parseFrom(inputStream);


        try {
            predictTime(message);
            protoStorage.generateTripUpdates();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void predictTime(FeedMessage message) throws SQLException {

        List<FeedEntity> entity = message.getEntityList();
        System.out.println("TripUpdateGenerator.predictTime: got " + entity.size() + " vehicles");
        Iterator<FeedEntity> entityIterator = entity.iterator();
        Map<String, Trip> tripMap = generator.getActiveTripMap();
        List<Trip> trips = new ArrayList<Trip>();

        // Instantiate new hash map for trip ID -> trip associations if none exists
        if (tripMap == null)
            tripMap = new HashMap<String, Trip>();

        try {
            TripDao tripDAO = H2DatabaseAccess.getTripDao();

            while (entityIterator.hasNext()) {
                FeedEntity feedEntity = entityIterator.next();
                if (feedEntity.hasVehicle()) {

                    // Read position/descriptor from GTFSRVehiclePosition feed
                    VehiclePosition vp = feedEntity.getVehicle();
                    TripDescriptor tripDescriptor = vp.getTrip();
                    Long recordedTime = vp.getTimestamp();

                    // System.out.println("Trip : " + tripDescriptor.getTripId()
                    // + " Route : " + tripDescriptor.getRouteId());

                    if (tripDescriptor.getRouteId() != null) {
                        // long startTime = System.currentTimeMillis();
                        // System.out.println("Time start " + startTime);

                        // Attempt to recycle the Trip instance based on tripId,
                        // if unavailable then load from DB
                        Trip trip = tripMap.get(tripDescriptor.getTripId());
                        if (trip == null)
                            trip = tripDAO.findTripWithFollowingTrip(tripDescriptor.getTripId());

                        // long endTime = System.currentTimeMillis();
                        // System.out.println("Time End " + endTime);
                        // System.out.println("Time Taken " + (endTime - startTime));

                        // store timestamp and descriptor for use in GTFSRTripUpdate feed
                        trip.setRecordedTimeStamp(recordedTime);
                        trip.setTripDescriptor(tripDescriptor);

                        // store Trip instance in tracking sets if stops are defined
                        if (trip.hasTripStops()) {
                            trips.add(trip);
                            tripMap.put(trip.getTripId(), trip);

                            // update delay for service
                            trip.calculateDelayForVehicle(vp);

                            // copy delay value to next trip
                            Trip nextTrip = trip.getNextTrip();
                            if (trip.hasValidDelayPrediction() && nextTrip != null) {
                                nextTrip.cascadeDelayFromPreviousTrip(trip);
                                trips.add(nextTrip);
                                System.out.println("TripUpdateGenerator: cascaded delay " + trip.getCurrentDelay() + " => "
                                + nextTrip.getCurrentDelay() + " to next trip " + nextTrip.getTripId());
                                tripMap.put(nextTrip.getTripId(), nextTrip);
                            }
                        }
                    }
                }
            }

            // Remove trips from lookup map that are no longer in the active feed
            Map<String, Trip> tripMapCopy = new HashMap<String, Trip>(tripMap);
            for (Trip trip : tripMapCopy.values()) {
                if (!trips.contains(trip)) {
                    System.out.println("TripUpdateGenerator: invalidated trip " + trip.getTripId());
                    tripMap.remove(trip.getTripId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        generator.setActiveTrips(trips);
        generator.setActiveTripMap(tripMap);
    }

    private List<String> getNextShapePoints(String routeId, String tripId,
    Float lat, Float longt) throws SQLException {

        String query = "SELECT SHAPE_PT_LAT , SHAPE_PT_LON , SHAPE_PT_SEQUENCE  FROM SHAPES WHERE SHAPE_ID =  ? "
        + "AND SHAPE_PT_SEQUENCE > (SELECT SHAPE_PT_SEQUENCE FROM SHAPES WHERE SHAPE_ID = ? AND SHAPE_PT_LAT =  ? "
        + "AND SHAPE_PT_LON = ? )";
        PreparedStatement stmt = null;
        Connection conn = null;
        System.out.println("route Id " + routeId + " lat : " + lat + " long : "
        + longt);
        try {
            conn = H2DatabaseAccess.getDbConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, routeId);
            stmt.setString(2, routeId);
            System.out.println("String lat " + String.valueOf(lat));
            stmt.setString(3, String.valueOf(lat));
            stmt.setString(4, String.valueOf(longt));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String shapeLat = rs.getString("SHAPE_PT_LAT");
                String shapeLong = rs.getString("SHAPE_PT_LON");
                String shapeSeq = rs.getString("SHAPE_PT_SEQUENCE");
                System.out.println("shape Lat : " + shapeLat + " shape longt: "
                + shapeLong + " shapeSeq : " + shapeSeq);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }

    private Trip getTripStoppingPattern(Connection h2Connection, String query,
    String tripId, String currentLat, String currentLong)
    throws SQLException {

        // Connection conn = null;
        PreparedStatement stmt = null;
        stmt = h2Connection.prepareStatement(query);
        Trip trip = new Trip();
        trip.setTripId(tripId);
        try {

            stmt.setString(1, tripId);
            ResultSet rs = stmt.executeQuery();
            List<TripStop> tripStops = new ArrayList<TripStop>();
            while (rs.next()) {
                TripStop stops = new TripStop();
                stops.setArrivalTime(rs.getString("STOP_TIMES.ARRIVAL_TIME"));
                stops.setDepartureTime(rs.getString("STOP_TIMES.DEPARTURE_TIME"));
                stops.setStopId(rs.getString("STOP_TIMES.STOP_ID"));
                stops.setStopSequence(rs.getString("STOP_TIMES.STOP_SEQUENCE"));
                stops.setStopLatitude(rs.getString("STOPS.STOP_LAT"));
                stops.setStopLongt(rs.getString("STOPS.STOP_LON"));
                // long st = System.currentTimeMillis();
                double distance = distance(Double.valueOf(currentLat),
                Double.valueOf(currentLong),
                Double.valueOf(stops.getStopLatitude()),
                Double.valueOf(stops.getStopLongt()), 'K');
                // long et = System.currentTimeMillis();
                // System.out.println("dist time taken " + (et - st));
                stops.setDistanceFromCurrent(distance);
                tripStops.add(stops);
            }
            // Remove stops where train has already departed
            if (tripStops.size() > 0) {
                List<TripStop> activeStops = removeDepartedStops(tripStops);
                trip.setTripStops(activeStops);
            } else {
                trip.setTripStops(tripStops);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }

        }
        return trip;
    }

    private List<TripStop> removeDepartedStops(List<TripStop> stops) {

        List<TripStop> activeStops = new ArrayList<TripStop>();
        TripStop min = Collections.min(stops, new Comparator<TripStop>() {

            @Override
            public int compare(TripStop o1, TripStop o2) {

                Double o1distance = new Double(o1.getDistanceFromCurrent());
                Double o2distance = new Double(o2.getDistanceFromCurrent());
                return o1distance.compareTo(o2distance);
            }

        });
        int minIndex = stops.indexOf(min);
        // System.out.println("min " + minIndex);
        Iterator<TripStop> iterator = stops.iterator();
        while (iterator.hasNext()) {
            TripStop tripStops = iterator.next();
            int index = stops.indexOf(tripStops);
            if (index >= minIndex) {
                activeStops.add(tripStops);
            }
        }
        // System.out.println("Min " + min.toString());
        // activeStops.add(min);
        return activeStops;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2,
    char unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
        + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
        * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {

        return (rad * 180.0 / Math.PI);
    }

    public ActiveTrips getGenerator() {

        return generator;
    }

    public void setGenerator(ActiveTrips generator) {

        this.generator = generator;
    }

    public TripUpdateConverter getProtoStorage() {

        return protoStorage;
    }

    public void setProtoStorage(TripUpdateConverter protoStorage) {

        this.protoStorage = protoStorage;
    }

}
