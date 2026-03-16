import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete train consist — the ordered collection of bogies
 * assigned to a specific train number and route.
 *
 * <p>Internally stores bogies as an {@link ArrayList} to support dynamic
 * attach/detach operations introduced in UC-04. A backward-compatible
 * {@link #getBogies()} method is provided so UC-02 and UC-03 (which operate
 * on {@code Bogie[]}) continue to work unchanged.</p>
 */
public class TrainConsist {

    /** Unique identifier for this train (e.g. "TN-2201"). */
    private String trainNumber;

    /** The route this consist is assigned to (e.g. "Chennai Central → Coimbatore"). */
    private String route;

    /**
     * Ordered list of bogies in this consist.
     * Backed by ArrayList — supports O(1) index access and dynamic resizing.
     * Migrated from Bogie[] in UC-04 to support attach/detach operations.
     */
    private List<Bogie> bogies;

    /**
     * UC-01 constructor — accepts a fixed-size {@code Bogie[]} and migrates
     * it into an internal ArrayList for future dynamic operations.
     *
     * @param trainNumber unique train identifier
     * @param route       route name or description
     * @param bogies      initial fixed array of bogies entered by the operator
     */
    public TrainConsist(String trainNumber, String route, Bogie[] bogies) {
        this.trainNumber = trainNumber;
        this.route = route;
        this.bogies = new ArrayList<>();
        for (Bogie b : bogies) {
            this.bogies.add(b);
        }
    }

    /**
     * UC-04 constructor — accepts a {@code List<Bogie>} directly.
     * Creates a defensive copy to prevent external mutation.
     *
     * @param trainNumber unique train identifier
     * @param route       route name or description
     * @param bogies      list of bogies to initialise the consist
     */
    public TrainConsist(String trainNumber, String route, List<Bogie> bogies) {
        this.trainNumber = trainNumber;
        this.route = route;
        this.bogies = new ArrayList<>(bogies);
    }

    /**
     * Returns the train number.
     *
     * @return train number string (e.g. "TN-2201")
     */
    public String getTrainNumber() {
        return trainNumber;
    }

    /**
     * Returns the route assigned to this consist.
     *
     * @return route description string
     */
    public String getRoute() {
        return route;
    }

    /**
     * Returns the live mutable list of bogies.
     * UC-04 and UC-05 use this to attach, detach, and reorder bogies directly.
     *
     * @return mutable ArrayList of bogies
     */
    public List<Bogie> getBogieList() {
        return bogies;
    }

    /**
     * Returns a snapshot of the bogie list as a {@code Bogie[]}.
     * Provided for backward compatibility with UC-02 (SafetyValidator)
     * and UC-03 (BogieSearchService), which were written against arrays.
     *
     * @return array snapshot of the current bogies list
     */
    public Bogie[] getBogies() {
        return bogies.toArray(new Bogie[0]);
    }
}
