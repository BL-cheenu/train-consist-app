import java.util.Scanner;

/**
 * Interactive console menu for fleet marshal operations (UC-07).
 *
 * <p>Allows the yard master to:
 * <ul>
 *   <li><b>MERGE</b> — attempt to merge consist B into consist A with duplicate detection.</li>
 *   <li><b>VIEW_REGISTRY</b> — display the current state of the fleet registry.</li>
 *   <li><b>DONE</b> — exit the marshal menu.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class MarshalMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the marshal menu loop until the yard master selects DONE.
     *
     * @param consistA the primary consist — target of any merge operation
     * @param consistB the secondary consist — source bogies to merge into A
     * @param registry the singleton fleet registry used for conflict detection
     */
    public static void run(TrainConsist consistA, TrainConsist consistB,
                           FleetRegistry registry) {
        System.out.println("\n========================================");
        System.out.println("       FLEET MARSHAL — UC-07");
        System.out.println("========================================");
        System.out.println("  Consist A : " + consistA.getTrainNumber()
                + " (" + consistA.getBogieList().size() + " bogies)");
        System.out.println("  Consist B : " + consistB.getTrainNumber()
                + " (" + consistB.getBogieList().size() + " bogies)");
        System.out.println("========================================");

        while (true) {
            System.out.println("Actions: MERGE | VIEW_REGISTRY | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "MERGE":
                    // Attempt atomic merge — either all commit or none
                    MarshalResult result = MarshalService.merge(consistA, consistB, registry);
                    MarshalPrinter.print(result);
                    break;

                case "VIEW_REGISTRY":
                    printRegistry(registry);
                    break;

                case "DONE":
                    System.out.println("\n  Marshal operations complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose MERGE, VIEW_REGISTRY, or DONE.");
            }
        }
    }

    /**
     * Prints the total count of IDs currently in the fleet registry.
     * Indicates whether the registry is empty or populated.
     *
     * @param registry the fleet registry to display
     */
    private static void printRegistry(FleetRegistry registry) {
        System.out.println("\n----------------------------------------");
        System.out.println("  Fleet Registry");
        System.out.println("----------------------------------------");
        if (registry.isEmpty()) {
            System.out.println("  Registry is empty — no bogies registered yet.");
        } else {
            System.out.println("  Total registered IDs: " + registry.size());
        }
        System.out.println("----------------------------------------\n");
    }
}
