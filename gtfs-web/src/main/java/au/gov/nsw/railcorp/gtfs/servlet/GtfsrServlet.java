// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.converter.CsvConverter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * This class implements the GTFSR that serves protocol buffers
 * of the current GTFS-R feed.
 */
public class GtfsrServlet implements HttpRequestHandler {

    private static final long serialVersionUID = 1L;

    private CsvConverter converter;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor.
     */
    public GtfsrServlet() {

        super();
    }

    /**
     * Gets the serialversionuid.
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {

        return serialVersionUID;
    }

    /**
     * Gets the converter.
     * @return the converter
     */
    public CsvConverter getConverter() {

        return converter;
    }

    /**
     * Setter for converter.
     * @param val
     *            the converter to set
     */
    public void setConverter(CsvConverter val) {

        this.converter = val;
    }


    /**
     * Handles all requests from App Developers for the GTFS-R feed.
     * A single parameter 'debug' can be passed to return the debug text representing the protocol buffer rather
     * than the binary protocol buffer
     * {@inheritDoc}
     * @see org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        assert converter != null;
        if (request.getParameter("debug") != null) {
            log.info("GTFS-R {} debug request received", request.getServletContext().getServletContextName());
            final PrintWriter writer = response.getWriter();
            writer.append(converter.getCurrentProtoBufDebug());
        } else {
            log.info("GTFS-R {} binary request received", request.getServletContext().getServletContextName());
            final ServletOutputStream output = response.getOutputStream();
            final byte[] buf = converter.getCurrentProtoBuf();
            if (buf != null) {
                output.write(buf);
            }
        }
    }

}
