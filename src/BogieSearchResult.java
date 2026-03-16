/**
 * Immutable value object returned by {@link BogieSearchService#findById} on a successful search.
 *
 * <p>Captures not just the matched bogie but also where it was found ({@code positionIndex})
 * and how many array comparisons were made ({@code comparisonsUsed}).
 * The comparison count is the teaching moment for UC-03 — it makes the O(n) cost of
 * linear search visible to the operator at runtime.</p>
 */
public final class BogieSearchResult {

    /** The bogie that matched the search ID. */
    private final Bogie bogie;

    /** Zero-based index of the matching bogie in the consist array. */
    private final int positionIndex;

    /**
     * Total number of bogies inspected before (and including) returning this result.
     * Always equals the full array length because the search performs a complete scan
     * to detect duplicate IDs.
     */
    private final int comparisonsUsed;

    /**
     * Constructs a BogieSearchResult with all fields.
     *
     * @param bogie           the matched bogie
     * @param positionIndex   zero-based position in the consist array
     * @param comparisonsUsed number of array comparisons made during the search
     */
    public BogieSearchResult(Bogie bogie, int positionIndex, int comparisonsUsed) {
        this.bogie = bogie;
        this.positionIndex = positionIndex;
        this.comparisonsUsed = comparisonsUsed;
    }

    /**
     * Returns the matched bogie.
     *
     * @return the bogie found at the searched ID
     */
    public Bogie getBogie() {
        return bogie;
    }

    /**
     * Returns the zero-based position index of the matched bogie in the consist.
     *
     * @return position index (0 = first bogie)
     */
    public int getPositionIndex() {
        return positionIndex;
    }

    /**
     * Returns the total number of comparisons made during the linear scan.
     * This value is always equal to the full consist size — the search never
     * terminates early, ensuring duplicate IDs are also detected.
     *
     * @return number of comparisons used
     */
    public int getComparisonsUsed() {
        return comparisonsUsed;
    }
}
