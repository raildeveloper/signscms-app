// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Greeter.
 */
@WebServlet("/RailCorp/gtfsr")
public class Greeter extends HttpServlet {

 private static final long serialVersionUID = 1L;

 /**
  * Default constructor.
  */
 public Greeter() {

  // TODO Auto-generated constructor stub
 }

 /**
  * Servlet Init.
  * @see Servlet#init(ServletConfig)
  * @param config
  *         - Config.
  * @throws ServletException
  *          - Throws Servlet Exception.
  */
 public void init(ServletConfig config) throws ServletException {

  super.init(config);
 }

 /**
  * Servlet doGet method.
  * @param request
  *         - Request.
  * @param response
  *         - Response.
  * @throws ServletException
  *          - Throws Servlet Exception
  * @throws IOException
  *          - Throws IOException.
  */
 protected void doGet(HttpServletRequest request,
 HttpServletResponse response) throws ServletException, IOException
 {

  doPost(request, response);
 }

 /**
  * Servlet doPost Method.
  * @param request
  *         - Request.
  * @param response
  *         - Response.
  * @throws ServletException
  *          - Throws Servlet Exception
  * @throws IOException
  *          - Throws IOException.
  */
 protected void doPost(HttpServletRequest request,
 HttpServletResponse response) throws ServletException, IOException
 {

  final String name = request.getParameter("name");
  final String welcomeMsg = "Hello " + name;
  response.setContentType("text/html");
  final PrintWriter writer = response.getWriter();
  writer.append("<html>");
  writer.append("<head>");
  writer.append("<title>");
  writer.append("Hello " + name);
  writer.append("</title>");
  writer.append("</head>");
  writer.append("<body>");
  writer.append("<h1>" + welcomeMsg + "</h1>");
  writer.append("</body>");
  writer.append("</html>");
 }

}
