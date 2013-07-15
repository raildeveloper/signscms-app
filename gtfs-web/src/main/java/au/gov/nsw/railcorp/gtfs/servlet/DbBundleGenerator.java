// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.helper.PublishBundle;
import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class TransitBundleListener. This class implements a
 * generic listener that receives incoming requests for GTFS Transit Data bundle
 * and serves the zip file.
 */
public class DbBundleGenerator implements HttpRequestHandler {

    private static final Logger log = LoggerFactory
    .getLogger(DbBundleGenerator.class);

    private static final String TEMP_DIRECTORY = "tempdb";

    /* Spring Injected Transit Bundle Bean */
    private TransitBundle transitBundle;

    /**
     * Handles GTFS Static Data request from App Developers. {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException
    {

        // Download the latest bundle
        try {

            final String transitBundleUrl = "http://jv7648.virtual.bitcloud.com.au/SydneyTrainsGTFS/GTFSTransitBundle";
            final URL bundleUrl = new URL(transitBundleUrl);
            final String useraccount = "sydneytrains:railcorp";
            final String basicAuth = "Basic "
            + javax.xml.bind.DatatypeConverter
            .printBase64Binary(useraccount.getBytes());

            final URLConnection uc = bundleUrl.openConnection();
            uc.setRequestProperty("Authorization", basicAuth);
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setRequestProperty("content-type", "binary/data");
            uc.connect();
            final InputStream inputStream = uc.getInputStream();
            final int fileSize = uc.getContentLength();
            final String tempUploadPath = request.getServletContext()
            .getRealPath("") + File.separator + TEMP_DIRECTORY;

            final File zipFile = new File(tempUploadPath);
            if (!zipFile.exists()) {
                zipFile.mkdirs();
            }
            final File bundle = new File(tempUploadPath + File.separator
            + "SydneyTrainsGTFS_TransitBundle.zip");
            if (bundle.exists()) {
                bundle.delete();
            }
            bundle.createNewFile();
            final FileOutputStream outputStream = new FileOutputStream(bundle, true);
            final int bytesToRead = 1024;
            final byte[] b = new byte[bytesToRead];
            int count;

            while ((count = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, count);
            }
            outputStream.close();
            inputStream.close();

            // Bundle downloaded - read csv files out of it and dump them in h2
            // database
            final PublishBundle publishBundle = new PublishBundle();
            publishBundle.publishBundleH2db(bundle);

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
