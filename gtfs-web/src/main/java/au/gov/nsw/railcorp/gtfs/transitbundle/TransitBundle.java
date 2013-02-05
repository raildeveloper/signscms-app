// RailCorp 2012

package au.gov.nsw.railcorp.gtfs.transitbundle;


/**
 * Bean to handle the latest Sydney Trains GTFS transit bundle.
 * @author RailCorp
 */
public class TransitBundle {

    private static TransitBundle sINSTANCE;

    private String latestBundleFileName = "";

    private String latestBundleLocation = "";

    private String bundleGenerationTime = "";

    /**
     * Return instance of Transit Bundle.
     * @return TransitBundle
     */
    public static synchronized TransitBundle getInstance() {

        if (sINSTANCE == null) {
            sINSTANCE = new TransitBundle();
        }
        return sINSTANCE;
    }

    /**
     * Get the Latest Transit Data Bundle File Name.
     * @return the latestBundleFileName
     */
    public String getLatestBundleFileName() {

        return latestBundleFileName;
    }

    /**
     * Set the Latest Transit Data Bundle File Name.
     * @param fileName
     *            the latestBundleFileName to set
     */
    public void setLatestBundleFileName(String fileName) {

        this.latestBundleFileName = fileName;
    }

    /**
     * Get the Latest Transit Data Bundle Location.
     * @return the latestBundleLocation
     */
    public String getLatestBundleLocation() {

        return latestBundleLocation;
    }

    /**
     * Set the Latest Transit Data Bundle Location.
     * @param fileLocation
     *            the latestBundleLocation to set
     */
    public void setLatestBundleLocation(String fileLocation) {

        this.latestBundleLocation = fileLocation;
    }

    /**
     * Get the Latest Transit Data Bundle Generation Time.
     * @return the bundleGenerationTime
     */
    public String getBundleGenerationTime() {

        return bundleGenerationTime;
    }

    /**
     * Set the Latest Transit Data Bundle Generation Time.
     * @param generationTime
     *            the bundleGenerationTime to set
     */
    public void setBundleGenerationTime(String generationTime) {

        this.bundleGenerationTime = generationTime;
    }

}
