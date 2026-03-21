import java.util.Objects;

/**
 * Abstract base class representing a single bogie in a train consist.
 *
 * <p>Implements {@link Comparable} for natural ordering by capacity (ascending).
 * When two bogies have equal capacity, {@code bogieId} is used as a tiebreaker
 * to prevent TreeSet from silently deduplicating bogies with the same capacity.</p>
 *
 * <p>Equality is based solely on {@code bogieId} — consistent with
 * {@link #equals} and {@link #hashCode} for correct List.remove() behaviour.</p>
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
     * Constructs a Bogie with the given ID, type, and capacity.
     *
     * @param bogieId   unique identifier for this bogie
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
     * Returns the subtype of this bogie as a String.
     * Subclasses return their specific enum's {@code .name()} value.
     *
     * @return subtype name (e.g. "SLEEPER", "CYLINDRICAL")
     */
    public abstract String getSubType();

    /**
     * Natural ordering for UC-09 — sorts by capacity ascending.
     *
     * <p><b>Tiebreaker rule:</b> when two bogies have equal capacity,
     * {@code bogieId} is used as a secondary comparator. This is critical —
     * if compareTo returned 0 for equal-capacity bogies with different IDs,
     * TreeSet would silently discard one of them as a "duplicate".</p>
     *
     * @param other the bogie to compare against
     * @return negative if this < other, positive if this > other, 0 only if same bogieId
     */
    @Override
    public int compareTo(Bogie other) {
        // Primary: compare by capacity ascending
        int capacityCompare = Integer.compare(this.capacity, other.capacity);
        if (capacityCompare != 0) {
            return capacityCompare;
        }
        // Tiebreaker: compare by bogieId — prevents silent TreeSet deduplication
        return this.bogieId.compareTo(other.bogieId);
    }

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
