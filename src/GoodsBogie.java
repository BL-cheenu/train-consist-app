/**
 * Concrete bogie representing a GOODS wagon.
 * UC-12: adds weight-aware constructors for departure sort.
 */
public class GoodsBogie extends Bogie {

    private GoodsSubType subType;

    /**
     * UC-12 constructor — accepts explicit weight separate from capacity.
     *
     * @param bogieId  unique identifier
     * @param subType  physical type of wagon
     * @param capacity freight tonnage
     * @param weight   physical bogie weight in tonnes
     */
    public GoodsBogie(String bogieId, GoodsSubType subType, int capacity, int weight) {
        super(bogieId, BogieType.GOODS, capacity, weight);
        this.subType = subType;
    }

    /**
     * Backward-compatible constructor — weight defaults to capacity.
     * Used by UC-01 through UC-11.
     *
     * @param bogieId  unique identifier
     * @param subType  physical type of wagon
     * @param capacity freight tonnage
     */
    public GoodsBogie(String bogieId, GoodsSubType subType, int capacity) {
        super(bogieId, BogieType.GOODS, capacity);
        this.subType = subType;
    }

    /**
     * Returns the GoodsSubType enum value.
     *
     * @return GoodsSubType (RECTANGULAR or CYLINDRICAL)
     */
    public GoodsSubType getGoodsSubType() { return subType; }

    /**
     * Returns the subtype name as a String — satisfies abstract method in Bogie.
     *
     * @return subtype name string
     */
    @Override
    public String getSubType() { return subType.name(); }
}
