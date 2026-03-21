import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;

/**
 * Application entry point for the Train Consist Management App.
 *
 * <p>Orchestrates all six use cases in sequence:
 * <ol>
 *   <li><b>UC-01</b> — Build and display a train consist.</li>
 *   <li><b>UC-02</b> — Compute capacity and validate safety constraints.</li>
 *   <li><b>UC-03</b> — Search and inspect a bogie by ID.</li>
 *   <li><b>UC-04</b> — Attach and detach bogies at a junction.</li>
 *   <li><b>UC-05</b> — Reorder bogies via yard shunting operations.</li>
 *   <li><b>UC-06</b> — View journey event log and replay consist state.</li>
 * </ol>
 */
public class Main {

    /** Shared Scanner instance bound to System.in. */
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

        // ── UC-01: Build consist ───────────────────────────────────────────────
        TrainConsist consist = ConsistBuilder.buildConsist();
        ConsistPrinter.printSummary(consist);

        // Create the journey log — shared across UC-04, UC-05, UC-06
        JourneyLog journeyLog = new JourneyLog(consist.getTrainNumber());

        // ── UC-02: Capacity and safety validation ──────────────────────────────
        RouteType routeType = readRouteType();
        ValidationResult result = SafetyValidator.validate(consist.getBogies(), routeType);
        ValidationPrinter.printReport(result);

        // ── UC-03: Search bogie by ID ──────────────────────────────────────────
        System.out.println("========================================");
        System.out.println("         BOGIE SEARCH");
        System.out.println("========================================");

        while (true) {
            String bogieId = readBogieId();
            if (bogieId.equalsIgnoreCase("exit")) break;

            Optional<BogieSearchResult> searchResult =
                    BogieSearchService.findById(consist.getBogies(), bogieId);

            if (searchResult.isPresent()) {
                BogieSearchPrinter.printFound(searchResult.get(), consist.getBogies().length);
            } else {
                BogieSearchPrinter.printNotFound(bogieId, consist.getTrainNumber());
            }

            System.out.print("Search another bogie? (yes / no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) break;
        }

        // ── UC-04: Junction attach / detach (now instrumented with journeyLog) ─
        JunctionOperationsMenu.run(consist, routeType, journeyLog);

        // ── UC-05: Yard shunting (now instrumented with journeyLog) ────────────
        ShuntingMenu.run(consist, journeyLog);

        // ── UC-06: Journey event log and replay ────────────────────────────────
        JourneyLogMenu.run(journeyLog);

        scanner.close();
    }

    /**
     * Reads and validates a RouteType from the console.
     *
     * @return validated RouteType enum value
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
     * Reads a non-empty bogie ID for the UC-03 search loop.
     *
     * @return trimmed bogie ID or "exit"
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
