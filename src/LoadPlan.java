import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Immutable value object produced by {@link LoadPlanService} (UC-09).
 *
 * <p>Holds two views of the consist's bogies:
 * <ul>
 *   <li>{@code sortedBogies} — all bogies sorted by the requested ordering
 *       (natural capacity order or custom GOODS-first order).</li>
 *   <li>{@code heavyHaulBogies} — subset of bogies above the heavy-haul
 *       capacity threshold, extracted via {@code TreeSet.tailSet()}.</li>
 * </ul>
 *
 * <p>Both sets are {@link TreeSet} instances — sorted, no duplicates,
 * O(log n) navigation operations.</p>
 */
public final class LoadPlan {

    /**
     * All bogies sorted by the plan's ordering (natural or custom).
     * TreeSet maintains sorted order automatically at O(log n) per insertion.
     */
    private final TreeSet<Bogie> sortedBogies;

    /**
     * Subset of bogies whose capacity exceeds the heavy-haul threshold.
     * Extracted using {@code TreeSet.tailSet()} — O(log n) range query.
     * Empty if no bogies exceed the threshold.
     */
    private final SortedSet<Bogie> heavyHaulBogies;

    /** The capacity threshold used to extract the heavy-haul subset. */
    private final int heavyHaulThreshold;

    /**
     * Constructs a LoadPlan with the sorted set and heavy-haul subset.
     *
     * @param sortedBogies     all bogies in sorted order
     * @param heavyHaulBogies  subset above the threshold
     * @param heavyHaulThreshold the capacity value used as the lower bound
     */
    public LoadPlan(TreeSet<Bogie> sortedBogies, SortedSet<Bogie> heavyHaulBogies,
                    int heavyHaulThreshold) {
        this.sortedBogies = sortedBogies;
        this.heavyHaulBogies = heavyHaulBogies;
        this.heavyHaulThreshold = heavyHaulThreshold;
    }

    /**
     * Returns all bogies in sorted order.
     *
     * @return TreeSet of bogies sorted by the plan's comparator
     */
    public TreeSet<Bogie> getSortedBogies() {
        return sortedBogies;
    }

    /**
     * Returns the subset of bogies above the heavy-haul threshold.
     *
     * @return SortedSet of heavy-haul bogies (may be empty)
     */
    public SortedSet<Bogie> getHeavyHaulBogies() {
        return heavyHaulBogies;
    }

    /**
     * Returns the heavy-haul capacity threshold used for range extraction.
     *
     * @return threshold capacity value
     */
    public int getHeavyHaulThreshold() {
        return heavyHaulThreshold;
    }
}
