// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.Assert;

/**
 * Class to Mock Filter Chain
 * @author RailCorp
 */
public class MockFilterChain implements FilterChain {

    private boolean shouldBeInvoked;

    private boolean wasInvoked;

    /*
     * (non-Javadoc)
     * @see javax.servlet.FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {

        this.wasInvoked = true;
    }

    public void setExpectedInvocation(boolean shouldBeInvoked) {

        this.shouldBeInvoked = shouldBeInvoked;

    }

    public void verify() {

        if (this.shouldBeInvoked) {
            Assert.assertTrue("Expected MockFilterChain to be invoked.", this.wasInvoked);
        } else {
            Assert.assertTrue("Expected MockFilterChain filter not to be invoked.", !this.wasInvoked);
        }
    }

}
