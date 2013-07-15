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
 * TripDAO.
 * @author Paritosh
 */
public class TripDao {

    private Connection dbConnection;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Connection getDbConnection() {

        return dbConnection;
    }

    public void setDbConnection(Connection dbConn) {

        this.dbConnection = dbConn;
    }

    /**
     * Finds Trip.
     * @param tripId id
     * @return Trip.
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
        + "  ST.STOP_LAT, ST.STOP_LON FROM STOP_TIMES S, STOPS ST "
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
                stops.setStopSequence(rs.getString("STOP_TIMES.STOP_SEQUENCE"));
                stops.setStopLatitude(rs.getString("STOPS.STOP_LAT"));
                stops.setStopLongt(rs.getString("STOPS.STOP_LON"));
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
     * Find Trip with Following Trip.
     * @param tripId
     *            tripId
     * @return Trip
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
     * Next TripId for Block.
     * @param blockId
     *            blockId
     * @param afterTripId
     *            afterTripId
     * @return String trip
     * @throws SQLException
     *             s
     */
    // Given a block_id and trip_id, return the trip_id that follows, or null if none
    public String nextTripIdForBlock(String blockId, String afterTripId) throws SQLException {

        String nextTripId = null;
        // If trip is not blocked, return null
        if (blockId == null || blockId.length() == 0) {
            return nextTripId;
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
                nextTripId = tripId;
                return nextTripId;
            } else if (tripId.equals(afterTripId)) {
                foundId = true;
            }
        }

        // If we made it here, then we didn't find any following trip in the block
        stmt.close();
        return nextTripId;
    }

}
