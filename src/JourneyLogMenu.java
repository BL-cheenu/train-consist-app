import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Interactive console menu for the journey event log (UC-06).
 *
 * <p>Presents three choices:
 * <ul>
 *   <li><b>VIEW</b> — prints all recorded events in insertion order.</li>
 *   <li><b>REPLAY</b> — reconstructs and prints the consist state at a given timestamp.</li>
 *   <li><b>DONE</b> — exits the log menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class JourneyLogMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the journey log menu loop until the auditor selects DONE.
     *
     * @param log the JourneyLog to view and replay
     */
    public static void run(JourneyLog log) {
        System.out.println("\n========================================");
        System.out.println("       JOURNEY LOG — UC-06");
        System.out.println("========================================");

        while (true) {
            System.out.println("Actions: VIEW | REPLAY | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "VIEW":
                    // Print all events in insertion order
                    JourneyLogPrinter.printLog(log);
                    break;

                case "REPLAY":
                    handleReplay(log);
                    break;

                case "DONE":
                    System.out.println("\n  Journey log review complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose VIEW, REPLAY, or DONE.");
            }
        }
    }

    /**
     * Reads a cutoff timestamp from the operator and triggers log replay up to that point.
     * Accepts ISO format (yyyy-MM-ddTHH:mm:ss) or the keyword NOW for the current time.
     *
     * @param log the JourneyLog to replay
     */
    private static void handleReplay(JourneyLog log) {
        System.out.print("  Enter cutoff timestamp (yyyy-MM-ddTHH:mm:ss) or NOW: ");
        String input = scanner.nextLine().trim();

        LocalDateTime cutoff;
        if (input.equalsIgnoreCase("NOW")) {
            cutoff = LocalDateTime.now();
        } else {
            try {
                cutoff = LocalDateTime.parse(input);
            } catch (Exception e) {
                System.out.println("  Invalid timestamp format. Use yyyy-MM-ddTHH:mm:ss");
                return;
            }
        }

        JourneyLogPrinter.printReplay(log, cutoff);
    }
}
