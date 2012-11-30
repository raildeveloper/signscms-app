// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.converter.types;

import java.math.BigDecimal;

/**
 * Represents a single row from the Service Alert CSV file.
 * @author John
 */
public class ServiceAlertCsvRow extends TripBasedCsvRow {

    private BigDecimal activeStart;

    private BigDecimal activeEnd;

    private String agencyId;

    private BigDecimal routeType;

    private BigDecimal cause;

    private BigDecimal effect;

    private String url;

    private String headerText;

    private String description;

    /**
     * Constructor.
     */
    public ServiceAlertCsvRow() {

        super();
    }

    /**
     * getter for activeStart.
     * @return the activeStart
     */
    public BigDecimal getActiveStart() {

        return activeStart;
    }

    /**
     * setter for activeStart.
     * @param val
     *            the activeStart to set
     */
    public void setActiveStart(BigDecimal val) {

        this.activeStart = val;
    }

    /**
     * getter for activeEnd.
     * @return the activeEnd
     */
    public BigDecimal getActiveEnd() {

        return activeEnd;
    }

    /**
     * setter for activeEnd.
     * @param val
     *            the activeEnd to set
     */
    public void setActiveEnd(BigDecimal val) {

        this.activeEnd = val;
    }

    /**
     * getter for agencyId.
     * @return the agencyId
     */
    public String getAgencyId() {

        return agencyId;
    }

    /**
     * setter for agencyId.
     * @param val
     *            the agencyId to set
     */
    public void setAgencyId(String val) {

        this.agencyId = val;
    }


    /**
     * getter for routeType.
     * @return the routeType
     */
    public BigDecimal getRouteType() {

        return routeType;
    }

    /**
     * setter for routeType.
     * @param val
     *            the routeType to set
     */
    public void setRouteType(BigDecimal val) {

        this.routeType = val;
    }

    /**
     * getter for cause.
     * @return the cause
     */
    public BigDecimal getCause() {

        return cause;
    }

    /**
     * setter for cause.
     * @param val
     *            the cause to set
     */
    public void setCause(BigDecimal val) {

        this.cause = val;
    }

    /**
     * getter for effect.
     * @return the effect
     */
    public BigDecimal getEffect() {

        return effect;
    }

    /**
     * setter for effect.
     * @param val
     *            the effect to set
     */
    public void setEffect(BigDecimal val) {

        this.effect = val;
    }

    /**
     * getter for url.
     * @return the url
     */
    public String getUrl() {

        return url;
    }

    /**
     * setter for url.
     * @param val
     *            the url to set
     */
    public void setUrl(String val) {

        this.url = val;
    }

    /**
     * getter for headerText.
     * @return the headerText
     */
    public String getHeaderText() {

        return headerText;
    }

    /**
     * setter for headerText.
     * @param val
     *            the headerText to set
     */
    public void setHeaderText(String val) {

        this.headerText = val;
    }

    /**
     * getter for description.
     * @return the description
     */
    public String getDescription() {

        return description;
    }

    /**
     * setter for description.
     * @param val
     *            the description to set
     */
    public void setDescription(String val) {

        this.description = val;
    }

    /**
     * Determines if data contents exist sufficiently to create a GTFS-R TimeRange
     * object with any data.
     * @return true if contents exist for TimeRange data
     */
    public boolean timeRangeContentExists() {

        return activeStart != null
            || activeEnd != null;
    }

    /**
     * Determines if data contents exist sufficiently to create a GTFS-R Informed Entity
     * object with any data.
     * @return true if contents exist for TimeRange data
     */
    public boolean informedEntityContentExists() {

        return agencyId != null
            || routeType != null
            || getStopId() != null
            || getTripId() != null
            || getRouteId() != null;
    }

}
