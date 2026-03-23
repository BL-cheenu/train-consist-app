import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generic stable bubble sort implementation with pass and swap telemetry (UC-12).
 *
 * <p>Bubble sort compares adjacent pairs and swaps them if out of order.
 * It makes up to n-1 passes — but terminates early if a pass produces zero swaps
 * (the list is already sorted). This early-exit optimisation means a sorted list
 * completes in a single pass with zero swaps.</p>
 *
 * <p><b>Stability:</b> swaps only when {@code comparator.compare(a, b) > 0} (strictly
 * greater) — equal elements are never swapped, preserving their original relative order.</p>
 *
 * <p><b>Generics:</b> works with any {@link Comparator} — the same implementation
 * handles weight-only sort and the multi-key Comparator.comparing chain.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class BubbleSortService {

    /**
     * Sorts a copy of the given list using stable bubble sort with the given comparator.
     * Returns a {@link DepartureSortResult} containing the sorted list and telemetry.
     *
     * <p>Works on a defensive copy — the original list is not modified.</p>
     *
     * @param bogies     the bogie list to sort
     * @param comparator the comparator defining the sort order
     * @return DepartureSortResult with sorted list and bubble sort telemetry
     */
    public static DepartureSortResult sort(List<Bogie> bogies, Comparator<Bogie> comparator) {
        // Work on a copy — caller's list is never modified
        List<Bogie> list = new ArrayList<>(bogies);
        int n = list.size();
        int totalPasses = 0;
        int totalSwaps = 0;

        long startNs = System.nanoTime();

        // Outer loop: up to n-1 passes
        for (int pass = 0; pass < n - 1; pass++) {
            totalPasses++;
            int swapsThisPass = 0;

            // Inner loop: compare adjacent pairs, shrink by pass count (tail already sorted)
            for (int i = 0; i < n - pass - 1; i++) {
                // Stable: swap ONLY when strictly greater — equal elements stay put
                if (comparator.compare(list.get(i), list.get(i + 1)) > 0) {
                    Bogie temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                    swapsThisPass++;
                    totalSwaps++;
                }
            }

            // Early-exit optimisation: zero swaps this pass → list is sorted
            if (swapsThisPass == 0) {
                break;
            }
        }

        long durationNs = System.nanoTime() - startNs;

        SortTelemetry telemetry = new SortTelemetry(
                "BubbleSort", totalPasses, totalSwaps, durationNs);

        return new DepartureSortResult(list, telemetry);
    }
}
