/**
 * Prints load plan results to the console (UC-09).
 *
 * <p>Two display modes:
 * <ul>
 *   <li>{@link #printPlan} — prints the sorted bogie list and heavy-haul subset.</li>
 *   <li>Each section uses a consistent table format matching ConsistPrinter.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class LoadPlanPrinter {

    /**
     * Prints the full load plan — sorted bogies and heavy-haul subset.
     *
     * @param plan  the LoadPlan to display
     * @param title the plan title (e.g. "NATURAL ORDER" or "GOODS-FIRST ORDER")
     */
    public static void printPlan(LoadPlan plan, String title) {
        System.out.println("\n========================================");
        System.out.println("     LOAD PLAN — " + title);
        System.out.println("========================================");

        if (plan.getSortedBogies().isEmpty()) {
            System.out.println("  No bogies in consist.");
        } else {
            // Print sorted bogie table
            System.out.printf("  %-4s %-12s %-12s %-14s %-8s%n",
                    "#", "Bogie ID", "Type", "Subtype", "Capacity");
            System.out.println("  " + "-".repeat(54));

            int pos = 1;
            for (Bogie b : plan.getSortedBogies()) {
                System.out.printf("  %-4d %-12s %-12s %-14s %-8d%n",
                        pos++, b.getBogieId(), b.getBogieType(),
                        b.getSubType(), b.getCapacity());
            }
        }

        // Print heavy-haul subset
        System.out.println("----------------------------------------");
        System.out.println("  Heavy-Haul Check (capacity > "
                + plan.getHeavyHaulThreshold() + "):");

        if (plan.getHeavyHaulBogies().isEmpty()) {
            System.out.println("  No heavy-haul bogies above threshold.");
        } else {
            for (Bogie b : plan.getHeavyHaulBogies()) {
                System.out.printf("  [HH] %-10s capacity: %d%n",
                        b.getBogieId(), b.getCapacity());
            }
        }

        System.out.println("========================================\n");
    }
}
