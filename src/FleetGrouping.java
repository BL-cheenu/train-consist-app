import java.util.List;
import java.util.Map;

/**
 * Immutable value object that holds bogies grouped by their {@link BogieType} (UC-10).
 *
 * <p>Produced by {@link FleetDashboardService#groupByType} using the
 * {@code computeIfAbsent} pattern. The groups map is a snapshot taken
 * at the moment of grouping — it is not live-updated.</p>
 */
public final class FleetGrouping {

    /**
     * Map from BogieType to the list of bogies of that type.
     * Built using computeIfAbsent — never contains null values,
     * even for types with no bogies (they simply have no key in the map).
     */
    private final Map<BogieType, List<Bogie>> groups;

    /**
     * Constructs a FleetGrouping with the given groups map.
     *
     * @param groups map of BogieType to list of bogies of that type
     */
    public FleetGrouping(Map<BogieType, List<Bogie>> groups) {
        this.groups = groups;
    }

    /**
     * Returns the full groups map.
     *
     * @return map of BogieType to bogie list
     */
    public Map<BogieType, List<Bogie>> getGroups() {
        return groups;
    }

    /**
     * Returns the list of bogies for the given type, or an empty list if none exist.
     *
     * @param type the BogieType to look up
     * @return list of bogies of that type (never null)
     */
    public List<Bogie> getGroup(BogieType type) {
        return groups.getOrDefault(type, List.of());
    }

    /**
     * Returns the count of bogies for the given type.
     *
     * @param type the BogieType to count
     * @return number of bogies of that type
     */
    public int countByType(BogieType type) {
        return getGroup(type).size();
    }

    /**
     * Returns the total capacity across all bogies of the given type.
     *
     * @param type the BogieType to sum
     * @return total capacity for that type
     */
    public int totalCapacityByType(BogieType type) {
        int total = 0;
        for (Bogie b : getGroup(type)) {
            total += b.getCapacity();
        }
        return total;
    }
}
