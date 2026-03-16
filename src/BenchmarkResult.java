/**
 * Immutable value object that stores the outcome of a single benchmark run (UC-05).
 *
 * <p>Records which operation was timed, which collection type was used,
 * how many iterations were performed, and how long it took in milliseconds.
 * The {@link #toString()} method formats the result as a fixed-width table row
 * for aligned console output.</p>
 */
public final class BenchmarkResult {

    /** Name of the operation that was benchmarked (e.g. "HEAD_INSERT", "RANDOM_GET"). */
    private final String operation;

    /** Name of the collection type used (e.g. "ArrayList", "LinkedList"). */
    private final String collectionType;

    /** Number of operations performed in this benchmark run. */
    private final int count;

    /** Elapsed time for all {@code count} operations, in milliseconds. */
    private final long durationMs;

    /**
     * Constructs a BenchmarkResult with all fields.
     *
     * @param operation      name of the benchmarked operation
     * @param collectionType collection implementation used
     * @param count          number of iterations performed
     * @param durationMs     elapsed time in milliseconds
     */
    public BenchmarkResult(String operation, String collectionType, int count, long durationMs) {
        this.operation = operation;
        this.collectionType = collectionType;
        this.count = count;
        this.durationMs = durationMs;
    }

    /**
     * Returns the name of the operation that was benchmarked.
     *
     * @return operation name string
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns the name of the collection type used.
     *
     * @return collection type name (e.g. "ArrayList", "LinkedList")
     */
    public String getCollectionType() {
        return collectionType;
    }

    /**
     * Returns the number of iterations performed during the benchmark.
     *
     * @return iteration count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the total elapsed time for all iterations, in milliseconds.
     *
     * @return duration in milliseconds
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Returns a fixed-width formatted string for aligned table output.
     * Format: {@code operation | collectionType | count ops | durationMs ms}
     *
     * @return formatted benchmark row
     */
    @Override
    public String toString() {
        return String.format("%-20s | %-12s | %,7d ops | %d ms",
                operation, collectionType, count, durationMs);
    }
}
