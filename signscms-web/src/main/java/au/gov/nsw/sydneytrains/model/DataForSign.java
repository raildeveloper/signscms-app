package au.gov.nsw.sydneytrains.model;

/**
 * Created by Oscar on 20/04/2017.
 *
 * Contains the data for the response to the URL request of a sign.
 */
public class DataForSign {

    private CnfDevice device;
    private CnfView view;

    public DataForSign(CnfDevice device, CnfView view) {
        this.device = device;
        this.view = view;
    }

    public CnfDevice getDevice() {
        return device;
    }

    public CnfView getView() { return view; }
}
