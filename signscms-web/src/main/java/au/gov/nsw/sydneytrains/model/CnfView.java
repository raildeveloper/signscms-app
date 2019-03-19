package au.gov.nsw.sydneytrains.model;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured view: a signage view that is configured from images
 */
public class CnfView {

    private final String viewId;
    private final String viewName;
    private final String description;
    private final int pixelsHorizontal;
    private final int pixelsVertical;
    private final String associated_Device;
    private final String[] images;
    private CnfDevice cnfDevice = null;

    public CnfView(final String viewId, final String viewName, final String description,
                   final int pixelsHorizontal, final int pixelsVertical, final String associated_Device, final String[] images)
    {
        this.viewId = viewId;
        this.viewName = viewName;
        this.description = description;
        this.pixelsHorizontal = pixelsHorizontal;
        this.pixelsVertical = pixelsVertical;
        this.associated_Device = associated_Device;
        this.images = images;
    }

    public String getViewId() { return viewId; }

    public String getViewName() {
        return viewName;
    }

    public String getDescription() {
        return description;
    }

    public int getPixelsHorizontal() {
        return pixelsHorizontal;
    }

    public int getPixelsVertical()
    {
        return pixelsVertical;
    }

    public String getAssociated_Device() { return  associated_Device;}

    public String[] getImages() { return images; }

    public CnfDevice getCnfDevice(){
        return cnfDevice;
    }

    public void setCnfDevice(CnfDevice cnfDevice){
        this.cnfDevice = cnfDevice;
    }

    // Debugging
    public void print() {
        System.out.println(getAsString());
    }

    public String getAsString() {
        String string = "viewId: " + this.viewId
                + " viewName: " + this.viewName
                + " description: " + this.description
                + " pixels (HxV): " + this.pixelsHorizontal + "x" + this.pixelsVertical
                + "assoc_device: " + this.associated_Device;
        return string;
    }
}
