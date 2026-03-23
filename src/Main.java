import java.util.Optional;
import java.util.Scanner;

/**
 * Application entry point for the Train Consist Management App.
 * UC-01 through UC-12.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     TRAIN CONSIST MANAGEMENT APP");
        System.out.println("========================================\n");

        System.out.println("--- Building Consist A ---");
        TrainConsist consistA = ConsistBuilder.buildConsist();
        ConsistPrinter.printSummary(consistA);

        FleetRegistry registry = FleetRegistry.getInstance();
        MarshalService.registerAll(consistA, registry);

        JourneyLog journeyLog = new JourneyLog(consistA.getTrainNumber());

        RouteType routeType = readRouteType();
        ValidationResult result = SafetyValidator.validate(consistA.getBogies(), routeType);
        ValidationPrinter.printReport(result);

        System.out.println("========================================");
        System.out.println("         BOGIE SEARCH");
        System.out.println("========================================");

        while (true) {
            String bogieId = readBogieId();
            if (bogieId.equalsIgnoreCase("exit")) break;
            Optional<BogieSearchResult> searchResult =
                    BogieSearchService.findById(consistA.getBogies(), bogieId);
            if (searchResult.isPresent())
                BogieSearchPrinter.printFound(searchResult.get(), consistA.getBogies().length);
            else
                BogieSearchPrinter.printNotFound(bogieId, consistA.getTrainNumber());
            System.out.print("Search another bogie? (yes / no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) break;
        }

        JunctionOperationsMenu.run(consistA, routeType, journeyLog);
        ShuntingMenu.run(consistA, journeyLog);
        JourneyLogMenu.run(journeyLog);

        System.out.println("\n--- Building Consist B (for marshal) ---");
        TrainConsist consistB = ConsistBuilder.buildConsist();
        ConsistPrinter.printSummary(consistB);
        MarshalMenu.run(consistA, consistB, registry);

        ManifestMenu.run(journeyLog, consistA);
        LoadPlanMenu.run(consistA);
        FleetDashboardMenu.run(consistA);
        ScheduleMenu.run(consistA);

        // ── UC-12: Departure sort ──────────────────────────────────────────────
        DepartureSortMenu.run(consistA);

        scanner.close();
    }

    private static RouteType readRouteType() {
        while (true) {
            System.out.print("Enter Route Type (SUBURBAN / EXPRESS / FREIGHT): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try { return RouteType.valueOf(input); }
            catch (IllegalArgumentException e) {
                System.out.println("  Invalid route type. Valid options: SUBURBAN, EXPRESS, FREIGHT");
            }
        }
    }

    private static String readBogieId() {
        while (true) {
            System.out.print("Enter Bogie ID to search (or 'exit' to quit): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("  Bogie ID cannot be empty. Please try again.");
        }
    }
}
