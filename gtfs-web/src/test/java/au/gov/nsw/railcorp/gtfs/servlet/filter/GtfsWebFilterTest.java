// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.io.IOUnitils;
import org.unitils.thirdparty.org.apache.commons.io.FileUtils;

/**
 * Test Case for Web Filter.
 * @author RailCorp
 */
public class GtfsWebFilterTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(GtfsWebFilterTest.class);

    @InjectMocks
    private GtfsWebFilter webFilter = new GtfsWebFilter();

    @Test
    public void testValidRequest() {

        log.info("GtfsWebFilterTest : testValidRequest");
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final MockFilterChain filterChain = new MockFilterChain();
        filterChain.setExpectedInvocation(true);
        try {
            when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/GTFSRVehiclePosition"));
            webFilter.doFilter(httpServletRequest, response, filterChain);
            filterChain.verify();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testInvalidRequest() {

        log.info("GtfsWebFilterTest : testInvalidRequest");
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);
        final File file = IOUnitils.createTempFile("responseFile.txt");
        try {
            final PrintWriter writer = new PrintWriter(file);
            when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/Invalid"));
            when(response.getWriter()).thenReturn(writer);
            webFilter.doFilter(httpServletRequest, response, filterChain);
            writer.flush();
            writer.close();
            assertTrue(FileUtils.readFileToString(file, "UTF-8").contains("Invalid Request"));
            IOUnitils.deleteTempFileOrDir(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }

    }

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
    }

}
