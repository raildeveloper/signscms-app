package au.gov.nsw.sydneytrains;

public class RunModel {

    private  String TripName;
    private  String DestinationId;
    private  String Id;
    private  String NextRunID;
    private  String ValidityEnd;
    private  String ValidityStart;


    public String getTripName() {
        return TripName;
    }

    public String getDestinationId() {
        return DestinationId;
    }

    public String getId() {
        return Id;
    }

    public String getNextRunID() {
        return NextRunID;
    }

    public String getValidityEnd() {
        return ValidityEnd;
    }

    public String getValidityStart() {
        return ValidityStart;
    }


    public void setTripName(String tripName) {
        TripName = tripName;
    }

    public void setDestinationId(String destinationId) {
        DestinationId = destinationId;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setNextRunID(String nextRunID) {
        NextRunID = nextRunID;
    }

    public void setValidityEnd(String validityEnd) {
        ValidityEnd = validityEnd;
    }

    public void setValidityStart(String validityStart) {
        ValidityStart = validityStart;
    }
}
