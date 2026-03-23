import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Prints the station schedule manifest in two sorted views (UC-11).
 *
 * <p>Both methods use {@code entrySet()} with {@code Map.Entry} to access
 * both the key (station name or time) and value (StationStop) simultaneously —
 * no second {@code get()} call needed.</p>
 *
 * <p>Summary totals (total stations, total bogies detached, total tonnage)
 * are computed during the same single iteration pass — not in a separate loop.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class SchedulePrinter {

    /** Formatter for departure times in HH:mm format. */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Prints the freight manifest in alphabetical station order (A→Z).
     *
     * <p>Iterates {@code TreeMap.entrySet()} — natural String key ordering
     * guarantees alphabetical output without any sort code.</p>
     *
     * <p>Summary totals computed in the same pass: total stations, total bogies,
     * and total tonnage across the entire route.</p>
     *
     * @param schedule the RouteSchedule containing the alphabetical TreeMap
     */
    public static void printAlphabetical(RouteSchedule schedule) {
        System.out.println("\n========================================");
        System.out.println("   FREIGHT MANIFEST — ALPHABETICAL");
        System.out.println("========================================");

        // Accumulators — computed during single entrySet() pass
        int totalBogies = 0;
        int totalTonnage = 0;

        // entrySet() iterates in A→Z order (TreeMap natural String ordering)
        for (Map.Entry<String, StationStop> entry : schedule.getAlphabetical().entrySet()) {
            String stationKey = entry.getKey();
            StationStop stop = entry.getValue();

            printStationRow(stationKey, stop);

            // Accumulate totals in same pass — no second iteration needed
            totalBogies += stop.getBogiesDetached().size();
            totalTonnage += stop.totalTonnage();
        }

        printSummary(schedule.size(), totalBogies, totalTonnage);
    }

    /**
     * Prints the station schedule in departure-time order (chronological).
     *
     * <p>Iterates {@code TreeMap.entrySet()} — natural LocalTime key ordering
     * guarantees chronological output without any sort code.</p>
     *
     * <p>Summary totals computed in the same pass.</p>
     *
     * @param schedule the RouteSchedule containing the time-sorted TreeMap
     */
    public static void printByDepartureTime(RouteSchedule schedule) {
        System.out.println("\n========================================");
        System.out.println("   DEPARTURE SCHEDULE — TIME ORDER");
        System.out.println("========================================");

        // Accumulators — computed during single entrySet() pass
        int totalBogies = 0;
        int totalTonnage = 0;

        // entrySet() iterates in chronological order (TreeMap natural LocalTime ordering)
        for (Map.Entry<LocalTime, StationStop> entry : schedule.getByTime().entrySet()) {
            LocalTime timeKey = entry.getKey();
            StationStop stop = entry.getValue();

            // Print departure time alongside station row
            System.out.printf("  [%s] ", timeKey.format(TIME_FORMATTER));
            printStationRow(stop.getStationName(), stop);

            // Accumulate totals in same pass
            totalBogies += stop.getBogiesDetached().size();
            totalTonnage += stop.totalTonnage();
        }

        printSummary(schedule.size(), totalBogies, totalTonnage);
    }

    /**
     * Prints a single station row — name, bogie list, and total tonnage.
     * If no bogies are detached, prints "0 tonnes" and "No detachments".
     *
     * @param stationKey the map key (station name or suffixed name for duplicates)
     * @param stop       the StationStop to display
     */
    private static void printStationRow(String stationKey, StationStop stop) {
        System.out.println("\n  Station : " + stationKey);
        System.out.println("  Departs : " + stop.getDepartureTime().format(TIME_FORMATTER));

        if (stop.getBogiesDetached().isEmpty()) {
            // UC-11 edge flow: station with no detachments — still appears, shows 0 tonnes
            System.out.println("  Bogies  : No detachments at this station");
            System.out.println("  Tonnage : 0 tonnes");
        } else {
            System.out.print("  Bogies  : ");
            // Use Map.Entry pattern — print bogie details without extra lookup
            for (int i = 0; i < stop.getBogiesDetached().size(); i++) {
                Bogie b = stop.getBogiesDetached().get(i);
                if (i > 0) System.out.print(", ");
                System.out.print(b.getBogieId() + " (" + b.getCapacity() + "t)");
            }
            System.out.println();
            System.out.println("  Tonnage : " + stop.totalTonnage() + " tonnes");
        }
        System.out.println("  " + "-".repeat(44));
    }

    /**
     * Prints the route-level summary totals at the end of each manifest.
     * Computed during the same iteration pass as the row printing.
     *
     * @param totalStations total number of stations on the route
     * @param totalBogies   total bogies detached across all stations
     * @param totalTonnage  total freight tonnage across all stations
     */
    private static void printSummary(int totalStations, int totalBogies, int totalTonnage) {
        System.out.println("\n  ROUTE SUMMARY");
        System.out.println("  " + "=".repeat(44));
        System.out.println("  Total Stations       : " + totalStations);
        System.out.println("  Total Bogies Detached: " + totalBogies);
        System.out.println("  Total Tonnage        : " + totalTonnage + " tonnes");
        System.out.println("========================================\n");
    }
}
