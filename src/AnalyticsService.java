import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Computes fleet analytics using Java 8 stream pipelines (UC-13).
 *
 * <p>Each public method is a separate, single-concern stream pipeline.
 * No method mixes multiple metrics in one chain — each answers one question.</p>
 *
 * <p>Stream pipeline pattern: source → intermediate ops (filter/map/sorted) →
 * terminal op (collect/average/max/forEach). Streams are lazy — nothing
 * executes until the terminal op is called.</p>
 *
 * <p>Method references ({@code Bogie::getCapacity}) replace lambdas wherever
 * the lambda just delegates to a single existing method.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class AnalyticsService {

    /**
     * Computes total capacity grouped by BogieType.
     *
     * <p>Stream pipeline:
     * <pre>
     *   bogies.stream()
     *         .collect(groupingBy(Bogie::getBogieType, summingInt(Bogie::getCapacity)))
     * </pre>
     * {@code groupingBy} partitions bogies by type; {@code summingInt} is the downstream
     * collector that sums capacity within each group. Equivalent to SQL:
     * {@code SELECT bogieType, SUM(capacity) FROM bogies GROUP BY bogieType}.</p>
     *
     * @param bogies the fleet bogie list to aggregate
     * @return map of BogieType → total capacity
     */
    public static Map<BogieType, Integer> capacityByType(List<Bogie> bogies) {
        return bogies.stream()
                .collect(Collectors.groupingBy(
                        Bogie::getBogieType,                  // key function — method reference
                        Collectors.summingInt(Bogie::getCapacity))); // downstream collector
    }

    /**
     * Computes the average freight tonnage across all GOODS bogies.
     *
     * <p>Stream pipeline:
     * <pre>
     *   bogies.stream()
     *         .filter(b -> b.getBogieType() == BogieType.GOODS)
     *         .mapToInt(Bogie::getCapacity)
     *         .average()
     * </pre>
     * Returns {@link OptionalDouble#empty()} if there are no GOODS bogies —
     * the dashboard prints "N/A" in that case.</p>
     *
     * @param bogies the fleet bogie list
     * @return OptionalDouble containing the average, or empty if no GOODS bogies
     */
    public static OptionalDouble averageFreightTonnage(List<Bogie> bogies) {
        return bogies.stream()
                .filter(b -> b.getBogieType() == BogieType.GOODS) // keep GOODS only
                .mapToInt(Bogie::getCapacity)                      // method reference
                .average();                                        // terminal op → OptionalDouble
    }

    /**
     * Finds all bogies whose capacity exceeds the given threshold.
     *
     * <p>Stream pipeline:
     * <pre>
     *   bogies.stream()
     *         .filter(b -> b.getCapacity() > threshold)
     *         .collect(Collectors.toList())
     * </pre>
     * Threshold is passed as a parameter — never hardcoded.</p>
     *
     * @param bogies    the fleet bogie list
     * @param threshold capacity above which a bogie is considered overloaded
     * @return list of overloaded bogies (empty if all within limits)
     */
    public static List<Bogie> findOverloaded(List<Bogie> bogies, int threshold) {
        return bogies.stream()
                .filter(b -> b.getCapacity() > threshold)  // configurable threshold
                .collect(Collectors.toList());
    }

    /**
     * Finds the station with the highest total freight tonnage.
     *
     * <p>Stream pipeline:
     * <pre>
     *   stations.stream()
     *           .max(Comparator.comparingInt(StationStop::totalTonnage))
     * </pre>
     * Returns {@link Optional#empty()} if the collection is empty.</p>
     *
     * @param stations the collection of station stops to evaluate
     * @return Optional containing the station with most freight, or empty if no stations
     */
    public static Optional<StationStop> topFreightStation(Collection<StationStop> stations) {
        return stations.stream()
                .max(Comparator.comparingInt(StationStop::totalTonnage)); // method reference
    }

    /**
     * Prints all bogies sorted by capacity ascending using stream().sorted().forEach().
     *
     * <p>Stream pipeline:
     * <pre>
     *   bogies.stream()
     *         .sorted(Comparator.comparingInt(Bogie::getCapacity))
     *         .forEach(System.out::println)
     * </pre>
     * {@code System.out::println} is a method reference replacing {@code b -> System.out.println(b)}.</p>
     *
     * @param bogies the list of bogies to print in sorted order
     */
    public static void printSortedManifest(List<Bogie> bogies) {
        System.out.println("\n  Sorted Manifest (capacity ascending):");
        System.out.println("  " + "-".repeat(50));
        bogies.stream()
                .sorted(Comparator.comparingInt(Bogie::getCapacity)  // method reference
                                  .thenComparing(Bogie::getBogieId)) // stable tiebreaker
                .forEach(System.out::println);                        // method reference
    }

    /**
     * Builds a complete DashboardReport by running all five analytics pipelines.
     *
     * @param bogies            the fleet bogie list
     * @param stations          the route's station stops
     * @param overloadThreshold capacity threshold for overload detection
     * @return a populated DashboardReport
     */
    public static DashboardReport buildReport(List<Bogie> bogies,
                                              Collection<StationStop> stations,
                                              int overloadThreshold) {
        return new DashboardReport(
                capacityByType(bogies),
                averageFreightTonnage(bogies),
                findOverloaded(bogies, overloadThreshold),
                topFreightStation(stations),
                overloadThreshold);
    }
}
