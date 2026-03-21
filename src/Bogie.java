import java.util.Objects;

/**
 * Abstract base class representing a single bogie in a train consist.
 *
 * <p>Implements {@link Comparable} for natural ordering by capacity (ascending).
 * When two bogies have equal capacity, {@code bogieId} is used as a tiebreaker
 * to prevent TreeSet from silently deduplicating bogies with the same capacity.</p>
 *
 * <p>UC-10 adds mutable cargo fields ({@code cargoWeight}, {@code cargoDesc})
 * that can be updated in-place via {@link BogieIndex#update}.
 * Because the HashMap stores references to the same Bogie objects as the List,
 * updating through the map is automatically reflected in the list — no sync needed.</p>
 */
public abstract class Bogie implements Comparable<Bogie> {

    /** Unique identifier for this bogie (e.g. "BG-01"). Used for lookup and equality. */
    protected String bogieId;

    /** Top-level type of this bogie — PASSENGER or GOODS. */
    protected BogieType bogieType;

    /**
     * Capacity of this bogie.
     * For PASSENGER bogies: number of seats.
     * For GOODS bogies: freight tonnage.
     */
    protected int capacity;

    /**
     * Current cargo weight loaded onto this bogie in tonnes (UC-10).
     * Mutable — updated in-place via BogieIndex.update().
     * Default: 0.0 (empty bogie).
     */
    private double cargoWeight;

    /**
     * Human-readable description of current cargo (UC-10).
     * Mutable — updated in-place via BogieIndex.update().
     * Default: empty string.
     */
    private String cargoDesc;

    /**
     * Constructs a Bogie with the given ID, type, and capacity.
     * Cargo fields default to 0.0 and empty string.
     *
     * @param bogieId   unique identifier for this bogie
     * @param bogieType top-level classification (PASSENGER or GOODS)
     * @param capacity  seating count or freight tonnage
     */
    public Bogie(String bogieId, BogieType bogieType, int capacity) {
        this.bogieId = bogieId;
        this.bogieType = bogieType;
        this.capacity = capacity;
        this.cargoWeight = 0.0;
        this.cargoDesc = "";
    }

    /**
     * Returns the unique ID of this bogie.
     *
     * @return bogie ID string
     */
    public String getBogieId() {
        return bogieId;
    }

    /**
     * Returns the top-level type of this bogie.
     *
     * @return PASSENGER or GOODS
     */
    public BogieType getBogieType() {
        return bogieType;
    }

    /**
     * Returns the capacity of this bogie.
     *
     * @return capacity as a positive integer
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the current cargo weight loaded on this bogie.
     *
     * @return cargo weight in tonnes
     */
    public double getCargoWeight() {
        return cargoWeight;
    }

    /**
     * Updates the cargo weight of this bogie in-place.
     * Called by {@link BogieIndex#update} — reflected immediately in both
     * the HashMap and the live List due to reference semantics.
     *
     * @param cargoWeight new cargo weight in tonnes (must be >= 0)
     */
    public void setCargoWeight(double cargoWeight) {
        this.cargoWeight = cargoWeight;
    }

    /**
     * Returns the current cargo description of this bogie.
     *
     * @return cargo description string
     */
    public String getCargoDesc() {
        return cargoDesc;
    }

    /**
     * Updates the cargo description of this bogie in-place.
     * Called by {@link BogieIndex#update} — reflected immediately in both
     * the HashMap and the live List due to reference semantics.
     *
     * @param cargoDesc new cargo description
     */
    public void setCargoDesc(String cargoDesc) {
        this.cargoDesc = cargoDesc;
    }

    /**
     * Returns the subtype of this bogie as a String.
     * Subclasses return their specific enum's {@code .name()} value.
     *
     * @return subtype name (e.g. "SLEEPER", "CYLINDRICAL")
     */
    public abstract String getSubType();

    /**
     * Natural ordering for UC-09 — sorts by capacity ascending.
     * Tiebreaker on bogieId prevents TreeSet silent deduplication.
     *
     * @param other the bogie to compare against
     * @return negative if this < other, positive if this > other, 0 only if same bogieId
     */
    @Override
    public int compareTo(Bogie other) {
        int capacityCompare = Integer.compare(this.capacity, other.capacity);
        if (capacityCompare != 0) return capacityCompare;
        return this.bogieId.compareTo(other.bogieId);
    }

    /**
     * Returns a formatted summary of this bogie including cargo details.
     *
     * @return human-readable string representation
     */
    @Override
    public String toString() {
        return bogieId + " | " + bogieType + " | " + getSubType()
                + " | Capacity: " + capacity
                + " | Cargo: " + cargoWeight + "t (" + cargoDesc + ")";
    }

    /**
     * Two bogies are equal if and only if their {@code bogieId} values are equal.
     *
     * @param o the object to compare
     * @return true if both bogies share the same bogieId
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bogie)) return false;
        Bogie other = (Bogie) o;
        return Objects.equals(bogieId, other.bogieId);
    }

    /**
     * Hash code derived from {@code bogieId}, consistent with {@link #equals}.
     *
     * @return hash code of the bogieId
     */
    @Override
    public int hashCode() {
        return Objects.hash(bogieId);
    }
}
