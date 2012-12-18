// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

/**
 * Servlet implementation class StaticFeedHandlerServlet This class implements a
 * generic listener that receives CSV input for GTFS static data. Zips and publish the Transit Data bundle.
 */
public class StaticFeedHandlerServlet implements HttpRequestHandler {

    /* Files to be uploaded at the following location. */
    private static final String UPLOAD_DIRECTORY = "upload";

    private static final Logger log = LoggerFactory.getLogger(StaticFeedHandlerServlet.class);

    /**
     * Handles GTFS Static Data request from PI Database.
     * Expects the CSV content as the request contents {@inheritDoc}
     * @see HttpRequestHandler#handleRequest(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            if (!ServletFileUpload.isMultipartContent(request)) {
                // if not, we stop here
                return;
            }
            final DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            final ServletFileUpload upload = new ServletFileUpload(factory);

            // construct directory path to store upload file
            final String uploadPath = request.getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;

            // create the directory if it doesn't exists
            final File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            final List formItems = upload.parseRequest(request);
            final Iterator iter = formItems.iterator();

            // iterates over form's fields
            while (iter.hasNext()) {
                final FileItem item = (FileItem) iter.next();
                // Process only fields that are not form fields

                if (!item.isFormField()) {
                    final String fileName = new File(item.getName()).getName();
                    final String filePath = uploadPath + File.separator + fileName;
                    final File sFile = new File(filePath);
                    // Saves the file on disk
                    item.write(sFile);

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
