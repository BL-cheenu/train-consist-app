import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that groups bogies by type for the fleet dashboard (UC-10).
 *
 * <p>Uses the {@code computeIfAbsent} pattern to build a {@code Map<BogieType, List<Bogie>>}
 * without manual null-checks:
 * <pre>
 *   groups.computeIfAbsent(type, k -> new ArrayList<>()).add(bogie);
 * </pre>
 *
 * <p>If the key (BogieType) has no entry yet, {@code computeIfAbsent} creates an empty
 * ArrayList, stores it in the map, and returns it — all in one call. This eliminates
 * the classic null-check pattern and prevents NPEs on missing type keys.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class FleetDashboardService {

    /**
     * Groups all bogies in the given list by their {@link BogieType}.
     *
     * <p>Iterates the list once (O(n)) and uses {@code computeIfAbsent} to
     * build a {@code HashMap<BogieType, List<Bogie>>} grouping.
     * The returned {@link FleetGrouping} is a snapshot — not live-updated.</p>
     *
     * @param bogies the live bogie list to group
     * @return FleetGrouping containing bogies grouped by BogieType
     */
    public static FleetGrouping groupByType(List<Bogie> bogies) {
        // HashMap: BogieType key → ArrayList of bogies of that type
        Map<BogieType, List<Bogie>> groups = new HashMap<>();

        for (Bogie b : bogies) {
            // computeIfAbsent: if BogieType key absent → create empty ArrayList + store + return it
            // Then .add(b) appends to that list — no null check needed
            groups.computeIfAbsent(b.getBogieType(), k -> new ArrayList<>()).add(b);
        }

        return new FleetGrouping(groups);
    }
}
