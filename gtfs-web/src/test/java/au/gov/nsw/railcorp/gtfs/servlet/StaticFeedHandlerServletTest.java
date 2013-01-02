// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Case for StaticFeedHandlerServlet
 * @author RailCorp
 */
public class StaticFeedHandlerServletTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(StaticFeedHandlerServletTest.class);

    @InjectMocks
    StaticFeedHandlerServlet feedHandlerServlet;

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        feedHandlerServlet = new StaticFeedHandlerServlet();
        final TransitBundle bundle = mock(TransitBundle.class);
        feedHandlerServlet.setTransitBundle(bundle);
    }

    @Test
    public void testGTFSDataUploadRequest() throws ServletException, IOException, FileUploadException {

        log.info("testGTFSDataUploadRequest");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final DiskFileItemFactory factory = mock(DiskFileItemFactory.class);
        final ServletFileUpload upload = new ServletFileUpload(factory);

        when(sContext.getContextPath()).thenReturn("SydneyTrainsGTFS");
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getMethod()).thenReturn("post");
        feedHandlerServlet.handleRequest(request, response);
        verify(request).getContentType();

    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        feedHandlerServlet = null;

    }

}
