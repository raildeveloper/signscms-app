// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.converter.CsvConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;


/**
 * Servlet implementation class CSVListenerServlet This class implements a
 * generic listener that receives CSV input and passes the CSV content to its
 * CSV converter.
 */
public class CsvListenerServlet implements HttpRequestHandler {

    private static final long serialVersionUID = 1L;

    private CsvConverter converter;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor.
     */
    public CsvListenerServlet() {

        super();
    }

    /**
     * getter for serialversionuid.
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {

        return serialVersionUID;
    }

    /**
     * getter for converter.
     * @return the converter
     */
    public CsvConverter getConverter() {

        return converter;
    }

    /**
     * setter for converter.
     * @param val
     *            the converter to set
     */
    public void setConverter(CsvConverter val) {

        this.converter = val;
    }


    /**
     * Handles all requests for the CSV Listener servlet from the PI Database.
     * Expects the CSV content as the request contents
     * {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
    {
        final PrintWriter writer = response.getWriter();
        final BufferedReader reader = request.getReader();
        log.info("CSV Process {} request received of {} bytes",
                request.getServletContext().getServletContextName(),
                request.getContentLength());
        if (converter.convertAndStoreCsv(reader)) {
            writer.append("Successful Update");
        } else {
            writer.append("Failed to CSV processing");
        }
    }

}
