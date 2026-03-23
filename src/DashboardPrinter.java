import java.util.Map;

/**
 * Prints the fleet analytics dashboard in a formatted layout (UC-13).
 *
 * <p>Each section of the dashboard corresponds to one stream pipeline result
 * from {@link AnalyticsService}. Absent values (OptionalDouble.empty(),
 * Optional.empty()) are printed descriptively rather than as null or exception.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class DashboardPrinter {

    /**
     * Prints the complete analytics dashboard from a DashboardReport.
     *
     * @param report the DashboardReport containing all analytics results
     */
    public static void print(DashboardReport report) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       FLEET ANALYTICS DASHBOARD          ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // ── Section 1: Capacity by Type ───────────────────────────────────────
        System.out.println("\n  ── Capacity by Bogie Type ──────────────────");
        if (report.getCapacityByType().isEmpty()) {
            System.out.println("  No bogies in fleet.");
        } else {
            for (Map.Entry<BogieType, Integer> entry : report.getCapacityByType().entrySet()) {
                System.out.printf("  %-12s : %d%n", entry.getKey(), entry.getValue());
            }
        }

        // ── Section 2: Average Freight Tonnage ────────────────────────────────
        System.out.println("\n  ── Average Freight Tonnage ─────────────────");
        if (report.getAvgFreight().isPresent()) {
            System.out.printf("  Average : %.2f tonnes%n", report.getAvgFreight().getAsDouble());
        } else {
            // UC-13 edge flow: no GOODS bogies in fleet
            System.out.println("  Average : N/A (no GOODS bogies in fleet)");
        }

        // ── Section 3: Overloaded Bogies ──────────────────────────────────────
        System.out.println("\n  ── Overloaded Bogies (capacity > "
                + report.getOverloadThreshold() + ") ──────");
        if (report.getOverloaded().isEmpty()) {
            // UC-13 edge flow: no overloaded bogies
            System.out.println("  All within limits.");
        } else {
            System.out.println("  Count : " + report.getOverloaded().size());
            for (Bogie b : report.getOverloaded()) {
                System.out.printf("  [!] %-10s %-12s capacity: %d%n",
                        b.getBogieId(), b.getBogieType(), b.getCapacity());
            }
        }

        // ── Section 4: Top Freight Station ───────────────────────────────────
        System.out.println("\n  ── Station with Most Freight ───────────────");
        if (report.getTopStation().isPresent()) {
            StationStop top = report.getTopStation().get();
            System.out.println("  Station : " + top.getStationName());
            System.out.println("  Tonnage : " + top.totalTonnage() + " tonnes");
            System.out.println("  Bogies  : " + top.getBogiesDetached().size());
        } else {
            // UC-13 edge flow: no stations in schedule
            System.out.println("  No station data available.");
        }

        System.out.println("\n══════════════════════════════════════════════\n");
    }
}
