// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter Implementation Class This class web filter which verifies
 * incoming request.
 */
public class GtfsWebFilter implements Filter {

    /** for logging. */
    private static final Logger log = LoggerFactory.getLogger(GtfsWebFilter.class);

    /** Valid URL Mappings. */
    private static enum VALID_URLS {
        GTFS_LoadServiceAlerts, GTFS_LoadVehiclePosition, GTFSRVehiclePosition, GTFSRServiceAlerts, GTFSTransitBundle, GTFS_LoadStatic
    };

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {

        log.info("GtfsWebFilter : destroy()");
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
    ServletException
    {

        // Log Request Details
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        log.info("Request from IP {} Request URL {}", httpServletRequest.getRemoteAddr(), httpServletRequest.getRequestURL());
        if (validRequest(httpServletRequest.getRequestURL().toString())) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            log.info("Rejected Request from IP {} Request URL {}", httpServletRequest.getRemoteAddr(), httpServletRequest.getRequestURL());
            final HttpServletResponse response = (HttpServletResponse) servletResponse;
            final int responseCode = 404;
            response.setStatus(responseCode);
            response.setContentType("text/html");
            final PrintWriter out = response.getWriter();
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Invalid Request</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<BIG>Invalid Request</BIG>");
            out.println("</BODY></HTML>");
            return;
        }
    }

    /**
     * Checks URL Pattern.
     * @param requestURL
     * @return
     */
    private boolean validRequest(String requestURL) {

        for (VALID_URLS validUrl : VALID_URLS.values()) {
            final String url = validUrl.toString();
            if (requestURL.contains(url)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {

        log.info("GtfsWebFilter : init()");
    }

}
