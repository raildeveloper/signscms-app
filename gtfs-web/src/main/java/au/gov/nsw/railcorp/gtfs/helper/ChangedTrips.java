// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.helper;

import au.gov.nsw.railcorp.gtfs.model.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * Changed Trips.
 * @author paritosh
 */
public class ChangedTrips {

    private static ChangedTrips sINSTANCE;

    private List<Trip> changedTrips = new ArrayList<Trip>();

    /**
     * Changed Trips.
     * @return ChangedTrips
     */
    public static synchronized ChangedTrips getInstance() {

        if (sINSTANCE == null) {
            sINSTANCE = new ChangedTrips();

        }
        return sINSTANCE;
    }

    public List<Trip> getChangedTrips() {

        return changedTrips;
    }

    public void setChangedTrips(List<Trip> changedtrips) {

        this.changedTrips = changedtrips;
    }
}
