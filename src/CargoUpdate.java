/**
 * Immutable value object that carries cargo update parameters for UC-10.
 *
 * <p>Passed to {@link BogieIndex#update} to modify a bogie's cargo details in-place.
 * Being immutable, it can be safely passed around without risk of mutation.</p>
 */
public final class CargoUpdate {

    /** The ID of the bogie to update. Used for O(1) HashMap lookup. */
    private final String bogieId;

    /**
     * New cargo weight in tonnes.
     * Must be non-negative — {@link BogieIndex#update} rejects negative values.
     */
    private final double newCargoWeight;

    /** New cargo description (e.g. "Coal", "Petroleum", "Passengers"). */
    private final String newCargoDesc;

    /**
     * Constructs a CargoUpdate with all fields.
     *
     * @param bogieId       the bogie ID to update
     * @param newCargoWeight new cargo weight in tonnes (must be >= 0)
     * @param newCargoDesc  new cargo description
     */
    public CargoUpdate(String bogieId, double newCargoWeight, String newCargoDesc) {
        this.bogieId = bogieId;
        this.newCargoWeight = newCargoWeight;
        this.newCargoDesc = newCargoDesc;
    }

    /**
     * Returns the bogie ID targeted by this update.
     *
     * @return bogie ID string
     */
    public String getBogieId() {
        return bogieId;
    }

    /**
     * Returns the new cargo weight in tonnes.
     *
     * @return cargo weight (>= 0)
     */
    public double getNewCargoWeight() {
        return newCargoWeight;
    }

    /**
     * Returns the new cargo description.
     *
     * @return cargo description string
     */
    public String getNewCargoDesc() {
        return newCargoDesc;
    }
}
