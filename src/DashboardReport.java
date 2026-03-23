import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Immutable value object holding all analytics results for the fleet dashboard (UC-13).
 *
 * <p>Each field corresponds to one stream pipeline computed by {@link AnalyticsService}:
 * <ul>
 *   <li>{@code capacityByType} — total capacity per BogieType (groupingBy + summingInt).</li>
 *   <li>{@code avgFreight} — average freight tonnage across GOODS bogies (OptionalDouble).</li>
 *   <li>{@code overloaded} — bogies whose capacity exceeds the configured threshold.</li>
 *   <li>{@code topStation} — the StationStop with the highest total tonnage (Optional).</li>
 * </ul>
 *
 * <p>Uses {@link OptionalDouble} and {@link Optional} to cleanly represent absent values
 * (no GOODS bogies → avgFreight is empty; no stations → topStation is empty).</p>
 */
public final class DashboardReport {

    /**
     * Total capacity summed per BogieType.
     * Computed via Collectors.groupingBy(Bogie::getBogieType, summingInt(Bogie::getCapacity)).
     */
    private final Map<BogieType, Integer> capacityByType;

    /**
     * Average freight tonnage across all GOODS bogies.
     * Empty if no GOODS bogies exist in the fleet.
     */
    private final OptionalDouble avgFreight;

    /**
     * List of bogies whose capacity exceeds the overload threshold.
     * Empty list if all bogies are within limits.
     */
    private final List<Bogie> overloaded;

    /**
     * The station stop with the highest total detached freight tonnage.
     * Empty if no stations are in the schedule.
     */
    private final Optional<StationStop> topStation;

    /** The overload threshold used to compute the overloaded list. */
    private final int overloadThreshold;

    /**
     * Constructs a DashboardReport with all analytics results.
     *
     * @param capacityByType   map of BogieType to total capacity
     * @param avgFreight       average GOODS tonnage (may be empty)
     * @param overloaded       list of over-threshold bogies
     * @param topStation       station with most freight (may be empty)
     * @param overloadThreshold the threshold used for overload detection
     */
    public DashboardReport(Map<BogieType, Integer> capacityByType,
                           OptionalDouble avgFreight,
                           List<Bogie> overloaded,
                           Optional<StationStop> topStation,
                           int overloadThreshold) {
        this.capacityByType = capacityByType;
        this.avgFreight = avgFreight;
        this.overloaded = overloaded;
        this.topStation = topStation;
        this.overloadThreshold = overloadThreshold;
    }

    /** @return total capacity per BogieType */
    public Map<BogieType, Integer> getCapacityByType()    { return capacityByType; }

    /** @return average freight tonnage (empty if no GOODS bogies) */
    public OptionalDouble getAvgFreight()                  { return avgFreight; }

    /** @return list of bogies exceeding the overload threshold */
    public List<Bogie> getOverloaded()                     { return overloaded; }

    /** @return station with most freight tonnage (empty if no stations) */
    public Optional<StationStop> getTopStation()           { return topStation; }

    /** @return the overload capacity threshold used for this report */
    public int getOverloadThreshold()                      { return overloadThreshold; }
}
