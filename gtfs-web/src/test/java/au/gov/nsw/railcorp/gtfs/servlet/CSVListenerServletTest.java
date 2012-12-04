// RailCorp 2012
package au.gov.nsw.railcorp.gtfs.servlet;


import au.gov.nsw.railcorp.gtfs.converter.CsvConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;


/**
 * @author John
 *
 */
public class CSVListenerServletTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(CSVListenerServletTest.class);
    @InjectMocks
    CsvListenerServlet csvServlet;
    
    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        csvServlet = new CsvListenerServlet();
        final CsvConverter con = mock(CsvConverter.class);
        csvServlet.setConverter(con);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        csvServlet = null;
    }

    // Servlet Test Cases:
    // pass CSV in request, verify recieved by converter
    // verify 'failed conversion' returns failed message
    // verify successful conversion returns success message
    
    /**
     * Test method for {@link au.gov.nsw.railcorp.gtfs.servlet.CsvListenerServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     * @throws IOException 
     * @throws ServletException 
     */
    @Test
    public void testHandleRequestNormal() throws ServletException, IOException {

        log.info("testHandleRequestNormal");
        BufferedReader bufReader = new BufferedReader(new StringReader("Testing CSV Content"));
        when(csvServlet.getConverter().convertAndStoreCsv(bufReader)).thenReturn(true);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter print = mock(PrintWriter.class);
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getContentLength()).thenReturn(bufReader.readLine().length());
        when(request.getReader()).thenReturn(bufReader);
        when(sContext.getServletContextName()).thenReturn("TestCSVListener");
        when(response.getWriter()).thenReturn(print);
        
        csvServlet.handleRequest(request, response);
        verify(csvServlet.getConverter()).convertAndStoreCsv(bufReader);
        verify(print).append("Successful Update");
    }

    @Test
    public void testHandleRequestFailedConvert() throws IOException, ServletException {

        log.info("testHandleRequestFailedConvert");
        BufferedReader bufReader = new BufferedReader(new StringReader("Testing Failed CSV Content"));
        when(csvServlet.getConverter().convertAndStoreCsv(bufReader)).thenReturn(false);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter print = mock(PrintWriter.class);
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getContentLength()).thenReturn(bufReader.readLine().length());
        when(request.getReader()).thenReturn(bufReader);
        when(sContext.getServletContextName()).thenReturn("TestCSVListener");
        when(response.getWriter()).thenReturn(print);
        
        csvServlet.handleRequest(request, response);
        verify(csvServlet.getConverter()).convertAndStoreCsv(bufReader);
        verify(print).append("Failed to CSV processing");
    }
}
