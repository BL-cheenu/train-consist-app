import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for attach and detach operations at a junction station (UC-04).
 * Updated in UC-06 to pass the shared {@link JourneyLog} to every mutation operation
 * so events are automatically recorded in the audit trail.
 */
public class JunctionOperationsMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the junction operations menu loop until the operator selects DONE.
     *
     * @param consist    the TrainConsist to operate on
     * @param routeType  used for safety re-validation after each operation
     * @param journeyLog the shared log — every mutation appends an event here
     */
    public static void run(TrainConsist consist, RouteType routeType, JourneyLog journeyLog) {
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
                    handleAttach(bogies, journeyLog);
                    break;
                case "DETACH":
                    handleDetach(bogies, journeyLog);
                    break;
                case "DONE":
                    System.out.println("\n--- Final Validation ---");
                    ValidationPrinter.printReport(SafetyValidator.validate(bogies, routeType));
                    return;
                default:
                    System.out.println("  Invalid action. Choose ATTACH, DETACH, or DONE.");
                    continue;
            }

            // Re-run safety validation after every mutation
            if (!bogies.isEmpty()) {
                ValidationResult result = SafetyValidator.validate(bogies, routeType);
                if (!result.isValid()) {
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
     * Reads a new bogie and inserts it at the specified position.
     * Passes actorId "YARD_OP" to the log.
     */
    private static void handleAttach(List<Bogie> bogies, JourneyLog log) {
        System.out.println("\n-- Attach Bogie --");
        Bogie newBogie = ConsistBuilder.buildSingleBogie();

        System.out.print("  Enter position to attach (0 to " + bogies.size() + ", or REAR): ");
        String posInput = scanner.nextLine().trim().toUpperCase();

        int position = posInput.equals("REAR") ? -1 : parsePosition(posInput);
        ConsistOperationsService.attach(bogies, new AttachRequest(newBogie, position),
                log, "YARD_OP");
        System.out.println("  Bogie '" + newBogie.getBogieId() + "' attached successfully.");
    }

    /**
     * Reads a bogie ID and removes it from the consist.
     * Passes actorId "YARD_OP" to the log.
     */
    private static void handleDetach(List<Bogie> bogies, JourneyLog log) {
        System.out.print("\n-- Detach Bogie --\n  Enter Bogie ID to detach: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) { System.out.println("  Bogie ID cannot be empty."); return; }
        boolean removed = ConsistOperationsService.detach(bogies,
                new DetachRequest(bogieId), log, "YARD_OP");
        if (removed) System.out.println("  Bogie '" + bogieId + "' detached successfully.");
    }

    /** Parses a position string, returning -1 on failure. */
    private static int parsePosition(String input) {
        try { return Integer.parseInt(input); }
        catch (NumberFormatException e) {
            System.out.println("  Invalid position. Attaching at rear.");
            return -1;
        }
    }
}
