import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for yard shunting operations (UC-05).
 *
 * <p>Presents a loop with five choices:
 * <ul>
 *   <li><b>MOVE_TO_FRONT</b> — relocate a bogie to position 0 by ID.</li>
 *   <li><b>ADD_BRAKE_VAN</b> — append a brake van to the rear of the consist.</li>
 *   <li><b>SWAP_ADJACENT</b> — swap two neighbouring bogies at position i and i+1.</li>
 *   <li><b>BENCHMARK</b> — run and display the ArrayList vs LinkedList timing comparison.</li>
 *   <li><b>DONE</b> — exit the shunting menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class ShuntingMenu {

    /** Shared Scanner instance bound to System.in for all menu input. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the shunting menu loop until the operator selects DONE.
     * Displays the current consist before each prompt.
     *
     * @param consist the TrainConsist to operate on (uses getBogieList() for live mutation)
     */
    public static void run(TrainConsist consist) {
        // Get the live mutable list — all shunting operations modify this directly
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
                case "MOVE_TO_FRONT":   handleMoveToFront(bogies);  break;
                case "ADD_BRAKE_VAN":   handleAddBrakeVan(bogies);  break;
                case "SWAP_ADJACENT":   handleSwapAdjacent(bogies); break;
                case "BENCHMARK":       handleBenchmark();           break;
                case "DONE":
                    System.out.println("\n  Shunting complete.\n");
                    return;
                default:
                    System.out.println("  Invalid action. Try again.");
            }
        }
    }

    /**
     * Reads a bogie ID and moves that bogie to position 0.
     * Delegates to {@link ShuntingService#moveToFront}.
     *
     * @param bogies the live bogie list to modify
     */
    private static void handleMoveToFront(List<Bogie> bogies) {
        System.out.print("  Enter Bogie ID to move to front: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) {
            System.out.println("  Bogie ID cannot be empty.");
            return;
        }
        boolean moved = ShuntingService.moveToFront(bogies, bogieId);
        if (moved) System.out.println("  Bogie '" + bogieId + "' moved to front.\n");
    }

    /**
     * Reads a brake van ID and appends a new GOODS/RECTANGULAR bogie to the rear.
     * Delegates to {@link ShuntingService#addBrakeVan}.
     *
     * @param bogies the live bogie list to append to
     */
    private static void handleAddBrakeVan(List<Bogie> bogies) {
        System.out.print("  Enter Brake Van Bogie ID: ");
        String bogieId = scanner.nextLine().trim();
        if (bogieId.isEmpty()) {
            System.out.println("  Bogie ID cannot be empty.");
            return;
        }
        // Brake van is modelled as a RECTANGULAR GOODS bogie with minimal capacity
        Bogie brakeVan = new GoodsBogie(bogieId, GoodsSubType.RECTANGULAR, 10);
        ShuntingService.addBrakeVan(bogies, brakeVan);
        System.out.println("  Brake van '" + bogieId + "' attached at rear.\n");
    }

    /**
     * Reads a position index and swaps the bogie at that position with its neighbour.
     * Delegates to {@link ShuntingService#swapAdjacent}.
     * Rejects non-numeric input with an error message.
     *
     * @param bogies the live bogie list to modify
     */
    private static void handleSwapAdjacent(List<Bogie> bogies) {
        System.out.print("  Enter position i to swap with i+1: ");
        String input = scanner.nextLine().trim();
        try {
            int position = Integer.parseInt(input);
            boolean swapped = ShuntingService.swapAdjacent(bogies, position);
            if (swapped) {
                System.out.println("  Swapped positions " + position
                        + " and " + (position + 1) + ".\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Invalid position. Please enter a number.");
        }
    }

    /**
     * Runs the ArrayList vs LinkedList benchmark and prints the results with Big-O insights.
     * Delegates to {@link ShuntingService#benchmark}.
     */
    private static void handleBenchmark() {
        int iterations = 10_000;
        System.out.println("\n  Running benchmark — " + iterations + " iterations...\n");

        // Print table header
        System.out.printf("  %-20s | %-12s | %9s | %s%n",
                "Operation", "Collection", "Count", "Time");
        System.out.println("  " + "-".repeat(57));

        // Print each result row — BenchmarkResult.toString() formats as a fixed-width row
        List<BenchmarkResult> results = ShuntingService.benchmark(iterations);
        for (BenchmarkResult r : results) System.out.println("  " + r);

        // Print the teaching insight below the table
        System.out.println("\n  Insight:");
        System.out.println("  HEAD_INSERT  — LinkedList O(1) vs ArrayList O(n) shift");
        System.out.println("  RANDOM_GET   — ArrayList O(1) vs LinkedList O(n) traversal\n");
    }
}
