/**
 * Immutable value object that records the telemetry of a sort operation (UC-12).
 *
 * <p>Captures the algorithm name, number of passes made, total swaps performed,
 * and elapsed time in nanoseconds. Used as the learning telemetry display to make
 * the O(n²) cost of bubble sort tangible vs O(n log n) of TimSort.</p>
 */
public final class SortTelemetry {

    /** Name of the sort algorithm (e.g. "BubbleSort", "Collections.sort (TimSort)"). */
    private final String algorithm;

    /**
     * Number of passes made through the list.
     * For bubble sort: up to n-1. Terminates early if a pass has zero swaps.
     * For Collections.sort: -1 (not applicable — TimSort is not pass-based).
     */
    private final int passes;

    /**
     * Total number of adjacent swaps performed.
     * For bubble sort: reflects actual work done — 0 means already sorted.
     * For Collections.sort: -1 (not applicable).
     */
    private final int swaps;

    /**
     * Total elapsed time for the sort in nanoseconds.
     * Measured with System.nanoTime() before and after the sort call.
     */
    private final long durationNs;

    /**
     * Constructs a SortTelemetry record.
     *
     * @param algorithm  name of the algorithm
     * @param passes     number of passes (-1 if not applicable)
     * @param swaps      number of swaps (-1 if not applicable)
     * @param durationNs elapsed time in nanoseconds
     */
    public SortTelemetry(String algorithm, int passes, int swaps, long durationNs) {
        this.algorithm = algorithm;
        this.passes = passes;
        this.swaps = swaps;
        this.durationNs = durationNs;
    }

    /** @return algorithm name */
    public String getAlgorithm()  { return algorithm; }

    /** @return number of passes (-1 if not applicable) */
    public int getPasses()        { return passes; }

    /** @return number of swaps (-1 if not applicable) */
    public int getSwaps()         { return swaps; }

    /** @return elapsed time in nanoseconds */
    public long getDurationNs()   { return durationNs; }

    /**
     * Returns a human-readable summary of this telemetry record.
     *
     * @return formatted telemetry string
     */
    @Override
    public String toString() {
        if (passes == -1) {
            return String.format("%-30s | duration: %d ns", algorithm, durationNs);
        }
        return String.format("%-30s | passes: %d | swaps: %d | duration: %d ns",
                algorithm, passes, swaps, durationNs);
    }
}
