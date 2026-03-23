import java.util.List;

/**
 * Prints query results from ConsistQueryService to the console (UC-14).
 *
 * <p>All Optional.empty() cases produce a clear descriptive message —
 * never silent failure or NullPointerException.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class QueryPrinter {

    /**
     * Prints the result of a HEAVIEST_OF_TYPE query.
     *
     * @param result the QueryResult wrapping Optional<Bogie>
     */
    public static void printHeaviestResult(QueryResult<Bogie> result) {
        System.out.println("\n----------------------------------------");
        System.out.println("  Query: " + result.getQueryDescription());
        System.out.println("  Time : " + result.getExecutionTimeNs() + " ns");
        System.out.println("----------------------------------------");

        // Optional.ifPresentOrElse pattern — safe, no null check
        if (result.getResult().isPresent()) {
            Bogie b = result.getResult().get();
            System.out.println("  Found  : " + b.getBogieId());
            System.out.println("  Type   : " + b.getBogieType());
            System.out.println("  Subtype: " + b.getSubType());
            System.out.println("  Cap    : " + b.getCapacity());
            System.out.println("  Weight : " + b.getWeight() + "t");
        } else {
            System.out.println("  Result : Not found — no bogies match this query.");
        }
        System.out.println("----------------------------------------\n");
    }

    /**
     * Prints the result of a PROXIMITY_CHECK query.
     *
     * @param result the QueryResult wrapping Optional<ProximityViolation>
     */
    public static void printProximityResult(QueryResult<ProximityViolation> result) {
        System.out.println("\n----------------------------------------");
        System.out.println("  Query: " + result.getQueryDescription());
        System.out.println("  Time : " + result.getExecutionTimeNs() + " ns");
        System.out.println("----------------------------------------");

        if (result.getResult().isPresent()) {
            ProximityViolation v = result.getResult().get();
            System.out.println("  [!] VIOLATION FOUND");
            System.out.println("  Source   : " + v.getSourceBogieId() + " @ pos " + v.getSourcePosition());
            System.out.println("  Conflict : " + v.getConflictingBogieId() + " @ pos " + v.getConflictPosition());
            System.out.println("  Window   : ±" + v.getDistance() + " positions");
            System.out.println("  Reason   : " + v.getReason());
        } else {
            System.out.println("  Result : CLEAR — no proximity violations in window.");
        }
        System.out.println("----------------------------------------\n");
    }

    /**
     * Prints the result of a COMPOUND_FILTER query.
     *
     * @param bogies      the filtered bogie list
     * @param description description of the compound filter applied
     */
    public static void printCompoundFilterResult(List<Bogie> bogies, String description) {
        System.out.println("\n----------------------------------------");
        System.out.println("  Query: " + description);
        System.out.println("----------------------------------------");

        if (bogies.isEmpty()) {
            System.out.println("  Result : No bogies match the compound filter.");
        } else {
            System.out.println("  Matches: " + bogies.size());
            System.out.printf("  %-10s %-12s %-14s %-8s %-8s%n",
                    "ID", "Type", "Subtype", "Cap", "Weight");
            System.out.println("  " + "-".repeat(56));
            for (Bogie b : bogies) {
                System.out.printf("  %-10s %-12s %-14s %-8d %-8d%n",
                        b.getBogieId(), b.getBogieType(),
                        b.getSubType(), b.getCapacity(), b.getWeight());
            }
        }
        System.out.println("----------------------------------------\n");
    }

    /**
     * Prints the result of a SAFE_CARGO_UPDATE query.
     *
     * @param result  Optional<Bogie> — present if updated, empty if bogie not found
     * @param bogieId the target bogie ID
     */
    public static void printCargoUpdateResult(java.util.Optional<Bogie> result, String bogieId) {
        System.out.println("\n----------------------------------------");
        if (result.isPresent()) {
            Bogie b = result.get();
            System.out.println("  Cargo updated: " + bogieId);
            System.out.println("  Weight : " + b.getCargoWeight() + "t");
            System.out.println("  Desc   : " + b.getCargoDesc());
        } else {
            System.out.println("  Bogie '" + bogieId + "' not found — no update performed.");
        }
        System.out.println("----------------------------------------\n");
    }
}
