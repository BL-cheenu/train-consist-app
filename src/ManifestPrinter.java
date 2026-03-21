import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Prints the safety manifest as a formatted table (UC-08).
 *
 * <p>Output format:
 * <pre>
 * ============================================
 *      SAFETY MANIFEST — TN-2201
 *      Generated: 2025-06-01 10:30:00
 * ============================================
 *   #    Bogie ID     Type         Subtype        Capacity
 *   ---------------------------------------------------
 *   1    BG-01        PASSENGER    SLEEPER        72
 *   2    BG-02        GOODS        RECTANGULAR    100
 * ============================================
 *   Total Passenger Capacity : 72 seats
 *   Total Freight Tonnage    : 100 tons
 * ============================================
 * </pre>
 *
 * <p>Bogie details (type, subtype, capacity) are looked up from the consist
 * by matching the bogie ID from the manifest's LinkedHashSet.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ManifestPrinter {

    /** Formatter for the generated-at timestamp on the manifest header. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Prints the full safety manifest to the console.
     *
     * <p>If the manifest is empty (no ATTACHED events in the log), prints a warning
     * message instead of an empty table.</p>
     *
     * @param manifest the SafetyManifest to print
     */
    public static void print(SafetyManifest manifest) {
        System.out.println("\n============================================");
        System.out.println("      SAFETY MANIFEST — " + manifest.getTrainNumber());
        System.out.println("      Generated: " + manifest.getGeneratedAt().format(FORMATTER));
        System.out.println("============================================");

        if (manifest.isEmpty()) {
            // UC-08 edge flow: no ATTACHED events in the log
            System.out.println("  WARNING: No bogies in manifest. No ATTACHED events recorded.");
            System.out.println("============================================\n");
            return;
        }

        // Build a lookup map from the consist: bogieId → Bogie
        // This lets us print full details for each ID in the LinkedHashSet
        Map<String, Bogie> bogieMap = buildBogieMap(manifest.getConsist());

        // Print column headers
        System.out.printf("  %-4s %-12s %-12s %-14s %-8s%n",
                "#", "Bogie ID", "Type", "Subtype", "Capacity");
        System.out.println("  " + "-".repeat(54));

        int position = 1;
        int totalPassenger = 0;
        int totalFreight = 0;

        // Iterate LinkedHashSet — guaranteed to yield IDs in insertion order
        for (String bogieId : manifest.getBogiesInOrder()) {
            Bogie b = bogieMap.get(bogieId);

            if (b == null) {
                // Bogie was in the log but not in the current consist (e.g. fully detached)
                System.out.printf("  %-4d %-12s %-12s %-14s %-8s%n",
                        position, bogieId, "UNKNOWN", "UNKNOWN", "N/A");
            } else {
                System.out.printf("  %-4d %-12s %-12s %-14s %-8d%n",
                        position, b.getBogieId(), b.getBogieType(),
                        b.getSubType(), b.getCapacity());

                // Accumulate capacity totals for the manifest footer
                if (b.getBogieType() == BogieType.PASSENGER) {
                    totalPassenger += b.getCapacity();
                } else {
                    totalFreight += b.getCapacity();
                }
            }
            position++;
        }

        // Print capacity totals footer
        System.out.println("============================================");
        if (totalPassenger == 0) {
            System.out.println("  Total Passenger Capacity : No passenger coaches");
        } else {
            System.out.println("  Total Passenger Capacity : " + totalPassenger + " seats");
        }
        System.out.println("  Total Freight Tonnage    : " + totalFreight + " tons");
        System.out.println("============================================\n");
    }

    /**
     * Builds a Map from bogieId to Bogie from the consist for O(1) lookup during printing.
     *
     * @param consist the TrainConsist to index
     * @return map of bogieId → Bogie
     */
    private static Map<String, Bogie> buildBogieMap(TrainConsist consist) {
        Map<String, Bogie> map = new HashMap<>();
        for (Bogie b : consist.getBogies()) {
            map.put(b.getBogieId(), b);
        }
        return map;
    }
}
