import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for the fleet analytics dashboard (UC-13).
 *
 * <p>Presents four choices:
 * <ul>
 *   <li><b>DASHBOARD</b> — runs all five stream pipelines and prints the full report.</li>
 *   <li><b>MANIFEST</b> — prints the sorted bogie manifest using stream().sorted().forEach().</li>
 *   <li><b>DONE</b> — exits the analytics menu.</li>
 * </ul>
 *
 * <p>The overload threshold is read from the operator at startup — never hardcoded.</p>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class AnalyticsMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the analytics menu loop until the manager selects DONE.
     *
     * @param bogies   the live bogie list to analyse
     * @param stations the collection of station stops from the route schedule
     */
    public static void run(List<Bogie> bogies, Collection<StationStop> stations) {
        System.out.println("\n========================================");
        System.out.println("   FLEET ANALYTICS — UC-13");
        System.out.println("========================================");

        // Read overload threshold from operator — not hardcoded
        int overloadThreshold = readOverloadThreshold();

        while (true) {
            System.out.println("Actions: DASHBOARD | MANIFEST | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "DASHBOARD":
                    // Build all five stream pipelines and print the dashboard
                    DashboardReport report = AnalyticsService.buildReport(
                            bogies, stations, overloadThreshold);
                    DashboardPrinter.print(report);
                    break;

                case "MANIFEST":
                    // sorted().forEach() with method references
                    AnalyticsService.printSortedManifest(bogies);
                    break;

                case "DONE":
                    System.out.println("\n  Analytics session complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose DASHBOARD, MANIFEST, or DONE.");
            }
        }
    }

    /**
     * Reads and validates the overload capacity threshold from the operator.
     * Loops until a positive integer is entered.
     *
     * @return validated overload threshold (> 0)
     */
    private static int readOverloadThreshold() {
        while (true) {
            System.out.print("Enter overload capacity threshold: ");
            String input = scanner.nextLine().trim();
            try {
                int threshold = Integer.parseInt(input);
                if (threshold > 0) return threshold;
                System.out.println("  Threshold must be positive. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a whole number.");
            }
        }
    }
}
