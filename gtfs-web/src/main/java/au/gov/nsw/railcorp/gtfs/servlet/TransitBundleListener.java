// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class TransitBundleListener.
 * This class implements a generic listener that receives incoming requests for GTFS Transit Data bundle and serves the zip file.
 */
public class TransitBundleListener implements HttpRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(TransitBundleListener.class);

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info("GTFS static feed {} request received of {} bytes",
        request.getServletContext().getServletContextName(),
        request.getContentLength());

    }

}
