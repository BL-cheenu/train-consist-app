import java.util.Scanner;

/**
 * Interactive console menu for load plan operations (UC-09).
 *
 * <p>Presents three choices:
 * <ul>
 *   <li><b>NATURAL</b> — sort bogies by capacity ascending (natural Comparable order).</li>
 *   <li><b>CUSTOM</b> — sort bogies GOODS-first, then capacity ascending.</li>
 *   <li><b>DONE</b> — exit the load plan menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class LoadPlanMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the load plan menu loop until the planner selects DONE.
     *
     * @param consist the TrainConsist whose bogies will be sorted and analysed
     */
    public static void run(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("       LOAD PLAN — UC-09");
        System.out.println("========================================");

        while (true) {
            System.out.println("Actions: NATURAL | CUSTOM | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "NATURAL":
                    // Natural order: capacity ascending, bogieId tiebreaker
                    LoadPlan naturalPlan = LoadPlanService.buildNaturalPlan(
                            consist.getBogieList());
                    LoadPlanPrinter.printPlan(naturalPlan, "NATURAL ORDER (capacity ascending)");
                    break;

                case "CUSTOM":
                    // Custom order: GOODS first, then capacity, then bogieId
                    LoadPlan customPlan = LoadPlanService.buildCustomPlan(
                            consist.getBogieList());
                    LoadPlanPrinter.printPlan(customPlan, "GOODS-FIRST ORDER");
                    break;

                case "DONE":
                    System.out.println("\n  Load planning complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose NATURAL, CUSTOM, or DONE.");
            }
        }
    }
}
