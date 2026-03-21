import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Service that builds load plans from a consist's bogie list (UC-09).
 *
 * <p>Three operations:
 * <ul>
 *   <li>{@link #buildNaturalPlan} — sorts by capacity ascending using Bogie's
 *       natural {@link Comparable} order (capacity, then bogieId tiebreaker).</li>
 *   <li>{@link #buildCustomPlan} — sorts using {@link BogieTypeCapacityComparator}:
 *       GOODS first, then capacity, then bogieId tiebreaker.</li>
 *   <li>{@link #heavyHaulCheck} — extracts bogies above a capacity threshold
 *       using {@code TreeSet.tailSet()} — O(log n) range query.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class LoadPlanService {

    /** Default heavy-haul threshold as per UC-09 specification. */
    public static final int DEFAULT_HEAVY_HAUL_THRESHOLD = 60;

    /**
     * Builds a load plan using Bogie's natural ordering (capacity ascending).
     *
     * <p>Constructs a {@code TreeSet} using the default constructor — this relies on
     * {@link Bogie#compareTo} for ordering and uniqueness. The bogieId tiebreaker
     * in compareTo ensures no two bogies with equal capacity are silently dropped.</p>
     *
     * @param bogies the consist's bogie list to sort
     * @return LoadPlan with natural-order TreeSet and heavy-haul subset
     */
    public static LoadPlan buildNaturalPlan(List<Bogie> bogies) {
        // TreeSet default ctor uses Comparable (Bogie.compareTo) for ordering
        TreeSet<Bogie> sorted = new TreeSet<>();
        sorted.addAll(bogies);

        SortedSet<Bogie> heavyHaul = heavyHaulCheck(sorted, DEFAULT_HEAVY_HAUL_THRESHOLD);

        return new LoadPlan(sorted, heavyHaul, DEFAULT_HEAVY_HAUL_THRESHOLD);
    }

    /**
     * Builds a load plan using the custom GOODS-first ordering.
     *
     * <p>Constructs a {@code TreeSet} with {@link BogieTypeCapacityComparator}
     * passed to the constructor — this overrides the natural order entirely.
     * The comparator's bogieId tiebreaker prevents silent deduplication.</p>
     *
     * @param bogies the consist's bogie list to sort
     * @return LoadPlan with custom-order TreeSet and heavy-haul subset
     */
    public static LoadPlan buildCustomPlan(List<Bogie> bogies) {
        // TreeSet(Comparator) ctor uses BogieTypeCapacityComparator for ordering
        TreeSet<Bogie> sorted = new TreeSet<>(new BogieTypeCapacityComparator());
        sorted.addAll(bogies);

        SortedSet<Bogie> heavyHaul = heavyHaulCheck(sorted, DEFAULT_HEAVY_HAUL_THRESHOLD);

        return new LoadPlan(sorted, heavyHaul, DEFAULT_HEAVY_HAUL_THRESHOLD);
    }

    /**
     * Extracts all bogies above the given capacity threshold using {@code tailSet()}.
     *
     * <p>{@code tailSet(fromElement)} returns a view of all elements >= fromElement
     * in the TreeSet's comparator order. We create a sentinel bogie with the threshold
     * capacity and bogieId "" (empty string — sorts before all real IDs) so that
     * tailSet includes all bogies with capacity > threshold and also capacity == threshold.</p>
     *
     * <p>If no bogies exceed the threshold, an empty SortedSet is returned.</p>
     *
     * @param sorted    the sorted TreeSet to extract from
     * @param threshold minimum capacity (exclusive lower bound for heavy-haul)
     * @return SortedSet of bogies with capacity > threshold (may be empty)
     */
    public static SortedSet<Bogie> heavyHaulCheck(TreeSet<Bogie> sorted, int threshold) {
        if (sorted.isEmpty()) {
            return new TreeSet<>(sorted.comparator());
        }

        // Sentinel bogie: capacity = threshold + 1, bogieId = "" (sorts first for any capacity)
        // This gives us all bogies strictly above the threshold
        Bogie sentinel = new PassengerBogie("\u0000", PassengerSubType.SLEEPER, threshold + 1);

        // tailSet returns a live view — all elements >= sentinel in comparator order
        return sorted.tailSet(sentinel);
    }
}
