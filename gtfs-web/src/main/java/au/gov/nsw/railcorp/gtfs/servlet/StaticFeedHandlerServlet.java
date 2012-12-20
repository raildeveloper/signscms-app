// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class StaticFeedHandlerServlet This class implements a
 * generic listener that receives CSV input for GTFS static data. Zips and publish the Transit Data bundle.
 */
public class StaticFeedHandlerServlet implements HttpRequestHandler {

    /* Files to be uploaded at the following location. */
    private static final String TEMP_DIRECTORY = "temp";

    private static final String PUBLISHED_DIRECTORY = "published";

    private static final Logger log = LoggerFactory.getLogger(StaticFeedHandlerServlet.class);

    /* Spring Injected Transit Bundle Bean */
    private TransitBundle transitBundle;

    /**
     * Handles GTFS Static Data request from PI Database.
     * Expects the CSV content as the request contents {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final List<String> uploadedFiles = new ArrayList<String>();
        try {
            log.info("Static Feed Upload request received : StaticFeedHandlerServlet : handleRequest()");
            if (!ServletFileUpload.isMultipartContent(request)) {
                // if not, we stop here
                log.info("HTTP Request received is not multipart/form-data");
                return;
            }
            log.info("Started reading files received via HTTP Post {}",
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            // Save the files received to java.io.tmpdir
            final DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            final ServletFileUpload upload = new ServletFileUpload(factory);

            // construct directory path to store upload file
            final String tempUploadPath = request.getServletContext().getRealPath("") + File.separator + TEMP_DIRECTORY;
            final String publishPath = request.getServletContext().getRealPath("") + File.separator + PUBLISHED_DIRECTORY;
            // create the directory if it doesn't exists
            final File tempDir = new File(tempUploadPath);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            final List formItems = upload.parseRequest(request);
            final Iterator iter = formItems.iterator();

            // iterates over form's fields
            while (iter.hasNext()) {
                final FileItem item = (FileItem) iter.next();
                // Process only fields that are not form fields

                if (!item.isFormField()) {
                    final String fileName = new File(item.getName()).getName();
                    final String filePath = tempUploadPath + File.separator + fileName;
                    final File sFile = new File(filePath);
                    // Saves the file on disk
                    item.write(sFile);
                    uploadedFiles.add(filePath);
                    log.info("File {} received of {} bytes",
                    sFile.getName(), sFile.length());
                }
            }
            log.info("Finished reading files received via HTTP Post {}",
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            // Files Uploaded - Create the Zip bundle
            createGtfsBundle(publishPath, uploadedFiles);

            // Bundle Created - Delete the temp files
            FileUtils.deleteDirectory(tempDir);

            final int responseCode = 200;
            response.setStatus(responseCode);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * This method create the GTFS Bundle.
     * @param uploadedFiles
     */
    private void createGtfsBundle(String publishPath, List<String> uploadedFiles) {

        try {
            log.info("Bundle Creation started {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            final File publishDir = new File(publishPath);
            if (!publishDir.exists()) {
                publishDir.mkdir();
            }
            // timeStamp - Appended to File Name - example - 20121219_135813
            final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            final String fileName = "SydneyTrainsGTFS_TransitBundle_" + timeStamp + ".zip";

            // Need to correct the upload path
            final String fileN = publishPath + File.separator + fileName;
            final File transitGtfsBundle = new File(fileN);
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(transitGtfsBundle);
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            int bytesRead;
            final int bytesToRead = 1024;
            final byte[] buffer = new byte[bytesToRead];
            for (String file : uploadedFiles) {
                final File f = new File(file);
                if (!f.exists()) {
                    log.info("Couldn't find file " + file);
                    continue;
                }
                final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(f));
                final ZipEntry entry = new ZipEntry(f.getName());
                entry.setMethod(ZipEntry.DEFLATED);
                zipOutputStream.putNextEntry(entry);
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            }
            zipOutputStream.close();
            outputStream.close();
            transitBundle.setLatestBundleFileName(fileName);
            transitBundle.setLatestBundleLocation(fileN);
            log.info("Bundle Creation finished {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            log.info("Successfully created transit bundle at " + fileN);

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    /**
     * Get the Transit Data Bundle.
     * @return the transitBundle
     */
    public TransitBundle getTransitBundle() {

        return transitBundle;
    }

    /**
     * Set the transit data bundle.
     * @param bundle
     *            the transitBundle to set
     */
    public void setTransitBundle(TransitBundle bundle) {

        this.transitBundle = bundle;
    }
}
