/**
 * Utility class for computing capacity totals from a bogie array.
 *
 * <p>Uses the accumulator pattern — iterates the array once to sum
 * values matching the target type. All methods are static; no instance state is needed.</p>
 */
public class CapacityCalculator {

    /**
     * Computes the total passenger seating capacity across all PASSENGER bogies.
     *
     * <p>Iterates the full array once (O(n)), skipping GOODS bogies.
     * Returns 0 if there are no PASSENGER bogies in the consist.</p>
     *
     * @param bogies array of bogies to scan
     * @return sum of capacity for all PASSENGER bogies
     */
    public static int computePassengerCapacity(Bogie[] bogies) {
        int total = 0;
        for (Bogie b : bogies) {
            if (b.getBogieType() == BogieType.PASSENGER) {
                total += b.getCapacity();
            }
        }
        return total;
    }

    /**
     * Computes the total freight tonnage across all GOODS bogies.
     *
     * <p>Iterates the full array once (O(n)), skipping PASSENGER bogies.
     * Returns 0 if there are no GOODS bogies in the consist.</p>
     *
     * @param bogies array of bogies to scan
     * @return sum of capacity for all GOODS bogies
     */
    public static int computeFreightTonnage(Bogie[] bogies) {
        int total = 0;
        for (Bogie b : bogies) {
            if (b.getBogieType() == BogieType.GOODS) {
                total += b.getCapacity();
            }
        }
        return total;
    }
}
