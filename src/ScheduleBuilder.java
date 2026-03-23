import java.time.LocalTime;
import java.util.List;
import java.util.TreeMap;

/**
 * Builds a {@link RouteSchedule} from a list of {@link StationStop} objects (UC-11).
 *
 * <p>Populates two TreeMaps in a single O(n log n) pass:
 * <ul>
 *   <li>Alphabetical: keyed by station name — duplicate names get a station-code suffix.</li>
 *   <li>By time: keyed by departure time — duplicate times get a millisecond offset.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ScheduleBuilder {

    /**
     * Builds a RouteSchedule from the given list of station stops.
     *
     * <p>Iterates the list once, inserting each stop into both TreeMaps.
     * Duplicate station names are resolved by appending a numeric suffix.
     * Duplicate departure times are resolved by adding one nanosecond offset.</p>
     *
     * @param stops the ordered list of station stops on the route
     * @return populated RouteSchedule with both sorted views ready for iteration
     */
    public static RouteSchedule build(List<StationStop> stops) {
        TreeMap<String, StationStop> alphabetical = new TreeMap<>();
        TreeMap<LocalTime, StationStop> byTime = new TreeMap<>();

        int duplicateNameCounter = 1;
        int duplicateTimeCounter = 1;

        for (StationStop stop : stops) {
            // Resolve duplicate station names by appending a station-code suffix
            String nameKey = stop.getStationName();
            if (alphabetical.containsKey(nameKey)) {
                nameKey = nameKey + " [" + duplicateNameCounter++ + "]";
            }
            alphabetical.put(nameKey, stop);

            // Resolve duplicate departure times by adding nanosecond offsets
            LocalTime timeKey = stop.getDepartureTime();
            while (byTime.containsKey(timeKey)) {
                timeKey = timeKey.plusNanos(duplicateTimeCounter++);
            }
            byTime.put(timeKey, stop);
        }

        return new RouteSchedule(alphabetical, byTime);
    }
}
