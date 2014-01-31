// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.dao;

import au.gov.nsw.railcorp.gtfs.model.Trip;
import au.gov.nsw.railcorp.gtfs.model.TripStop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TripDao.
 * @author paritosh
 */
public class TripDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Connection dbConnection;

    public Connection getDbConnection() {

        return dbConnection;
    }

    public void setDbConnection(Connection dbConn) {

        this.dbConnection = dbConn;
    }

    /**
     * findTrip.
     * @param tripId
     *            tripid
     * @return trip
     * @throws SQLException
     *             s
     */
    // Retrieve a Trip object from h2 based on trip_id
    public Trip findTrip(String tripId) throws SQLException {

        // Instantiate Trip object
        final Trip trip = new Trip();
        trip.setTripId(tripId);

        // Retrieve trip data
        final String tripQuery = "SELECT ROUTE_ID, SERVICE_ID, TRIP_ID, TRIP_HEADSIGN, DIRECTION_ID, "
        + "BLOCK_ID, SHAPE_ID FROM TRIPS WHERE TRIP_ID = ?";
        final PreparedStatement tripStmt = dbConnection.prepareStatement(tripQuery);
        try {
            tripStmt.setString(1, tripId);
            final ResultSet rs = tripStmt.executeQuery();
            while (rs.next()) {
                trip.setRouteId(rs.getString("ROUTE_ID"));
                trip.setServiceId(rs.getString("SERVICE_ID"));
                trip.setHeadsign(rs.getString("TRIP_HEADSIGN"));
                trip.setDirectionId(rs.getInt("DIRECTION_ID"));
                trip.setBlockId(rs.getString("BLOCK_ID"));
                trip.setShapeId(rs.getString("SHAPE_ID"));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (tripStmt != null) {
                tripStmt.close();
            }
        }

        // Retrieve stops for trip
        final String stopQuery = "SELECT S.ARRIVAL_TIME, S.DEPARTURE_TIME, S.STOP_SEQUENCE, S.STOP_ID, "
        + "  ST.STOP_LAT, ST.STOP_LON, ST.STOP_NAME "
        + "FROM STOP_TIMES S, STOPS ST "
        + "WHERE TRIP_ID = ? AND S.STOP_ID = ST.STOP_ID "
        + "ORDER BY STOP_SEQUENCE ";
        final PreparedStatement stopStmt = dbConnection.prepareStatement(stopQuery);
        try {

            stopStmt.setString(1, tripId);
            final ResultSet rs = stopStmt.executeQuery();
            final List<TripStop> tripStops = new ArrayList<TripStop>();
            while (rs.next()) {
                final TripStop stops = new TripStop();
                stops.setArrivalTime(rs.getString("STOP_TIMES.ARRIVAL_TIME"));
                stops.setDepartureTime(rs.getString("STOP_TIMES.DEPARTURE_TIME"));
                stops.setStopId(rs.getString("STOP_TIMES.STOP_ID"));
                stops.setStopSequence(rs.getInt("STOP_TIMES.STOP_SEQUENCE"));
                stops.setStopLatitude(rs.getString("STOPS.STOP_LAT"));
                stops.setStopLongt(rs.getString("STOPS.STOP_LON"));
                stops.setStopName(rs.getString("STOPS.STOP_NAME"));
                tripStops.add(stops);
            }

            if (tripStops.size() > 0) {
                trip.setTripStops(tripStops);
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (stopStmt != null) {
                stopStmt.close();
            }
        }

        return trip;
    }

    /**
     * findTripWithFollowingTrip.
     * @param tripId
     *            tripid
     * @return trip
     * @throws SQLException
     *             s
     */
    // Given a specified trip_id load that trip and set trip.nextTrip the next in the block
    public Trip findTripWithFollowingTrip(String tripId) throws SQLException {

        final Trip trip = this.findTrip(tripId);
        if (trip != null) {
            final String nextTripId = this.nextTripIdForBlock(trip.getBlockId(), tripId);
            if (nextTripId != null) {
                final Trip nextTrip = this.findTrip(nextTripId);
                trip.setNextTrip(nextTrip);
            }
        }
        return trip;
    }

    /**
     * nextTripIdForBlock.
     * @param blockId
     *            blockid
     * @param afterTripId
     *            aftertripid
     * @return string
     * @throws SQLException
     *             s
     */
    // Given a block_id and trip_id, return the trip_id that follows, or null if none
    public String nextTripIdForBlock(String blockId, String afterTripId) throws SQLException {

        // If trip is not blocked, return null
        if (blockId == null || blockId.length() == 0) {
            return null;
        }

        // Query database for all trips in this block sorted by ascending starting time
        final String blockQuery = "select t.trip_id, min(st.departure_time) "
        + "from trips t inner join stop_times st on t.trip_id = st.trip_id "
        + "where t.block_id = ? group by t.trip_id "
        + "order by min(st.departure_time)";
        final PreparedStatement stmt = dbConnection.prepareStatement(blockQuery);
        stmt.setString(1, blockId);
        final ResultSet rs = stmt.executeQuery();

        // Loop over results to find trip_id immediately following afterTripId
        boolean foundId = afterTripId == null;
        while (rs.next()) {
            final String tripId = rs.getString("trip_id");
            if (foundId) {
                stmt.close();
                return tripId;
            } else if (tripId.equals(afterTripId)) {
                foundId = true;
            }
        }

        // If we made it here, then we didn't find any following trip in the block
        stmt.close();
        return null;
    }

    /**
     * Get StopName for StopId.
     * @param stopId
     *            StopId to search.
     * @return stopName
     * @throws SQLException
     *             s
     */
    public String getStopNameForStopId(String stopId) throws SQLException {

        String stopName = null;
        PreparedStatement stopStmt = null;
        if (stopId != null) {
            // Query database for StopName
            try {
                final String stopQuery = "SELECT STOP_NAME FROM STOPS WHERE STOP_ID = ?";
                stopStmt = dbConnection.prepareStatement(stopQuery);
                stopStmt.setString(1, stopId);
                final ResultSet rs = stopStmt.executeQuery();

                while (rs.next()) {
                    stopName = rs.getString("STOP_NAME");
                }

            } catch (SQLException s) {
                log.debug(s.getMessage());
            } finally {
                if (stopStmt != null) {
                    stopStmt.close();
                }
            }
        }
        return stopName;

    }

    /**
     * Get Lat and Long for given stop Id.
     * @param tripStop
     *            tripStop.
     * @param stopId
     *            stopId.
     * @throws SQLException
     *             s
     */
    public void getGtfsStopsDetails(TripStop tripStop, String stopId) throws SQLException {

        PreparedStatement stopStmt = null;

        final String stopQuery = "SELECT STOP_NAME, STOP_LAT, STOP_LON FROM STOPS WHERE STOP_ID = ?";
        try {
            stopStmt = dbConnection.prepareStatement(stopQuery);
            stopStmt.setString(1, stopId);
            final ResultSet rs = stopStmt.executeQuery();

            while (rs.next()) {
                tripStop.setStopName((rs.getString("STOP_NAME") != null) ? rs.getString("STOP_NAME") : "");
                tripStop.setStopLatitude((rs.getString("STOP_LAT") != null) ? rs.getString("STOP_LAT") : "");
                tripStop.setStopLongt((rs.getString("STOP_LON") != null) ? rs.getString("STOP_LON") : "");

            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (stopStmt != null) {
                stopStmt.close();
            }
        }

    }
}
