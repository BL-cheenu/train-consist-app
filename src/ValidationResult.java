/**
 * Immutable value object returned by {@link SafetyValidator} after evaluating a consist.
 *
 * <p>Holds the computed capacity figures and the list of safety/length violations found.
 * The {@code isValid} flag is derived automatically — it is {@code true} only when
 * the violations array is empty.</p>
 *
 * <p>Being immutable, this object is safe to pass around and log without side effects.</p>
 */
public final class ValidationResult {

    /** Total number of passenger seats across all PASSENGER bogies in the consist. */
    private final int passengerCapacity;

    /** Total freight tonnage across all GOODS bogies in the consist. */
    private final int freightTonnage;

    /**
     * Array of violation messages collected during validation.
     * Each entry describes one specific rule breach (adjacency or length).
     * Empty array means no violations were found.
     */
    private final String[] violations;

    /**
     * Overall safety flag — {@code true} if no violations were found, {@code false} otherwise.
     * Derived from {@code violations.length == 0} at construction time.
     */
    private final boolean isValid;

    /**
     * Constructs a ValidationResult with the given capacity figures and violations.
     * The {@code isValid} flag is set automatically based on whether violations is empty.
     *
     * @param passengerCapacity total passenger seats
     * @param freightTonnage    total freight tonnage
     * @param violations        array of violation messages (empty if none)
     */
    public ValidationResult(int passengerCapacity, int freightTonnage, String[] violations) {
        this.passengerCapacity = passengerCapacity;
        this.freightTonnage = freightTonnage;
        this.violations = violations;
        this.isValid = violations.length == 0;
    }

    /**
     * Returns the total passenger seating capacity of the consist.
     *
     * @return sum of capacity across all PASSENGER bogies
     */
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    /**
     * Returns the total freight tonnage of the consist.
     *
     * @return sum of capacity across all GOODS bogies
     */
    public int getFreightTonnage() {
        return freightTonnage;
    }

    /**
     * Returns the array of violation messages.
     * Each message describes one rule breach detected during validation.
     *
     * @return violation messages array (empty if consist is valid)
     */
    public String[] getViolations() {
        return violations;
    }

    /**
     * Returns {@code true} if no safety or length violations were found.
     *
     * @return true when violations array is empty
     */
    public boolean isValid() {
        return isValid;
    }
}
