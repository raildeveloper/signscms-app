// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.helper.ActiveTrips;
import au.gov.nsw.railcorp.gtfs.model.Trip;
import au.gov.nsw.railcorp.gtfs.model.TripStop;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet to provide information on current state of trip data
 */
public class TripInfoServlet implements HttpRequestHandler {

    /* Spring Injected Transit Bundle Bean */
    private ActiveTrips generator;

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException {

        // Check request type
        String format = (request.getParameter("format") != null) ? request.getParameter("format") : "html";
        String tripId = request.getParameter("tripId");

        // Prevent browser caching of response
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        if (format.equals("json")) {
            if (tripId != null)
                writeTripJsonForId(tripId, response);
            else
                writeAllTrips(response);
        }
        else {
            request.getRequestDispatcher("/WEB-INF/trip-info.jsp").forward(request, response);
        }
    }

    // Output a JSON payload listing all trips currently operating
    private void writeAllTrips(HttpServletResponse response) throws IOException {

        final PrintWriter writer = response.getWriter();
        response.setContentType("application/json");

        if (generator.getActiveTrips() == null) {
            writer.append("[]");
            return;
        }

        List<String> trips = new ArrayList<String>();
        for (Trip trip : generator.getActiveTrips()) {
            trips.add("{ \"tripId\": \""
            + trip.getTripId()
            + "\""
            +
            (trip.getVehiclePosition() != null ? ", \"label\": \"" + trip.getVehiclePosition().getVehicle().getLabel() + "\"" : "")
            +
            (trip.hasValidDelayPrediction() ? ", \"delay\": \"" + trip.getCurrentDelay() + "\"" : "")
            +
            (trip.getVehiclePosition() != null ? ", \"lat\": " + trip.getVehiclePosition().getPosition().getLatitude() + ", \"lon\": "
            + trip.getVehiclePosition().getPosition().getLongitude() : "") +
            (trip.getCurrentStop() != null ? ", \"currentStop\": \"" + trip.getCurrentStop().getStopId() + "\"" : "") +
            (trip.getNextStop() != null ? ", \"nextStop\": \"" + trip.getNextStop().getStopId() + "\"" : "") +
            " }");
        }
        writer.append("[" + StringUtils.join(trips, ", ") + "]");

    }

    // Output a JSON payload describing the current state of this trip
    private void writeTripJsonForId(String tripId, HttpServletResponse response) throws IOException {

        final PrintWriter writer = response.getWriter();
        response.setContentType("application/json");

        // Retrieve Trip object from active trip storage, throw HTTP 404 if none found
        Trip trip = generator.getActiveTripMap().get(tripId);
        if (trip == null) {
            response.setStatus(404); // file not found
            writer.append("{ tripId: \"" + tripId + "\", error: \"Trip is not currently operating\" }");
            return;
        }

        // Convert stops to JSON
        List<String> stops = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (TripStop stop : trip.getTripStops()) {
            stops.add("{ \"stopId\": " + stop.getStopId() +
            ", \"name\": \"" + stop.getStopName() + "\"" +
            ", \"scheduled\": { \"arr\": \"" + this.formatTime(sdf, stop.getScheduledArrivalTime()) +
            "\", \"dep\": \"" + this.formatTime(sdf, stop.getScheduledDepartureTime()) + "\" } " +
            ", \"predicted\": { \"arr\": \"" + this.formatTime(sdf, stop.getPredictedArrivalTime()) +
            "\", \"dep\": \"" + this.formatTime(sdf, stop.getPredictedDepartureTime()) + "\" } " +
            ", \"actual\": { \"arr\": \"" + this.formatTime(sdf, stop.getActualArrivalTime()) +
            "\", \"dep\": \"" + this.formatTime(sdf, stop.getActualDepartureTime()) + "\" } " +
            "}");
        }

        // Generate Trip JSON payload
        writer.append("{ \"tripId\": \""
        + trip.getTripId()
        + "\""
        +
        (trip.getNextTrip() != null ? ", \"continuesAs\": \"" + trip.getNextTrip().getTripId() + "\"" : "")
        +
        (trip.getVehiclePosition() != null ? ", \"label\": \"" + trip.getVehiclePosition().getVehicle().getLabel() + "\"" : "")
        +
        (trip.hasValidDelayPrediction() ? ", \"delay\": \"" + trip.getCurrentDelay() + "\"" : "")
        +
        (trip.getVehiclePosition() != null ? ", \"lat\": " + trip.getVehiclePosition().getPosition().getLatitude() + ", \"lon\": "
        + trip.getVehiclePosition().getPosition().getLongitude() : "") +
        (trip.getCurrentStop() != null ? ", \"currentStop\": \"" + trip.getCurrentStop().getStopName() + "\"" : "") +
        (trip.getNextStop() != null ? ", \"nextStop\": \"" + trip.getNextStop().getStopName() + "\"" : "") +
        ", \"stops\": [" + StringUtils.join(stops, ", ") + "] }");
    }

    // Quick macro to convert a Date object to HH:mm:ss if non-null, or empty string otherwise
    public String formatTime(SimpleDateFormat sdf, Date time) {

        return time != null ? sdf.format(time) : "";
    }

    public ActiveTrips getGenerator() {

        return generator;
    }

    public void setGenerator(ActiveTrips generator) {

        this.generator = generator;
    }
}
