import java.time.LocalTime;
import java.util.TreeMap;

/**
 * Holds two sorted views of the route's station stops (UC-11).
 *
 * <p>Two {@link TreeMap} instances provide two independent sorted views:
 * <ul>
 *   <li>{@code alphabetical} — keyed by station name (String natural order A→Z).
 *       Iterating entrySet() gives stations in alphabetical order automatically —
 *       no sort code needed.</li>
 *   <li>{@code byTime} — keyed by departure time (LocalTime natural order).
 *       Iterating entrySet() gives stations in chronological order automatically.</li>
 * </ul>
 *
 * <p>Both maps hold references to the same {@link StationStop} objects —
 * no data is duplicated. Building cost is O(n log n); each iteration is O(n).</p>
 *
 * <p>Duplicate station names: if two stops share the same name, the second is
 * stored with a station-code suffix (e.g. "Salem [SLM-2]") — detected by
 * {@link ScheduleBuilder} before insertion.</p>
 */
public final class RouteSchedule {

    /**
     * Alphabetical view: station name → StationStop.
     * TreeMap natural String ordering gives A→Z iteration via entrySet().
     */
    private final TreeMap<String, StationStop> alphabetical;

    /**
     * Time-sorted view: departure time → StationStop.
     * TreeMap natural LocalTime ordering gives chronological iteration via entrySet().
     */
    private final TreeMap<LocalTime, StationStop> byTime;

    /**
     * Constructs a RouteSchedule with both TreeMaps already populated.
     *
     * @param alphabetical alphabetical TreeMap keyed by station name
     * @param byTime       chronological TreeMap keyed by departure time
     */
    public RouteSchedule(TreeMap<String, StationStop> alphabetical,
                         TreeMap<LocalTime, StationStop> byTime) {
        this.alphabetical = alphabetical;
        this.byTime = byTime;
    }

    /**
     * Returns the alphabetical TreeMap (station name → StationStop).
     *
     * @return TreeMap sorted by station name A→Z
     */
    public TreeMap<String, StationStop> getAlphabetical() {
        return alphabetical;
    }

    /**
     * Returns the time-sorted TreeMap (departure time → StationStop).
     *
     * @return TreeMap sorted chronologically by departure time
     */
    public TreeMap<LocalTime, StationStop> getByTime() {
        return byTime;
    }

    /**
     * Returns the total number of stations in this schedule.
     *
     * @return station count
     */
    public int size() {
        return alphabetical.size();
    }
}
