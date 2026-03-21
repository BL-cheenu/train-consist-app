import java.util.Scanner;

/**
 * Interactive console menu for fleet dashboard and cargo update operations (UC-10).
 *
 * <p>Presents three choices:
 * <ul>
 *   <li><b>UPDATE</b> — look up a bogie by ID (O(1)) and update its cargo details.</li>
 *   <li><b>DASHBOARD</b> — display all bogies grouped by type with totals.</li>
 *   <li><b>DONE</b> — exit the fleet dashboard menu.</li>
 * </ul>
 *
 * <p>The {@link BogieIndex} is built once at menu startup and reused for all
 * subsequent lookups — not rebuilt on every operation.</p>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class FleetDashboardMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the fleet dashboard menu loop until the operator selects DONE.
     * Builds the BogieIndex once from the consist's live bogie list.
     *
     * @param consist the TrainConsist whose bogies will be indexed and updated
     */
    public static void run(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("     FLEET DASHBOARD — UC-10");
        System.out.println("========================================");

        // Build the HashMap index once — O(n) — reused for all subsequent O(1) lookups
        BogieIndex index = BogieIndex.build(consist.getBogieList());
        System.out.println("  Index built: " + index.size() + " bogies indexed.");

        while (true) {
            System.out.println("Actions: UPDATE | DASHBOARD | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "UPDATE":
                    handleUpdate(index);
                    break;

                case "DASHBOARD":
                    // Group bogies by type using computeIfAbsent pattern
                    FleetGrouping grouping = FleetDashboardService.groupByType(
                            consist.getBogieList());
                    FleetDashboardPrinter.printGrouping(grouping);
                    break;

                case "DONE":
                    System.out.println("\n  Fleet dashboard session complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose UPDATE, DASHBOARD, or DONE.");
            }
        }
    }

    /**
     * Reads a bogie ID and new cargo details from the operator,
     * then performs an O(1) update via the BogieIndex.
     *
     * <p>Rejects negative cargo weights with a re-prompt loop.</p>
     *
     * @param index the pre-built BogieIndex for O(1) lookup
     */
    private static void handleUpdate(BogieIndex index) {
        System.out.print("  Enter Bogie ID to update: ");
        String bogieId = scanner.nextLine().trim();

        if (bogieId.isEmpty()) {
            System.out.println("  Bogie ID cannot be empty.");
            return;
        }

        // O(1) lookup — check before prompting for cargo details
        if (!index.contains(bogieId)) {
            FleetDashboardPrinter.printUpdateResult(null, bogieId);
            return;
        }

        double cargoWeight = readCargoWeight();

        System.out.print("  Enter cargo description: ");
        String cargoDesc = scanner.nextLine().trim();

        CargoUpdate update = new CargoUpdate(bogieId, cargoWeight, cargoDesc);
        index.update(update);

        // Retrieve updated bogie for confirmation display
        Bogie updated = index.lookup(bogieId);
        FleetDashboardPrinter.printUpdateResult(updated, bogieId);
    }

    /**
     * Reads and validates a cargo weight from the console.
     * Loops until the operator enters a non-negative number.
     *
     * @return validated cargo weight (>= 0)
     */
    private static double readCargoWeight() {
        while (true) {
            System.out.print("  Enter new cargo weight (tonnes, >= 0): ");
            String input = scanner.nextLine().trim();
            try {
                double weight = Double.parseDouble(input);
                if (weight >= 0) return weight;
                System.out.println("  Cargo weight cannot be negative. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a numeric value.");
            }
        }
    }
}
