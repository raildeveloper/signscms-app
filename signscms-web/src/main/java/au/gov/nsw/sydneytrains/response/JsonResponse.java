package au.gov.nsw.sydneytrains.response;

/**
 * Created by administrator on 9/4/17.
 */
public class JsonResponse {
    private String status = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    private Object result = null;

}
