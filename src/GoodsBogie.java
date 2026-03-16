
public class GoodsBogie extends Bogie {

    private GoodsSubType subType;

    public GoodsBogie(String bogieId, GoodsSubType subType, int capacity) {
        super(bogieId, BogieType.GOODS, capacity);
        this.subType = subType;
    }

    public GoodsSubType getSubType() {
        return subType;
    }

    @Override
    public String toString() {
        return "Bogie{id='" + bogieId + "', type=" + bogieType + ", subType=" + subType + ", capacity=" + capacity + "}";
    }
}
