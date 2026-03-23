import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Departure sort service using {@link Collections#sort} (TimSort) with a
 * multi-key {@link Comparator} chain (UC-12).
 *
 * <p>TimSort is a hybrid merge/insertion sort:
 * <ul>
 *   <li>O(n log n) worst case — far better than bubble sort's O(n²).</li>
 *   <li>O(n) on nearly-sorted data — typical for real train consists.</li>
 *   <li>Guaranteed stable — equal elements keep original relative order.</li>
 * </ul>
 *
 * <p>The multi-key comparator uses the {@code Comparator.comparing} chain —
 * no nested if-else blocks:</p>
 * <pre>
 *   Comparator.comparing(Bogie::getBogieType, typeOrder)
 *             .thenComparingInt(Bogie::getWeight)
 *             .thenComparing(Bogie::getBogieId)
 * </pre>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class DepartureSortService {

    /**
     * Sorts a copy of the given list using {@link Collections#sort} with the given comparator.
     * Returns a {@link DepartureSortResult} with the sorted list and timing telemetry.
     *
     * <p>Works on a defensive copy — the original list is not modified.</p>
     *
     * @param bogies     the bogie list to sort
     * @param comparator the comparator defining the sort order
     * @return DepartureSortResult with sorted list and TimSort telemetry
     */
    public static DepartureSortResult sort(List<Bogie> bogies, Comparator<Bogie> comparator) {
        // Work on a copy — caller's list is never modified
        List<Bogie> copy = new ArrayList<>(bogies);

        long startNs = System.nanoTime();
        Collections.sort(copy, comparator); // TimSort — stable, O(n log n)
        long durationNs = System.nanoTime() - startNs;

        // passes and swaps are -1 — not applicable for TimSort
        SortTelemetry telemetry = new SortTelemetry(
                "Collections.sort (TimSort)", -1, -1, durationNs);

        return new DepartureSortResult(copy, telemetry);
    }

    /**
     * Builds the weight-only comparator: ascending weight, bogieId tiebreaker.
     * Used for the simple departure braking-balance sort.
     *
     * @return Comparator for weight-ascending sort
     */
    public static Comparator<Bogie> weightComparator() {
        return Comparator.comparingInt(Bogie::getWeight)
                         .thenComparing(Bogie::getBogieId);
    }

    /**
     * Builds the multi-key departure comparator using Comparator.comparing chain.
     *
     * <p>Order:
     * <ol>
     *   <li>Type priority: PASSENGER first, then GOODS.</li>
     *   <li>Within each type group: ascending weight.</li>
     *   <li>Tiebreaker: bogieId ascending (stable, unique).</li>
     * </ol>
     *
     * <p>Built with {@code Comparator.comparing(...).thenComparingInt(...).thenComparing(...)}
     * — no nested if-else required.</p>
     *
     * @return multi-key Comparator for departure clearance sort
     */
    public static Comparator<Bogie> multiKeyComparator() {
        // Type order: PASSENGER = 0, GOODS = 1 (PASSENGER first)
        Comparator<BogieType> typeOrder = Comparator.comparingInt(
                t -> t == BogieType.PASSENGER ? 0 : 1);

        return Comparator.comparing(Bogie::getBogieType, typeOrder)
                         .thenComparingInt(Bogie::getWeight)
                         .thenComparing(Bogie::getBogieId);
    }
}
