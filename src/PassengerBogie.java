
public class PassengerBogie extends Bogie {

    private PassengerSubType subType;

    public PassengerBogie(String bogieId, PassengerSubType subType, int capacity) {
        super(bogieId, BogieType.PASSENGER, capacity);
        this.subType = subType;
    }

    public PassengerSubType getSubType() {
        return subType;
    }

    @Override
    public String toString() {
        return "Bogie{id='" + bogieId + "', type=" + bogieType + ", subType=" + subType + ", capacity=" + capacity + "}";
    }
}
