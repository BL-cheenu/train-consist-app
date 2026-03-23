import java.util.Optional;

/**
 * Immutable value object wrapping the result of a consist query (UC-14).
 *
 * <p>Pairs an {@link Optional} result with a human-readable description and
 * execution time. The Optional forces callers to handle the "not found" case
 * explicitly — no null returns, no NullPointerException risk.</p>
 *
 * @param <T> the type of the query result (e.g. Bogie, ProximityViolation)
 */
public final class QueryResult<T> {

    /**
     * The query result — present if found, empty if not.
     * Using Optional here forces the caller to check before using the value.
     */
    private final Optional<T> result;

    /** Human-readable description of what was queried (e.g. "Heaviest PASSENGER bogie"). */
    private final String queryDescription;

    /** Execution time of the query in nanoseconds (measured with System.nanoTime()). */
    private final long executionTimeNs;

    /**
     * Constructs a QueryResult.
     *
     * @param result           Optional result (present = found, empty = not found)
     * @param queryDescription description of the query that was run
     * @param executionTimeNs  elapsed time in nanoseconds
     */
    public QueryResult(Optional<T> result, String queryDescription, long executionTimeNs) {
        this.result = result;
        this.queryDescription = queryDescription;
        this.executionTimeNs = executionTimeNs;
    }

    /** @return Optional result (present or empty) */
    public Optional<T> getResult()          { return result; }

    /** @return query description string */
    public String getQueryDescription()     { return queryDescription; }

    /** @return execution time in nanoseconds */
    public long getExecutionTimeNs()        { return executionTimeNs; }

    /** @return true if a result was found */
    public boolean isFound()                { return result.isPresent(); }
}
