import java.util.Scanner;

/**
 * Interactive console menu for the safety manifest (UC-08).
 *
 * <p>Presents two choices:
 * <ul>
 *   <li><b>GENERATE</b> — builds and prints a safety manifest snapshot
 *       from the current journey log state.</li>
 *   <li><b>DONE</b> — exits the menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class ManifestMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the safety manifest menu loop until the guard selects DONE.
     *
     * @param log     the JourneyLog containing the attachment history
     * @param consist the TrainConsist used to resolve bogie details
     */
    public static void run(JourneyLog log, TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("      SAFETY MANIFEST — UC-08");
        System.out.println("========================================");

        while (true) {
            System.out.println("Actions: GENERATE | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "GENERATE":
                    // Snapshot taken at this exact moment — mid-journey safe
                    SafetyManifest manifest = ManifestBuilder.build(log, consist);
                    ManifestPrinter.print(manifest);
                    break;

                case "DONE":
                    System.out.println("\n  Manifest session complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose GENERATE or DONE.");
            }
        }
    }
}
