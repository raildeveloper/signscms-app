package au.gov.nsw.sydneytrains.controller;

/**
 * Created by Oscar on 10/04/2017.
 *
 * Configured view: a signage view that is configured from images
 */
public class CnfView {

    private final int viewId;
    private final String viewName;
    private final String description;
    private final int pixelsHorizontal;
    private final int pixelsVertical;

    public CnfView(final int viewId, final String viewName, final String description,
                   final int pixelsHorizontal, final int pixelsVertical)
    {
        this.viewId = viewId;
        this.viewName = viewName;
        this.description = description;
        this.pixelsHorizontal = pixelsHorizontal;
        this.pixelsVertical = pixelsVertical;
    }

    public int getViewId() {
        return viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public String getDescription() {
        return description;
    }

    public int getPixelsHorizontal() {
        return pixelsHorizontal;
    }

    public int getPixelsVertical() {
        return pixelsVertical;
    }
}
