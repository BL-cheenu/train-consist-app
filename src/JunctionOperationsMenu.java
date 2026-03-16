import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for attach and detach operations at a junction station (UC-04).
 *
 * <p>Presents a loop with three choices:
 * <ul>
 *   <li><b>ATTACH</b> — reads new bogie details and inserts at the specified position.</li>
 *   <li><b>DETACH</b> — removes a bogie by ID from the consist.</li>
 *   <li><b>DONE</b> — runs a final safety validation and exits the menu.</li>
 * </ul>
 *
 * <p>After every ATTACH or DETACH, safety validation is automatically re-run.
 * If a violation is detected, the operator is warned but the operation is kept
 * (operator override semantics from the UC-04 alternate flow).</p>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class JunctionOperationsMenu {

    /** Shared Scanner instance bound to System.in for all menu input. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the junction operations menu loop until the operator selects DONE.
     *
     * <p>Displays the current consist before each prompt.
     * On DONE, performs a final safety validation and prints the report.</p>
     *
     * @param consist   the TrainConsist to operate on (uses getBogieList() for live mutation)
     * @param routeType the route type used for safety re-validation after each operation
     */
    public static void run(TrainConsist consist, RouteType routeType) {
        // Get the live mutable list — all attach/detach operations modify this directly
        List<Bogie> bogies = consist.getBogieList();

        System.out.println("\n========================================");
        System.out.println("       JUNCTION OPERATIONS — UC-04");
        System.out.println("========================================");

        while (true) {
            ConsistPrinter.printSummary(consist);
            System.out.println("Actions: ATTACH | DETACH | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "ATTACH":
                    handleAttach(bogies);
                    break;
                case "DETACH":
                    handleDetach(bogies);
                    break;
                case "DONE":
                    // Final safety check before leaving the junction menu
                    System.out.println("\n--- Final Validation ---");
                    ValidationPrinter.printReport(SafetyValidator.validate(bogies, routeType));
                    return;
                default:
                    System.out.println("  Invalid action. Choose ATTACH, DETACH, or DONE.");
                    continue;
            }

            // Re-run safety validation after every successful operation
            if (!bogies.isEmpty()) {
                ValidationResult result = SafetyValidator.validate(bogies, routeType);
                if (!result.isValid()) {
                    // UC-04 alternate flow: warn but allow operator override
                    System.out.println("  [!] Safety check after operation:");
                    for (String v : result.getViolations()) System.out.println("      " + v);
                    System.out.println("  Operator override accepted — operation kept.\n");
                } else {
                    System.out.println("  Safety check: PASSED\n");
                }
            }
        }
    }

    /**
     * Handles the ATTACH action — reads a new bogie from the console and
     * inserts it at the operator-specified position.
     *
     * <p>Accepts a numeric index or the keyword "REAR". Invalid position input
     * defaults to rear attachment.</p>
     *
     * @param bogies the live bogie list to insert the new bogie into
     */
    private static void handleAttach(List<Bogie> bogies) {
        System.out.println("\n-- Attach Bogie --");
        // Reuse ConsistBuilder to read and validate the new bogie's details
        Bogie newBogie = ConsistBuilder.buildSingleBogie();

        System.out.print("  Enter position to attach (0 to " + bogies.size() + ", or REAR): ");
        String posInput = scanner.nextLine().trim().toUpperCase();

        // -1 signals "rear" to AttachRequest
        int position = posInput.equals("REAR") ? -1 : parsePosition(posInput);
        AttachRequest request = new AttachRequest(newBogie, position);
        ConsistOperationsService.attach(bogies, request);
        System.out.println("  Bogie '" + newBogie.getBogieId() + "' attached successfully.");
    }

    /**
     * Handles the DETACH action — reads a bogie ID and removes it from the consist.
     * Prints an error if the ID is empty or not found.
     *
     * @param bogies the live bogie list to remove the bogie from
     */
    private static void handleDetach(List<Bogie> bogies) {
        System.out.print("\n-- Detach Bogie --\n  Enter Bogie ID to detach: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) {
            System.out.println("  Bogie ID cannot be empty.");
            return;
        }
        boolean removed = ConsistOperationsService.detach(bogies, new DetachRequest(bogieId));
        if (removed) System.out.println("  Bogie '" + bogieId + "' detached successfully.");
    }

    /**
     * Parses a position string to an integer.
     * Returns -1 (rear) if the string is not a valid integer.
     *
     * @param input the raw position string entered by the operator
     * @return parsed position index, or -1 on parse failure
     */
    private static int parsePosition(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("  Invalid position. Attaching at rear.");
            return -1;
        }
    }
}
