/**
 * Concrete bogie representing a GOODS wagon.
 *
 * <p>Extends {@link Bogie} with a {@link GoodsSubType} field that specifies
 * the physical shape of the wagon (RECTANGULAR or CYLINDRICAL).
 * The {@code capacity} field inherited from Bogie represents freight tonnage.</p>
 *
 * <p><b>Safety note:</b> CYLINDRICAL wagons carry petroleum/flammable liquid.
 * {@link SafetyValidator} enforces that no CYLINDRICAL bogie is adjacent
 * to any PASSENGER bogie in the consist.</p>
 */
public class GoodsBogie extends Bogie {

    /** The physical shape/type of this goods wagon (RECTANGULAR or CYLINDRICAL). */
    private GoodsSubType subType;

    /**
     * Constructs a GoodsBogie with the given ID, subtype, and tonnage capacity.
     *
     * @param bogieId  unique identifier (e.g. "BG-03")
     * @param subType  physical type of this wagon
     * @param capacity freight tonnage this wagon can carry
     */
    public GoodsBogie(String bogieId, GoodsSubType subType, int capacity) {
        super(bogieId, BogieType.GOODS, capacity);
        this.subType = subType;
    }

    /**
     * Returns the GoodsSubType enum value for this wagon.
     * Use this when you need the typed enum (e.g. in tests or switch statements).
     *
     * @return GoodsSubType enum (RECTANGULAR or CYLINDRICAL)
     */
    public GoodsSubType getGoodsSubType() {
        return subType;
    }

    /**
     * Returns the subtype name as a String — satisfies the abstract method in {@link Bogie}.
     * Used by printers and validators for display without casting.
     *
     * @return subtype name (e.g. "RECTANGULAR", "CYLINDRICAL")
     */
    @Override
    public String getSubType() {
        return subType.name();
    }
}
