import java.util.List;

/**
 * Prints departure sort results and algorithm comparison to the console (UC-12).
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class DepartureSortPrinter {

    /**
     * Prints the sorted consist from a DepartureSortResult with telemetry.
     *
     * @param result the DepartureSortResult to display
     * @param title  short label for this sort run (e.g. "WEIGHT SORT — BubbleSort")
     */
    public static void printResult(DepartureSortResult result, String title) {
        System.out.println("\n========================================");
        System.out.println("  " + title);
        System.out.println("========================================");

        // Print telemetry
        SortTelemetry t = result.getTelemetry();
        System.out.println("  Algorithm : " + t.getAlgorithm());
        if (t.getPasses() >= 0) {
            System.out.println("  Passes    : " + t.getPasses());
            System.out.println("  Swaps     : " + t.getSwaps()
                    + (t.getSwaps() == 0 ? " (already sorted — early exit)" : ""));
        }
        System.out.println("  Duration  : " + t.getDurationNs() + " ns");
        System.out.println("----------------------------------------");

        // Print sorted bogie list
        System.out.printf("  %-4s %-10s %-12s %-10s %-8s%n",
                "#", "Bogie ID", "Type", "Subtype", "Weight");
        System.out.println("  " + "-".repeat(48));

        List<Bogie> sorted = result.getSortedConsist();
        for (int i = 0; i < sorted.size(); i++) {
            Bogie b = sorted.get(i);
            System.out.printf("  %-4d %-10s %-12s %-10s %-8d%n",
                    (i + 1), b.getBogieId(), b.getBogieType(),
                    b.getSubType(), b.getWeight());
        }
        System.out.println("========================================\n");
    }

    /**
     * Compares two sort results and prints whether they are identical.
     * Used to confirm bubble sort and TimSort produce the same ordering.
     *
     * @param bubbleResult result from BubbleSortService
     * @param timsortResult result from DepartureSortService
     */
    public static void printComparison(DepartureSortResult bubbleResult,
                                       DepartureSortResult timsortResult) {
        System.out.println("========================================");
        System.out.println("  SORT COMPARISON");
        System.out.println("========================================");

        List<Bogie> bubble = bubbleResult.getSortedConsist();
        List<Bogie> timsort = timsortResult.getSortedConsist();

        boolean identical = bubble.size() == timsort.size();
        if (identical) {
            for (int i = 0; i < bubble.size(); i++) {
                if (!bubble.get(i).getBogieId().equals(timsort.get(i).getBogieId())) {
                    identical = false;
                    break;
                }
            }
        }

        if (identical) {
            System.out.println("  Result : IDENTICAL ✓ — both algorithms agree");
        } else {
            System.out.println("  Result : MISMATCH ✗ — algorithms disagree");
            System.out.println("  BubbleSort order   : " + getIds(bubble));
            System.out.println("  TimSort order      : " + getIds(timsort));
        }

        System.out.println("  BubbleSort duration : "
                + bubbleResult.getTelemetry().getDurationNs() + " ns");
        System.out.println("  TimSort duration    : "
                + timsortResult.getTelemetry().getDurationNs() + " ns");
        System.out.println("========================================\n");
    }

    /**
     * Extracts bogie IDs from a sorted list for display in comparison output.
     *
     * @param bogies sorted bogie list
     * @return comma-separated ID string
     */
    private static String getIds(List<Bogie> bogies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bogies.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(bogies.get(i).getBogieId());
        }
        return sb.toString();
    }
}
