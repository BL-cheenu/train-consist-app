/**
 * Prints fleet dashboard output and cargo update results to the console (UC-10).
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class FleetDashboardPrinter {

    /**
     * Prints the fleet dashboard — bogies grouped by type with count and total capacity.
     *
     * @param grouping the FleetGrouping produced by FleetDashboardService
     */
    public static void printGrouping(FleetGrouping grouping) {
        System.out.println("\n========================================");
        System.out.println("         FLEET DASHBOARD — UC-10");
        System.out.println("========================================");

        if (grouping.getGroups().isEmpty()) {
            System.out.println("  No bogies in fleet.");
            System.out.println("========================================\n");
            return;
        }

        // Iterate each BogieType group
        for (BogieType type : BogieType.values()) {
            int count = grouping.countByType(type);
            if (count == 0) continue; // skip types with no bogies

            int totalCapacity = grouping.totalCapacityByType(type);
            System.out.println("\n  " + type + " bogies (" + count + "):");
            System.out.println("  " + "-".repeat(46));
            System.out.printf("  %-12s %-14s %-8s %-12s %-12s%n",
                    "Bogie ID", "Subtype", "Capacity", "Cargo (t)", "Description");
            System.out.println("  " + "-".repeat(64));

            for (Bogie b : grouping.getGroup(type)) {
                System.out.printf("  %-12s %-14s %-8d %-12.1f %-12s%n",
                        b.getBogieId(), b.getSubType(), b.getCapacity(),
                        b.getCargoWeight(), b.getCargoDesc().isEmpty() ? "-" : b.getCargoDesc());
            }

            System.out.println("  Total capacity: " + totalCapacity);
        }

        System.out.println("\n========================================\n");
    }

    /**
     * Prints the result of a cargo update operation.
     *
     * @param bogie   the updated bogie (or null if not found)
     * @param bogieId the ID that was searched
     */
    public static void printUpdateResult(Bogie bogie, String bogieId) {
        System.out.println("\n----------------------------------------");
        if (bogie == null) {
            // UC-10 edge flow: bogie ID not in HashMap
            System.out.println("  Bogie '" + bogieId + "' not found in index.");
        } else {
            System.out.println("  Cargo updated successfully.");
            System.out.println("  Bogie ID    : " + bogie.getBogieId());
            System.out.println("  Type        : " + bogie.getBogieType());
            System.out.println("  Cargo Weight: " + bogie.getCargoWeight() + "t");
            System.out.println("  Cargo Desc  : " + bogie.getCargoDesc());
        }
        System.out.println("----------------------------------------\n");
    }
}
