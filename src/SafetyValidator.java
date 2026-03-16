import java.util.List;

/**
 * Validates a train consist against safety and operational constraints.
 *
 * <p>Two rules are enforced:
 * <ol>
 *   <li><b>Adjacency rule:</b> No CYLINDRICAL (petroleum) bogie may be placed
 *       immediately next to any PASSENGER bogie. Checked via an O(n) pass over
 *       consecutive index pairs (i, i+1).</li>
 *   <li><b>Length rule:</b> Total bogie count must not exceed the maximum allowed
 *       for the given {@link RouteType}.</li>
 * </ol>
 *
 * <p>All violations are collected before returning — the validator never stops
 * at the first breach. Results are returned as an immutable {@link ValidationResult}.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class SafetyValidator {

    /**
     * Validates a consist provided as a {@code List<Bogie>} (UC-04 and later).
     * Converts to array internally and delegates to the array overload.
     *
     * @param bogies    mutable list of bogies (from TrainConsist.getBogieList())
     * @param routeType route category used to check the max-length constraint
     * @return ValidationResult containing capacities, violations, and isValid flag
     */
    public static ValidationResult validate(List<Bogie> bogies, RouteType routeType) {
        return validate(bogies.toArray(new Bogie[0]), routeType);
    }

    /**
     * Validates a consist provided as a {@code Bogie[]} (UC-02 original interface).
     * Computes passenger capacity and freight tonnage, then collects all violations.
     *
     * @param bogies    fixed array of bogies to validate
     * @param routeType route category used to check the max-length constraint
     * @return ValidationResult containing capacities, violations, and isValid flag
     */
    public static ValidationResult validate(Bogie[] bogies, RouteType routeType) {
        int passengerCapacity = CapacityCalculator.computePassengerCapacity(bogies);
        int freightTonnage = CapacityCalculator.computeFreightTonnage(bogies);
        String[] violations = collectViolations(bogies, routeType);
        return new ValidationResult(passengerCapacity, freightTonnage, violations);
    }

    /**
     * Performs a single O(n) pass to collect all rule violations.
     *
     * <p>Adjacency check: examines every consecutive pair (i, i+1).
     * A single-bogie consist has no pairs — the loop body never executes.</p>
     *
     * <p>Length check: performed after the adjacency loop using a single comparison.</p>
     *
     * @param bogies    array of bogies to inspect
     * @param routeType used to determine the max-length threshold
     * @return exact-sized String[] of violation messages (empty if none found)
     */
    private static String[] collectViolations(Bogie[] bogies, RouteType routeType) {
        int count = 0;

        // Over-allocate temp buffer — worst case: one violation per pair + one length violation
        String[] temp = new String[bogies.length + 1];

        // Adjacency check — O(n) single pass over consecutive pairs
        for (int i = 0; i < bogies.length - 1; i++) {
            Bogie current = bogies[i];
            Bogie next = bogies[i + 1];

            // Rule: CYLINDRICAL bogie must not be immediately before a PASSENGER bogie
            if (isCylindrical(current) && isPassenger(next)) {
                temp[count++] = "SAFETY VIOLATION: CYLINDRICAL bogie '" + current.getBogieId()
                        + "' (index " + i + ") is adjacent to PASSENGER bogie '"
                        + next.getBogieId() + "' (index " + (i + 1) + ")";
            }

            // Rule: PASSENGER bogie must not be immediately before a CYLINDRICAL bogie
            if (isPassenger(current) && isCylindrical(next)) {
                temp[count++] = "SAFETY VIOLATION: PASSENGER bogie '" + current.getBogieId()
                        + "' (index " + i + ") is adjacent to CYLINDRICAL bogie '"
                        + next.getBogieId() + "' (index " + (i + 1) + ")";
            }
        }

        // Length check — single comparison against route's maximum allowed bogies
        if (bogies.length > routeType.getMaxLength()) {
            temp[count++] = "LENGTH VIOLATION: Consist has " + bogies.length
                    + " bogies but " + routeType.name()
                    + " route allows max " + routeType.getMaxLength() + " bogies";
        }

        // Trim temp buffer to exact size so callers get a clean array
        String[] violations = new String[count];
        for (int i = 0; i < count; i++) violations[i] = temp[i];
        return violations;
    }

    /**
     * Returns true if the bogie is a GOODS bogie with CYLINDRICAL subtype.
     * Uses {@code getSubType()} String comparison to avoid instanceof casting.
     *
     * @param b bogie to inspect
     * @return true if b is a CYLINDRICAL goods wagon
     */
    private static boolean isCylindrical(Bogie b) {
        return b.getBogieType() == BogieType.GOODS
                && GoodsSubType.CYLINDRICAL.name().equals(b.getSubType());
    }

    /**
     * Returns true if the bogie is a PASSENGER type.
     *
     * @param b bogie to inspect
     * @return true if b is a passenger coach
     */
    private static boolean isPassenger(Bogie b) {
        return b.getBogieType() == BogieType.PASSENGER;
    }
}
