import java.util.Optional;
import java.util.Scanner;

/**
 * Application entry point for the Train Consist Management App.
 *
 * <p>Orchestrates all five use cases in sequence:
 * <ol>
 *   <li><b>UC-01</b> — Build and display a train consist.</li>
 *   <li><b>UC-02</b> — Compute capacity and validate safety constraints.</li>
 *   <li><b>UC-03</b> — Search and inspect a bogie by ID.</li>
 *   <li><b>UC-04</b> — Attach and detach bogies at a junction.</li>
 *   <li><b>UC-05</b> — Reorder bogies via yard shunting operations.</li>
 * </ol>
 *
 * <p>All service and menu classes use static methods — {@code Main} calls them
 * directly without instantiation, matching the pattern used by the operator's
 * existing codebase.</p>
 */
public class Main {

    /** Shared Scanner instance bound to System.in — used for route type and search prompts. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Application entry point — runs all use cases in order.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     TRAIN CONSIST MANAGEMENT APP");
        System.out.println("========================================\n");

        // ── UC-01: Build consist from operator input ──────────────────────────
        TrainConsist consist = ConsistBuilder.buildConsist();
        ConsistPrinter.printSummary(consist);

        // ── UC-02: Capacity computation and safety validation ─────────────────
        RouteType routeType = readRouteType();
        ValidationResult result = SafetyValidator.validate(consist.getBogies(), routeType);
        ValidationPrinter.printReport(result);

        // ── UC-03: Linear search by bogie ID ─────────────────────────────────
        System.out.println("========================================");
        System.out.println("         BOGIE SEARCH");
        System.out.println("========================================");

        while (true) {
            String bogieId = readBogieId();

            // 'exit' keyword ends the search loop
            if (bogieId.equalsIgnoreCase("exit")) break;

            Optional<BogieSearchResult> searchResult =
                    BogieSearchService.findById(consist.getBogies(), bogieId);

            if (searchResult.isPresent()) {
                BogieSearchPrinter.printFound(searchResult.get(), consist.getBogies().length);
            } else {
                BogieSearchPrinter.printNotFound(bogieId, consist.getTrainNumber());
            }

            System.out.print("Search another bogie? (yes / no): ");
            String again = scanner.nextLine().trim().toLowerCase();
            if (!again.equals("yes")) break;
        }

        // ── UC-04: Junction attach / detach with safety re-validation ─────────
        JunctionOperationsMenu.run(consist, routeType);

        // ── UC-05: Yard shunting — reorder, benchmark ArrayList vs LinkedList ──
        ShuntingMenu.run(consist);

        scanner.close();
    }

    /**
     * Reads and validates a RouteType from the console.
     * Loops until the operator enters a recognised value (case-insensitive).
     *
     * @return validated RouteType enum value (SUBURBAN, EXPRESS, or FREIGHT)
     */
    private static RouteType readRouteType() {
        while (true) {
            System.out.print("Enter Route Type (SUBURBAN / EXPRESS / FREIGHT): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return RouteType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid route type. Valid options: SUBURBAN, EXPRESS, FREIGHT");
            }
        }
    }

    /**
     * Reads a non-empty bogie ID string from the console for UC-03 search.
     * Loops until the operator enters a non-blank value or the keyword "exit".
     *
     * @return trimmed bogie ID string, or "exit" to end the search loop
     */
    private static String readBogieId() {
        while (true) {
            System.out.print("Enter Bogie ID to search (or 'exit' to quit): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("  Bogie ID cannot be empty. Please try again.");
        }
    }
}
