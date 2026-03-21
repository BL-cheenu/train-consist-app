import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for yard shunting operations (UC-05).
 * Updated in UC-06 to pass the shared {@link JourneyLog} to every mutation
 * so events are automatically recorded in the audit trail.
 */
public class ShuntingMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the shunting menu loop until the operator selects DONE.
     *
     * @param consist    the TrainConsist to operate on
     * @param journeyLog the shared log — every mutation appends an event here
     */
    public static void run(TrainConsist consist, JourneyLog journeyLog) {
        List<Bogie> bogies = consist.getBogieList();

        System.out.println("\n========================================");
        System.out.println("       YARD SHUNTING — UC-05");
        System.out.println("========================================");

        while (true) {
            ConsistPrinter.printSummary(consist);
            System.out.println("Actions: MOVE_TO_FRONT | ADD_BRAKE_VAN | SWAP_ADJACENT | BENCHMARK | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "MOVE_TO_FRONT":   handleMoveToFront(bogies, journeyLog);  break;
                case "ADD_BRAKE_VAN":   handleAddBrakeVan(bogies, journeyLog);  break;
                case "SWAP_ADJACENT":   handleSwapAdjacent(bogies, journeyLog); break;
                case "BENCHMARK":       handleBenchmark();                        break;
                case "DONE":
                    System.out.println("\n  Shunting complete.\n");
                    return;
                default:
                    System.out.println("  Invalid action. Try again.");
            }
        }
    }

    /** Reads a bogie ID and moves it to position 0. Passes actorId "SHUNTER". */
    private static void handleMoveToFront(List<Bogie> bogies, JourneyLog log) {
        System.out.print("  Enter Bogie ID to move to front: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) { System.out.println("  Bogie ID cannot be empty."); return; }
        boolean moved = ShuntingService.moveToFront(bogies, bogieId, log, "SHUNTER");
        if (moved) System.out.println("  Bogie '" + bogieId + "' moved to front.\n");
    }

    /** Creates a brake van and appends to rear. Passes actorId "SHUNTER". */
    private static void handleAddBrakeVan(List<Bogie> bogies, JourneyLog log) {
        System.out.print("  Enter Brake Van Bogie ID: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) { System.out.println("  Bogie ID cannot be empty."); return; }
        Bogie brakeVan = new GoodsBogie(bogieId, GoodsSubType.RECTANGULAR, 10);
        ShuntingService.addBrakeVan(bogies, brakeVan, log, "SHUNTER");
        System.out.println("  Brake van '" + bogieId + "' attached at rear.\n");
    }

    /** Reads a position and swaps it with its neighbour. Passes actorId "SHUNTER". */
    private static void handleSwapAdjacent(List<Bogie> bogies, JourneyLog log) {
        System.out.print("  Enter position i to swap with i+1: ");
        String input = scanner.nextLine().trim();
        try {
            int position = Integer.parseInt(input);
            boolean swapped = ShuntingService.swapAdjacent(bogies, position, log, "SHUNTER");
            if (swapped)
                System.out.println("  Swapped positions " + position + " and " + (position + 1) + ".\n");
        } catch (NumberFormatException e) {
            System.out.println("  Invalid position. Please enter a number.");
        }
    }

    /** Runs the ArrayList vs LinkedList benchmark and prints results. */
    private static void handleBenchmark() {
        int iterations = 10_000;
        System.out.println("\n  Running benchmark — " + iterations + " iterations...\n");
        System.out.printf("  %-20s | %-12s | %9s | %s%n", "Operation", "Collection", "Count", "Time");
        System.out.println("  " + "-".repeat(57));
        List<BenchmarkResult> results = ShuntingService.benchmark(iterations);
        for (BenchmarkResult r : results) System.out.println("  " + r);
        System.out.println("\n  Insight:");
        System.out.println("  HEAD_INSERT  — LinkedList O(1) vs ArrayList O(n) shift");
        System.out.println("  RANDOM_GET   — ArrayList O(1) vs LinkedList O(n) traversal\n");
    }
}
