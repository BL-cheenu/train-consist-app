import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Interactive console menu for smart consist queries (UC-14).
 *
 * <p>Four query types:
 * <ul>
 *   <li><b>HEAVIEST_OF_TYPE</b> — finds heaviest bogie of operator-chosen type.</li>
 *   <li><b>PROXIMITY_CHECK</b> — checks CYLINDRICAL/PASSENGER conflict in window.</li>
 *   <li><b>COMPOUND_FILTER</b> — type + min capacity predicate composition.</li>
 *   <li><b>SAFE_CARGO_UPDATE</b> — Optional.ifPresent cargo update via BogieIndex.</li>
 * </ul>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class QueryMenu {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the query menu loop until the operator selects DONE.
     *
     * @param bogies the consist bogie list to query against
     * @param index  the pre-built BogieIndex for O(1) lookup
     */
    public static void run(List<Bogie> bogies, BogieIndex index) {
        System.out.println("\n========================================");
        System.out.println("   SMART QUERY ENGINE — UC-14");
        System.out.println("========================================");

        while (true) {
            System.out.println("Queries: HEAVIEST_OF_TYPE | PROXIMITY_CHECK | COMPOUND_FILTER | SAFE_CARGO_UPDATE | DONE");
            System.out.print("Enter query: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "HEAVIEST_OF_TYPE":
                    handleHeaviestOfType(bogies);
                    break;

                case "PROXIMITY_CHECK":
                    handleProximityCheck(bogies);
                    break;

                case "COMPOUND_FILTER":
                    handleCompoundFilter(bogies);
                    break;

                case "SAFE_CARGO_UPDATE":
                    handleSafeCargoUpdate(bogies, index);
                    break;

                case "DONE":
                    System.out.println("\n  Query session complete.\n");
                    return;

                default:
                    System.out.println("  Invalid query. Try again.");
            }
        }
    }

    /**
     * HEAVIEST_OF_TYPE — finds the heaviest bogie of operator-chosen type.
     * Demonstrates: stream().filter(predicate).max() returning Optional.
     */
    private static void handleHeaviestOfType(List<Bogie> bogies) {
        BogieType type = readBogieType();

        // Named predicate before use
        Predicate<Bogie> isType = new QueryBuilder().withType(type).build();

        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, isType);
        QueryPrinter.printHeaviestResult(result);
    }

    /**
     * PROXIMITY_CHECK — scans ±N positions around a bogie for CYLINDRICAL/PASSENGER conflicts.
     * Demonstrates: Optional, window clamping, ProximityViolation.
     */
    private static void handleProximityCheck(List<Bogie> bogies) {
        System.out.print("  Enter Bogie ID to check: ");
        String bogieId = scanner.nextLine().trim();

        System.out.print("  Enter distance window (e.g. 3): ");
        int distance = readPositiveInt();

        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, bogieId, distance);
        QueryPrinter.printProximityResult(result);
    }

    /**
     * COMPOUND_FILTER — type AND min capacity predicate composition.
     * Demonstrates: Predicate.and() via QueryBuilder chain.
     */
    private static void handleCompoundFilter(List<Bogie> bogies) {
        BogieType type = readBogieType();

        System.out.print("  Enter minimum capacity: ");
        int minCap = readPositiveInt();

        // Compose predicates with QueryBuilder — Predicate.and() under the hood
        Predicate<Bogie> composed = new QueryBuilder()
                .withType(type)
                .withMinCapacity(minCap)
                .build();

        List<Bogie> matches = ConsistQueryService.findAll(bogies, composed);
        QueryPrinter.printCompoundFilterResult(matches,
                type + " bogies with capacity >= " + minCap);
    }

    /**
     * SAFE_CARGO_UPDATE — Optional.ifPresent update via BogieIndex O(1) lookup.
     * Demonstrates: Optional.map().flatMap() chain — no null check, no NPE risk.
     */
    private static void handleSafeCargoUpdate(List<Bogie> bogies, BogieIndex index) {
        System.out.print("  Enter Bogie ID to update: ");
        String bogieId = scanner.nextLine().trim();

        System.out.print("  New cargo weight (tonnes): ");
        double weight = readNonNegativeDouble();

        System.out.print("  New cargo description: ");
        String desc = scanner.nextLine().trim();

        CargoUpdate update = new CargoUpdate(bogieId, weight, desc);
        Optional<Bogie> result = ConsistQueryService.safeCargoUpdate(index, update);
        QueryPrinter.printCargoUpdateResult(result, bogieId);
    }

    // ── Input helpers ─────────────────────────────────────────────────────────

    private static BogieType readBogieType() {
        while (true) {
            System.out.print("  Enter bogie type (PASSENGER / GOODS): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try { return BogieType.valueOf(input); }
            catch (IllegalArgumentException e) {
                System.out.println("  Invalid type. Valid: PASSENGER, GOODS");
            }
        }
    }

    private static int readPositiveInt() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val > 0) return val;
                System.out.println("  Must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Enter a whole number.");
            }
        }
    }

    private static double readNonNegativeDouble() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= 0) return val;
                System.out.println("  Cargo weight cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Enter a numeric value.");
            }
        }
    }
}
