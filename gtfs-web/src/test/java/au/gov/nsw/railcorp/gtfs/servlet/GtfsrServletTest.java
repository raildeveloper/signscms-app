// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.gov.nsw.railcorp.gtfs.converter.CsvConverter;
import au.gov.nsw.railcorp.gtfs.servlet.GtfsrServlet;

public class GtfsrServletTest extends TestCase {

 private static final Logger log = LoggerFactory
 .getLogger(GtfsrServletTest.class);

 @InjectMocks
 private GtfsrServlet gtfsServlet;

 @Override
 public void setUp() {
     gtfsServlet = new GtfsrServlet();
     CsvConverter con = mock(CsvConverter.class);
     gtfsServlet.setConverter(con);
 }
 
 @Override
 public void tearDown() {
     gtfsServlet = null;
 }
 
 // Test cases:
 // send out normal binary
 // request debug contents
 // send out null buffer
 // send out null debug contents
 
 @Test
 public void testhandleRequestNormalBuffer() throws IOException, ServletException
 {
     log.info("GtfsrServletTest - testhandleRequestNormalBuffer");
     final HttpServletRequest request = mock(HttpServletRequest.class);
     final HttpServletResponse response = mock(HttpServletResponse.class);
     final ServletContext sContext = mock(ServletContext.class);
     final ServletOutputStream out = mock(ServletOutputStream.class);
     byte[] buf = new byte[] {0xB, 0xC, 0xF, 0x0};
     when(request.getServletContext()).thenReturn(sContext);
     when(sContext.getServletContextName()).thenReturn("TestGTFS-RServlet");
     when(response.getOutputStream()).thenReturn(out);
     when(gtfsServlet.getConverter().getCurrentProtoBuf()).thenReturn(buf);
     
     gtfsServlet.handleRequest(request, response);
     
     verify(gtfsServlet.getConverter()).getCurrentProtoBuf();
     verify(out).write(buf);
 }


 @Test
 public void testHandleRequestDebugContent() throws IOException, ServletException {

     log.info("GtfsrServletTest - testHandleRequestDebugContent");
     final HttpServletRequest request = mock(HttpServletRequest.class);
     final HttpServletResponse response = mock(HttpServletResponse.class);
     final ServletContext sContext = mock(ServletContext.class);
     final PrintWriter print = mock(PrintWriter.class);
     when(request.getServletContext()).thenReturn(sContext);
     when(request.getParameter("debug")).thenReturn("");
     when(sContext.getServletContextName()).thenReturn("TestGTFS-RServlet");
     when(response.getWriter()).thenReturn(print);
     when(gtfsServlet.getConverter().getCurrentProtoBufDebug()).thenReturn("Debug GTFS-R Output");
     
     gtfsServlet.handleRequest(request, response);
     
     verify(gtfsServlet.getConverter()).getCurrentProtoBufDebug();
     verify(print).append("Debug GTFS-R Output");
}
 
 @Test
 public void testHandleRequestNullBuffer() throws IOException, ServletException {

     log.info("GtfsrServletTest - testHandleRequestNullBuffer");
     final HttpServletRequest request = mock(HttpServletRequest.class);
     final HttpServletResponse response = mock(HttpServletResponse.class);
     final ServletContext sContext = mock(ServletContext.class);
     final ServletOutputStream out = mock(ServletOutputStream.class);
     when(request.getServletContext()).thenReturn(sContext);
     when(sContext.getServletContextName()).thenReturn("TestGTFS-RServlet");
     when(response.getOutputStream()).thenReturn(out);
     when(gtfsServlet.getConverter().getCurrentProtoBuf()).thenReturn(null);
     
     gtfsServlet.handleRequest(request, response);
     
     verify(gtfsServlet.getConverter()).getCurrentProtoBuf();
     verify(out, never()).write(null);
     
 }
 
}
