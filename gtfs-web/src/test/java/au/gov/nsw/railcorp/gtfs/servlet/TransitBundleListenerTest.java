// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.gov.nsw.railcorp.gtfs.transitbundle.TransitBundle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.io.IOUnitils;

/**
 * Test Case for TransitBundleListener
 * @author RailCorp
 */
public class TransitBundleListenerTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(TransitBundleListenerTest.class);

    private File testDataBundle;

    @InjectMocks
    TransitBundleListener transitBundleListener;

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        transitBundleListener = new TransitBundleListener();
        final TransitBundle bundle = mock(TransitBundle.class);
        transitBundleListener.setTransitBundle(bundle);
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        final String zipFileName = "SydneyTrainsGTFS_TransitBundle_" + timeStamp + ".zip";
        testDataBundle = IOUnitils.createTempFile(zipFileName);

    }

    @Test
    public void testGTFSBundleDownload() throws ServletException, IOException {

        log.info("testGTFSBundleDownload");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        when(sContext.getContextPath()).thenReturn("SydneyTrainsGTFS");
        when(request.getServletContext()).thenReturn(sContext);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(transitBundleListener.getTransitBundle().getLatestBundleLocation()).thenReturn(testDataBundle.getPath());
        transitBundleListener.handleRequest(request, response);
        verify(response).setContentType("application/zip");
        IOUnitils.deleteTempFileOrDir(testDataBundle);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        transitBundleListener = null;
        if (testDataBundle.exists()) {
            IOUnitils.deleteTempFileOrDir(testDataBundle);
        }
    }

}
