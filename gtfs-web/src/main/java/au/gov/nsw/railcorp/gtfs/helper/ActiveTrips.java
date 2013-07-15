// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.helper;

import au.gov.nsw.railcorp.gtfs.model.Trip;

import java.util.List;
import java.util.Map;

/**
 * ActiveTrips.
 * @author paritosh
 */
public class ActiveTrips {

    private static ActiveTrips sINSTANCE;

    private List<Trip> activeTrips;

    private Map<String, Trip> activeTripMap;

    /**
     * GetActiveTrip Instance.
     * @return ActiveTrips.
     */
    public static synchronized ActiveTrips getInstance() {

        if (sINSTANCE == null) {
            sINSTANCE = new ActiveTrips();
        }
        return sINSTANCE;
    }

    public List<Trip> getActiveTrips() {

        return activeTrips;
    }

    public void setActiveTrips(List<Trip> activetrips) {

        this.activeTrips = activetrips;
    }

    public Map<String, Trip> getActiveTripMap() {

        return activeTripMap;
    }

    public void setActiveTripMap(Map<String, Trip> activetripMap) {

        this.activeTripMap = activetripMap;
    }

}
