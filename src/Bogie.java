import java.util.Objects;

/**
 * Abstract base class representing a single bogie in a train consist.
 *
 * <p>UC-12 adds a {@code weight} field — the physical weight of the bogie in tonnes,
 * distinct from {@code capacity} (seats or freight tonnage). Weight is used for
 * braking-balance departure sort: lighter bogies must be towards the front.</p>
 *
 * <p>Implements {@link Comparable} for natural ordering by capacity (UC-09 unchanged).
 * UC-12 uses explicit Comparator chains, not compareTo.</p>
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
     * Physical weight of this bogie in tonnes (UC-12).
     * Used for departure sort — lighter bogies placed towards the front
     * for braking balance. Distinct from capacity.
     */
    protected int weight;

    /**
     * Current cargo weight loaded onto this bogie in tonnes (UC-10).
     * Mutable — updated in-place via BogieIndex.update().
     */
    private double cargoWeight;

    /**
     * Human-readable description of current cargo (UC-10).
     * Mutable — updated in-place via BogieIndex.update().
     */
    private String cargoDesc;

    /**
     * Constructs a Bogie with the given ID, type, capacity, and weight.
     * Cargo fields default to 0.0 and empty string.
     *
     * @param bogieId   unique identifier for this bogie
     * @param bogieType top-level classification (PASSENGER or GOODS)
     * @param capacity  seating count or freight tonnage
     * @param weight    physical bogie weight in tonnes (for departure sort)
     */
    public Bogie(String bogieId, BogieType bogieType, int capacity, int weight) {
        this.bogieId = bogieId;
        this.bogieType = bogieType;
        this.capacity = capacity;
        this.weight = weight;
        this.cargoWeight = 0.0;
        this.cargoDesc = "";
    }

    /**
     * Backward-compatible constructor — weight defaults to capacity value.
     * Preserves UC-01 through UC-11 constructors without modification.
     *
     * @param bogieId   unique identifier
     * @param bogieType classification
     * @param capacity  seating or tonnage capacity
     */
    public Bogie(String bogieId, BogieType bogieType, int capacity) {
        this(bogieId, bogieType, capacity, capacity);
    }

    public String getBogieId()   { return bogieId; }
    public BogieType getBogieType() { return bogieType; }
    public int getCapacity()     { return capacity; }

    /**
     * Returns the physical weight of this bogie in tonnes.
     * Used for UC-12 departure sort (braking balance optimisation).
     *
     * @return bogie weight in tonnes
     */
    public int getWeight()       { return weight; }

    public double getCargoWeight() { return cargoWeight; }
    public void setCargoWeight(double cargoWeight) { this.cargoWeight = cargoWeight; }
    public String getCargoDesc()   { return cargoDesc; }
    public void setCargoDesc(String cargoDesc)     { this.cargoDesc = cargoDesc; }

    /** Returns the subtype name as a String. Implemented by subclasses. */
    public abstract String getSubType();

    /**
     * Natural ordering (UC-09): capacity ascending, bogieId tiebreaker.
     * Prevents TreeSet silent deduplication on equal capacity.
     */
    @Override
    public int compareTo(Bogie other) {
        int cap = Integer.compare(this.capacity, other.capacity);
        if (cap != 0) return cap;
        return this.bogieId.compareTo(other.bogieId);
    }

    @Override
    public String toString() {
        return bogieId + " | " + bogieType + " | " + getSubType()
                + " | Cap: " + capacity + " | Wt: " + weight + "t"
                + " | Cargo: " + cargoWeight + "t (" + cargoDesc + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bogie)) return false;
        return Objects.equals(bogieId, ((Bogie) o).bogieId);
    }

    @Override
    public int hashCode() { return Objects.hash(bogieId); }
}
