import java.util.Comparator;

/**
 * Custom comparator for UC-09 that orders bogies by:
 * <ol>
 *   <li><b>Type:</b> GOODS bogies first, then PASSENGER bogies.</li>
 *   <li><b>Capacity:</b> ascending within each type group.</li>
 *   <li><b>Bogie ID:</b> ascending tiebreaker — prevents TreeSet from silently
 *       deduplicating two bogies with the same type and capacity but different IDs.</li>
 * </ol>
 *
 * <p>Implemented as a standalone class (not a lambda) for testability —
 * a named class can be verified with {@code assertInstanceOf} and its
 * ordering logic can be tested in isolation.</p>
 *
 * <p>Used by {@link LoadPlanService#buildCustomPlan} to construct a
 * {@code TreeSet} with this custom ordering.</p>
 */
public class BogieTypeCapacityComparator implements Comparator<Bogie> {

    /**
     * Compares two bogies using the GOODS-first, capacity-ascending, bogieId-tiebreak ordering.
     *
     * @param a the first bogie
     * @param b the second bogie
     * @return negative if a comes before b, positive if a comes after b, 0 only if same bogieId
     */
    @Override
    public int compare(Bogie a, Bogie b) {
        // Primary: GOODS before PASSENGER
        // GOODS ordinal = 1, PASSENGER ordinal = 0 in BogieType enum
        // We want GOODS first, so compare in reverse enum order
        int typeCompare = typeRank(a.getBogieType()) - typeRank(b.getBogieType());
        if (typeCompare != 0) {
            return typeCompare;
        }

        // Secondary: capacity ascending within the same type group
        int capacityCompare = Integer.compare(a.getCapacity(), b.getCapacity());
        if (capacityCompare != 0) {
            return capacityCompare;
        }

        // Tiebreaker: bogieId ascending — prevents silent TreeSet deduplication
        return a.getBogieId().compareTo(b.getBogieId());
    }

    /**
     * Maps BogieType to a sort rank: GOODS = 0 (first), PASSENGER = 1 (second).
     *
     * @param type the bogie type to rank
     * @return 0 for GOODS, 1 for PASSENGER
     */
    private int typeRank(BogieType type) {
        return type == BogieType.GOODS ? 0 : 1;
    }
}
