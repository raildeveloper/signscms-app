// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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

    /* Spring Injected Transit Bundle Bean */
    private TransitBundle transitBundle;

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.info("GTFS static feed request received : TransitBundleListener : handleRequest() ");

        try {
            log.info("Transit Bundle version : {} served from location {} ", transitBundle.getLatestBundleFileName(),
            transitBundle.getLatestBundleLocation());
            final File bundle = new File(transitBundle.getLatestBundleLocation());
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
