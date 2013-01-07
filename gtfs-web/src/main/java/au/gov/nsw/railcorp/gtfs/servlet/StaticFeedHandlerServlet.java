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

    /* Response to be given if request successfully processed */
    private static final int REQUEST_SUCCESS_CODE = 200;

    /* Response to be given if the request processing encountered any errors - Bad Request */
    private static final int BAD_REQUEST_CODE = 400;

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

        // construct directory path to store upload file
        // Temp Path for all uploaded files - This shall be deleted once request is complete.
        final String tempUploadPath = request.getServletContext().getRealPath("") + File.separator + TEMP_DIRECTORY;

        // Publish path to store all the Published GTFS bundles
        final String publishPath = request.getServletContext().getRealPath("") + File.separator + PUBLISHED_DIRECTORY;

        // create the directory if it doesn't exists
        final File tempDir = new File(tempUploadPath);
        final List<String> uploadedFiles = new ArrayList<String>();
        try {
            log.info("Static Feed Upload request received : StaticFeedHandlerServlet : handleRequest()");
            if (!ServletFileUpload.isMultipartContent(request)) {
                // if not, we stop here
                log.info("HTTP Request received is not multipart/form-data");
                response.setStatus(BAD_REQUEST_CODE);
                return;
            }
            log.info("Started reading files received via HTTP Post {}",
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            // Save the files received to java.io.tmpdir
            final DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            final ServletFileUpload upload = new ServletFileUpload(factory);

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
            if (uploadedFiles != null && uploadedFiles.size() > 0) {
                createGtfsBundle(publishPath, uploadedFiles);
            } else {
                // Error Condition
                response.setStatus(BAD_REQUEST_CODE);
                log.error("Files not read, some error ");
            }

            response.setStatus(REQUEST_SUCCESS_CODE);
        } catch (Exception e) {
            response.setStatus(BAD_REQUEST_CODE);
            log.error(e.getMessage());
        } finally {
            // Bundle Created - Delete the temp files
            FileUtils.deleteDirectory(tempDir);
        }
    }

    /**
     * This method create the GTFS Bundle.
     * @param uploadedFiles
     * @throws IOException
     */
    private void createGtfsBundle(String publishPath, List<String> uploadedFiles) throws IOException {

        try {
            log.info("Bundle Creation started {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            final File publishDir = new File(publishPath);
            if (!publishDir.exists()) {
                publishDir.mkdir();
            }
            // timeStamp - Appended to File Name - example - 20121219_135813
            final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            final String gtfsBundlePublishFileName = "SydneyTrainsGTFS_TransitBundle_" + timeStamp + ".zip";

            // Need to correct the upload path
            final String gtfsBundlePublishFileLocation = publishPath + File.separator + gtfsBundlePublishFileName;
            final File transitGtfsBundle = new File(gtfsBundlePublishFileLocation);
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(transitGtfsBundle);
            final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            int bytesRead;
            final int bytesToRead = 1024;
            final byte[] buffer = new byte[bytesToRead];
            for (String file : uploadedFiles) {
                final File uploadedFile = new File(file);
                if (!uploadedFile.exists()) {
                    log
                    .info(
                    "GTFS Bundle Creation Error - File there in uploaded list but couldn't find file at Temp Location {} - "
                    + "Continue with the rest of files", file);
                    continue;
                }
                final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(uploadedFile));
                final ZipEntry entry = new ZipEntry(uploadedFile.getName());
                entry.setMethod(ZipEntry.DEFLATED);
                zipOutputStream.putNextEntry(entry);
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            }
            zipOutputStream.close();
            outputStream.close();
            transitBundle.setLatestBundleFileName(gtfsBundlePublishFileName);
            transitBundle.setLatestBundleLocation(gtfsBundlePublishFileLocation);
            log.info("Bundle Creation finished {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            log.info("Successfully created transit bundle at " + gtfsBundlePublishFileLocation);

        } catch (FileNotFoundException e) {
            log
            .error("Bundle Creation Failure at {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            log.error(e.getMessage());
            throw new IOException(e);
        } catch (IOException e) {
            log
            .error("Bundle Creation Failure at {}", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            log.error(e.getMessage());
            throw new IOException(e);
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
