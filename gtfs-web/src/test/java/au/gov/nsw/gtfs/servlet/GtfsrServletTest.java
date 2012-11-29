// RailCorp 2012

package au.gov.nsw.gtfs.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitils.io.IOUnitils;
import org.unitils.thirdparty.org.apache.commons.io.FileUtils;

import au.gov.nsw.railcorp.gtfs.servlet.GtfsrServlet;

public class GtfsrServletTest extends TestCase {

 private static final Logger log = LoggerFactory
 .getLogger(GtfsrServletTest.class);

 @InjectMocks
 private GtfsrServlet gtfsServlet;

 @Test
 public void handleRequestTest() throws IOException, ServletException
 {

  log.info("GtfsrServletTest - handleRequest");
  final HttpServletRequest request = mock(HttpServletRequest.class);
  final HttpServletResponse response = mock(HttpServletResponse.class);
  final File file = IOUnitils.createTempFile("responseFile.txt");
  final PrintWriter writer = new PrintWriter(file);
  when(response.getWriter()).thenReturn(writer);
  gtfsServlet.handleRequest(request, response);
  writer.flush();
  assertTrue(FileUtils.readFileToString(file, "UTF-8").contains(
  "Hello"));
 }

}
