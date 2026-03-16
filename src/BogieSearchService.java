import java.util.Optional;

/**
 * Service that performs a linear search over the consist's bogie array by bogie ID.
 *
 * <p>UC-03 teaching points demonstrated by this class:
 * <ul>
 *   <li><b>Linear search:</b> every element is examined from index 0 — O(n) worst case.</li>
 *   <li><b>Full scan:</b> the search never stops at the first match; it continues to the end
 *       to detect duplicate IDs, giving an accurate total comparison count.</li>
 *   <li><b>Optional:</b> the not-found case is modelled as {@code Optional.empty()} rather
 *       than returning {@code null}, following idiomatic Java.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class BogieSearchService {

    /**
     * Searches the bogie array for a bogie whose ID matches the given string.
     *
     * <p>Performs a full linear scan (O(n)) regardless of where the match is found,
     * in order to detect and warn about duplicate IDs.
     * Returns the <em>first</em> match wrapped in an {@link Optional},
     * or {@code Optional.empty()} if no match exists.</p>
     *
     * @param bogies  the consist's bogie array to search
     * @param bogieId the ID string to look up
     * @return Optional containing the first BogieSearchResult, or empty if not found
     */
    public static Optional<BogieSearchResult> findById(Bogie[] bogies, String bogieId) {
        int comparisons = 0;           // tracks total array elements inspected
        boolean duplicateWarned = false; // ensures the duplicate warning prints only once
        BogieSearchResult firstMatch = null; // holds the first match found

        for (int i = 0; i < bogies.length; i++) {
            comparisons++;

            if (bogies[i].getBogieId().equals(bogieId)) {
                if (firstMatch == null) {
                    // Record the first match — position and running comparison count
                    firstMatch = new BogieSearchResult(bogies[i], i, comparisons);
                } else if (!duplicateWarned) {
                    // Warn once if a second bogie with the same ID is encountered
                    System.out.println("  WARNING: Duplicate bogie ID '" + bogieId
                            + "' found at index " + i + ". Returning first match only.");
                    duplicateWarned = true;
                }
            }
        }

        if (firstMatch != null) {
            // Re-wrap with the final comparison count (full scan completed)
            return Optional.of(new BogieSearchResult(
                    firstMatch.getBogie(),
                    firstMatch.getPositionIndex(),
                    comparisons));
        }

        return Optional.empty();
    }
}
