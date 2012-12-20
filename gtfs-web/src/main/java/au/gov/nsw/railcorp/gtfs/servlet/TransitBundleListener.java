// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

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
            if (!bundle.exists()) {
                // Server restart scenario or nothing has been published.
                // See if any previously published bundle exists
                final String publishPath = request.getServletContext().getRealPath("") + File.separator + PUBLISHED_DIRECTORY;
                final File dir = new File(publishPath);
                if (!dir.exists()) {
                    // Nothing published yet.
                    final int responseCode = 404;
                    response.setStatus(responseCode);
                    return;
                } else {
                    // Try getting latest bundle by Last Modified date.
                    final FileFilter fileFilter = new WildcardFileFilter("*.zip");
                    final File[] files = dir.listFiles(fileFilter);
                    Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

                    if (files != null && files.length > 0) {
                        bundle = files[0];
                        if (!bundle.exists()) {
                            // Something really weird.
                            final int responseCode = 404;
                            response.setStatus(responseCode);

                        } else {
                            transitBundle.setLatestBundleLocation(publishPath + bundle.getName());
                            transitBundle.setLatestBundleFileName(bundle.getName());
                        }
                    }

                }

            }
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"SydneyTrainsGTFS_TransitBundle.ZIP\"");

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
