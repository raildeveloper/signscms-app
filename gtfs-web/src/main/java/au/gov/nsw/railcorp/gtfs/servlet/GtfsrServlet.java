// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.converter.StoredProtocolBufferRetriever;
import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * This class implements the GTFSR that serves protocol buffers
 * of the current GTFS-R feed.
 */
public class GtfsrServlet implements HttpRequestHandler {

    private static final long serialVersionUID = 1L;

    private StoredProtocolBufferRetriever protoStorage;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final long MILLISECOND_IN_SECOND = 1000L;

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
    public StoredProtocolBufferRetriever getProtoStorage() {

        return protoStorage;
    }

    /**
     * Setter for converter.
     * @param val
     *            the converter to set
     */
    public void setProtoStorage(StoredProtocolBufferRetriever val) {

        this.protoStorage = val;
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
        assert protoStorage != null;
        if (request.getParameter("debug") != null) {
            log.info("GTFS-R {} debug request received", request.getServletContext().getServletContextName());
            final PrintWriter writer = response.getWriter();
            writer.append(protoStorage.getCurrentProtoBufDebug());

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(protoStorage.getCurrentProtoBuf());
            long feedGenerationTimeStamp = feed.getHeader().getTimestamp();
            Date feedGenerationDate = new Date(feedGenerationTimeStamp * MILLISECOND_IN_SECOND);
            final SimpleDateFormat headerDateFormat = new SimpleDateFormat(HEADER_DATE_FORMAT);
            headerDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            final String lastModifiedHeader = headerDateFormat.format(feedGenerationDate);
            response.setHeader("Last-Modified", lastModifiedHeader);
        } else {
            log.info("GTFS-R {} binary request received", request.getServletContext().getServletContextName());
            final ServletOutputStream output = response.getOutputStream();
            final byte[] buf = protoStorage.getCurrentProtoBuf();
            if (buf != null) {
                output.write(buf);
            }
        }
    }

}
