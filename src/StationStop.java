import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single stop on the route — a station where bogies are detached (UC-11).
 *
 * <p>Holds the station name, scheduled departure time, and the list of bogies
 * to be detached at this stop. Used as the value in both TreeMaps of
 * {@link RouteSchedule}.</p>
 */
public final class StationStop {

    /** The station name (e.g. "Chennai Central"). Used as key in the alphabetical TreeMap. */
    private final String stationName;

    /**
     * Scheduled departure time from this station.
     * Used as key in the time-sorted TreeMap.
     * LocalTime implements Comparable — TreeMap sorts chronologically automatically.
     */
    private final LocalTime departureTime;

    /**
     * List of bogies to be detached at this station.
     * May be empty — the station still appears in the manifest with 0 tonnage.
     */
    private final List<Bogie> bogiesDetached;

    /**
     * Constructs a StationStop with a name, departure time, and bogie list.
     *
     * @param stationName    the station name
     * @param departureTime  scheduled departure time
     * @param bogiesDetached list of bogies to detach at this stop (may be empty)
     */
    public StationStop(String stationName, LocalTime departureTime, List<Bogie> bogiesDetached) {
        this.stationName = stationName;
        this.departureTime = departureTime;
        this.bogiesDetached = new ArrayList<>(bogiesDetached);
    }

    /**
     * Returns the station name.
     *
     * @return station name string
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * Returns the scheduled departure time.
     *
     * @return LocalTime departure time
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Returns the list of bogies detached at this station.
     *
     * @return list of bogies (may be empty, never null)
     */
    public List<Bogie> getBogiesDetached() {
        return bogiesDetached;
    }

    /**
     * Computes the total freight tonnage of all bogies detached at this station.
     * Returns 0 if no bogies are detached.
     *
     * @return total tonnage of detached bogies
     */
    public int totalTonnage() {
        int total = 0;
        for (Bogie b : bogiesDetached) {
            total += b.getCapacity();
        }
        return total;
    }
}
