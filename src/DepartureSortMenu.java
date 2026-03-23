import java.util.Comparator;
import java.util.Scanner;

/**
 * Interactive console menu for departure sort operations (UC-12).
 *
 * <p>Presents three choices:
 * <ul>
 *   <li><b>WEIGHT</b> — sort by weight ascending; runs both BubbleSort and TimSort,
 *       prints telemetry and confirms identical results.</li>
 *   <li><b>MULTI_KEY</b> — sort by PASSENGER-first, then weight, then bogieId
 *       using Comparator.comparing chain.</li>
 *   <li><b>DONE</b> — exit the menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class DepartureSortMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the departure sort menu loop until the controller selects DONE.
     *
     * @param consist the TrainConsist to sort
     */
    public static void run(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("   DEPARTURE SORT — UC-12");
        System.out.println("========================================");

        while (true) {
            System.out.println("Actions: WEIGHT | MULTI_KEY | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "WEIGHT":
                    handleWeightSort(consist);
                    break;

                case "MULTI_KEY":
                    handleMultiKeySort(consist);
                    break;

                case "DONE":
                    System.out.println("\n  Departure sort complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose WEIGHT, MULTI_KEY, or DONE.");
            }
        }
    }

    /**
     * Runs weight-ascending sort using both BubbleSort and TimSort,
     * then compares results to confirm they are identical.
     *
     * @param consist the consist to sort
     */
    private static void handleWeightSort(TrainConsist consist) {
        Comparator<Bogie> comparator = DepartureSortService.weightComparator();

        // BubbleSort — O(n²) with pass/swap telemetry
        DepartureSortResult bubbleResult = BubbleSortService.sort(
                consist.getBogieList(), comparator);
        DepartureSortPrinter.printResult(bubbleResult, "WEIGHT SORT — BubbleSort");

        // TimSort — O(n log n) via Collections.sort
        DepartureSortResult timsortResult = DepartureSortService.sort(
                consist.getBogieList(), comparator);
        DepartureSortPrinter.printResult(timsortResult, "WEIGHT SORT — TimSort");

        // Confirm both algorithms agree
        DepartureSortPrinter.printComparison(bubbleResult, timsortResult);
    }

    /**
     * Runs multi-key sort (PASSENGER first → weight → bogieId) using TimSort only.
     *
     * @param consist the consist to sort
     */
    private static void handleMultiKeySort(TrainConsist consist) {
        Comparator<Bogie> comparator = DepartureSortService.multiKeyComparator();

        DepartureSortResult result = DepartureSortService.sort(
                consist.getBogieList(), comparator);
        DepartureSortPrinter.printResult(result, "MULTI-KEY SORT (PASSENGER first → weight → ID)");
    }
}
