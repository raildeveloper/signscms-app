// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class TransitBundleListener.
 * This class implements a generic listener that receives incoming requests for GTFS Transit Data bundle and serves the zip file.
 */
public class TransitBundleListener implements HttpRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(TransitBundleListener.class);

    private static final String PUBLISHED_DIRECTORY = "published";

    private static final int NOT_MODIFIED_STATUS_CODE = 304;

    private static final int ERROR_STATUS_CODE = 404;

    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    private static final String FILE_DATE_FORMAT = "yyyyMMdd_HHmmss";

    private static final String HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String RESPONSE_CONTENT_TYPE = "application/zip";

    private static final String FILE_WILD_CARD_FILTER = "*.zip";

    /* Spring Injected Transit Bundle Bean */
    private TransitBundle transitBundle;

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info("GTFS static feed request received : TransitBundleListener : handleRequest() ");

        try {

            log.info("Transit Bundle version : {} served from location {} ", transitBundle.getLatestBundleFileName(),
            transitBundle.getLatestBundleLocation());
            File bundle = new File(transitBundle.getLatestBundleLocation());
            final SimpleDateFormat fileDateFormat = new SimpleDateFormat(FILE_DATE_FORMAT);
            fileDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            if (!bundle.exists()) {
                // Server restart scenario or nothing has been published.
                // See if any previously published bundle exists
                final String publishPath = request.getServletContext().getRealPath("") + File.separator + PUBLISHED_DIRECTORY;
                final File dir = new File(publishPath);
                if (!dir.exists()) {
                    // Nothing published yet.
                    response.setStatus(ERROR_STATUS_CODE);
                    return;
                } else {
                    // Try getting latest bundle by Last Modified date.
                    final FileFilter fileFilter = new WildcardFileFilter(FILE_WILD_CARD_FILTER);
                    final File[] files = dir.listFiles(fileFilter);
                    Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

                    if (files != null && files.length > 0) {
                        bundle = files[0];
                        if (!bundle.exists()) {
                            // Something really weird.
                            response.setStatus(ERROR_STATUS_CODE);

                        } else {
                            transitBundle.setLatestBundleLocation(publishPath + bundle.getName());
                            transitBundle.setLatestBundleFileName(bundle.getName());
                            final Date lastModified = new Date(bundle.lastModified());

                            final String generationTime = fileDateFormat.format(lastModified);
                            transitBundle.setBundleGenerationTime(generationTime);
                        }
                    } else {
                        // Something really weird - publish directory exists but nothing published
                        final int responseCode = 404;
                        response.setStatus(responseCode);
                        return;
                    }

                }

            }

            // Verify in Request Header for Header Name - If-Modified-Since - App developers may not want to download everytime.
            final Date bundleGenerationDate = fileDateFormat.parse(transitBundle.getBundleGenerationTime());

            final String ifModifiedSince = request.getHeader(IF_MODIFIED_SINCE);
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                try {

                    final Date ifModifiedSinceDate = fileDateFormat.parse(ifModifiedSince);

                    if (bundleGenerationDate.before(ifModifiedSinceDate) || bundleGenerationDate.equals(ifModifiedSinceDate)) {
                        response.setStatus(NOT_MODIFIED_STATUS_CODE);
                        return;
                    }
                } catch (ParseException e) {
                    log.error("ERROR Parsing request header - If-Modified-Since  {} - Message - ", ifModifiedSince, e.getMessage());
                }
            }

            response.setContentType(RESPONSE_CONTENT_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=\"SydneyTrainsGTFS_TransitBundle.zip\"");
            final SimpleDateFormat headerDateFormat = new SimpleDateFormat(HEADER_DATE_FORMAT);
            headerDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            final String lastModifiedHeader = headerDateFormat.format(bundleGenerationDate);
            response.setHeader("Last-Modified", lastModifiedHeader);
            final FileInputStream fis = new FileInputStream(bundle);
            final BufferedInputStream inputStream = new BufferedInputStream(fis);
            final ServletOutputStream servletOutputStream = response.getOutputStream();
            final int bytesToRead = 2048;
            final byte[] bytes = new byte[bytesToRead];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                servletOutputStream.write(bytes, 0, bytesRead);
            }
            servletOutputStream.flush();
            servletOutputStream.close();
            inputStream.close();
            fis.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Get the Transit Bundle.
     * @return the transitBundle
     */
    public TransitBundle getTransitBundle() {

        return transitBundle;
    }

    /**
     * Set the Transit Bundle.
     * @param bundle
     *            the transitBundle to set
     */
    public void setTransitBundle(TransitBundle bundle) {

        this.transitBundle = bundle;
    }

}
