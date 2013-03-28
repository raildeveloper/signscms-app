// RailCorp 2012
package au.gov.nsw.railcorp.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.gov.nsw.railcorp.gtfs.converter.GeneralProtocolBufferConverter;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class ProtoListenerServletTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(CSVListenerServletTest.class);
    @InjectMocks
    ProtoListenerServlet protoServlet;
    
    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    protected void setUp() throws Exception {

        super.setUp();
        protoServlet = new ProtoListenerServlet();
        final GeneralProtocolBufferConverter con = mock(GeneralProtocolBufferConverter.class);
        protoServlet.setConverter(con);
    }

    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {

        super.tearDown();
        protoServlet = null;
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
        final String inputString = "Testing CSV Content";
        final ServletInputStream in = mock(ServletInputStream.class); //new ByteArrayInputStream(inputString.getBytes());
        when(protoServlet.getConverter().storeProtocolBuffer(in)).thenReturn(true);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter print = mock(PrintWriter.class);
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getContentLength()).thenReturn(inputString.length());
        when(request.getInputStream()).thenReturn(in);
        when(sContext.getServletContextName()).thenReturn("TestCSVListener");
        when(response.getWriter()).thenReturn(print);
        
        protoServlet.handleRequest(request, response);
        verify(protoServlet.getConverter()).storeProtocolBuffer(in);
        verify(print).append("Successful Update");
    }

    @Test
    public void testHandleRequestFailedConvert() throws IOException, ServletException {

        log.info("testHandleRequestFailedConvert");
        final String inputString = "Testing Failed CSV Content";
        final ServletInputStream in = mock(ServletInputStream.class); //new ByteArrayInputStream(inputString.getBytes());
        when(protoServlet.getConverter().storeProtocolBuffer(in)).thenReturn(false);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext sContext = mock(ServletContext.class);
        final PrintWriter print = mock(PrintWriter.class);
        when(request.getServletContext()).thenReturn(sContext);
        when(request.getContentLength()).thenReturn(inputString.length());
        when(request.getInputStream()).thenReturn(in);
        when(sContext.getServletContextName()).thenReturn("TestCSVListener");
        when(response.getWriter()).thenReturn(print);
        
        protoServlet.handleRequest(request, response);
        verify(protoServlet.getConverter()).storeProtocolBuffer(in);
        verify(print).append("Failed Protocol Buffer validation processing");
    }
    
}
