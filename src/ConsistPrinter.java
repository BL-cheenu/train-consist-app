/**
 * Prints a formatted consist summary to the console (UC-01).
 *
 * <p>Displays train number, route, total bogie count, and a tabular list of
 * all bogies with their ID, type, subtype, and capacity.
 * If the consist is empty, a warning message is shown instead of the table.</p>
 *
 * <p>All methods are static. {@link #print} is an instance alias kept for
 * internal use by UC-04 and UC-05 menus that hold a ConsistPrinter reference.</p>
 */
public class ConsistPrinter {

    /**
     * Prints the full consist summary in a bordered table format.
     *
     * <p>Uses {@code Bogie.getSubType()} directly — no instanceof casting needed,
     * since getSubType() is defined as abstract on Bogie and overridden in subclasses.</p>
     *
     * @param consist the TrainConsist to display
     */
    public static void printSummary(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("        TRAIN CONSIST SUMMARY");
        System.out.println("========================================");
        System.out.println("Train Number : " + consist.getTrainNumber());
        System.out.println("Route        : " + consist.getRoute());
        System.out.println("Total Bogies : " + consist.getBogies().length);
        System.out.println("----------------------------------------");

        if (consist.getBogies().length == 0) {
            // UC-01 edge flow: operator entered 0 bogies
            System.out.println("  WARNING: No bogies added. Consist is empty.");
        } else {
            // Print column headers with fixed-width formatting for alignment
            System.out.printf("  %-12s  %-12s  %-14s  %-8s%n",
                    "Bogie ID", "Type", "Subtype", "Capacity");
            System.out.println("  " + "-".repeat(50));

            // Print one row per bogie — getSubType() returns String, no cast needed
            for (Bogie b : consist.getBogies()) {
                System.out.printf("  %-12s  %-12s  %-14s  %-8d%n",
                        b.getBogieId(), b.getBogieType(), b.getSubType(), b.getCapacity());
            }
        }

        System.out.println("========================================\n");
    }

    /**
     * Instance method alias for {@link #printSummary}.
     * Used by UC-04 ({@link JunctionOperationsMenu}) and UC-05 ({@link ShuntingMenu})
     * which call {@code printer.print(consist)} on an instance.
     *
     * @param consist the TrainConsist to display
     */
    public void print(TrainConsist consist) {
        printSummary(consist);
    }
}
