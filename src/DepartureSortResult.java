import java.util.List;

/**
 * Immutable value object returned by both sort services (UC-12).
 *
 * <p>Pairs the sorted bogie list with the {@link SortTelemetry} that describes
 * how the sort was performed. Used to compare bubble sort and TimSort results
 * and verify they produce identical ordering.</p>
 */
public final class DepartureSortResult {

    /**
     * The sorted bogie list.
     * For BubbleSortService: sorted in-place, same list reference returned.
     * For DepartureSortService: Collections.sort result on a copy.
     */
    private final List<Bogie> sortedConsist;

    /** The telemetry recorded during the sort (passes, swaps, duration). */
    private final SortTelemetry telemetry;

    /**
     * Constructs a DepartureSortResult.
     *
     * @param sortedConsist the sorted bogie list
     * @param telemetry     sort performance metrics
     */
    public DepartureSortResult(List<Bogie> sortedConsist, SortTelemetry telemetry) {
        this.sortedConsist = sortedConsist;
        this.telemetry = telemetry;
    }

    /**
     * Returns the sorted bogie list.
     *
     * @return sorted list of bogies
     */
    public List<Bogie> getSortedConsist() { return sortedConsist; }

    /**
     * Returns the telemetry recorded during sorting.
     *
     * @return SortTelemetry with algorithm, passes, swaps, duration
     */
    public SortTelemetry getTelemetry() { return telemetry; }
}
