import java.util.Objects;

/**
 * Abstract base class representing a single bogie (coach/wagon) in a train consist.
 *
 * <p>A bogie has a unique ID, a top-level type (PASSENGER or GOODS), and a capacity
 * (seats for passenger bogies, tonnage for goods bogies). Subclasses must provide
 * a concrete {@link #getSubType()} implementation that returns their enum name as a String.</p>
 *
 * <p>Equality is based solely on {@code bogieId} — this allows
 * {@code List.remove(bogie)} to work correctly in UC-04.</p>
 *
 * @see PassengerBogie
 * @see GoodsBogie
 */
public abstract class Bogie {

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
     * Constructs a Bogie with the given ID, type, and capacity.
     *
     * @param bogieId  unique identifier for this bogie
     * @param bogieType top-level classification (PASSENGER or GOODS)
     * @param capacity  seating count or freight tonnage
     */
    public Bogie(String bogieId, BogieType bogieType, int capacity) {
        this.bogieId = bogieId;
        this.bogieType = bogieType;
        this.capacity = capacity;
    }

    /**
     * Returns the unique ID of this bogie.
     *
     * @return bogie ID string (e.g. "BG-01")
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
     * Interpreted as seat count for PASSENGER, or tonnage for GOODS.
     *
     * @return capacity as a positive integer
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the subtype of this bogie as a String (enum name).
     * Subclasses return their specific enum's {@code .name()} value.
     *
     * <p>Returning String (not the enum itself) avoids casting at the call site
     * and allows polymorphic use in printers and validators without instanceof checks.</p>
     *
     * @return subtype name (e.g. "SLEEPER", "CYLINDRICAL")
     */
    public abstract String getSubType();

    /**
     * Returns a formatted summary of this bogie.
     * Format: {@code bogieId | bogieType | subType | Capacity: capacity}
     *
     * @return human-readable string representation
     */
    @Override
    public String toString() {
        return bogieId + " | " + bogieType + " | " + getSubType() + " | Capacity: " + capacity;
    }

    /**
     * Two bogies are equal if and only if their {@code bogieId} values are equal.
     * This enables {@code List.remove(bogie)} to locate and remove by ID in UC-04.
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
     * Hash code derived from {@code bogieId}, consistent with {@link #equals(Object)}.
     *
     * @return hash code of the bogieId
     */
    @Override
    public int hashCode() {
        return Objects.hash(bogieId);
    }
}
