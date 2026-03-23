/**
 * Concrete bogie representing a PASSENGER coach.
 * UC-12: adds weight-aware constructors for departure sort.
 */
public class PassengerBogie extends Bogie {

    private PassengerSubType subType;

    /**
     * UC-12 constructor — accepts explicit weight separate from capacity.
     *
     * @param bogieId  unique identifier
     * @param subType  class of service
     * @param capacity number of seats
     * @param weight   physical bogie weight in tonnes
     */
    public PassengerBogie(String bogieId, PassengerSubType subType, int capacity, int weight) {
        super(bogieId, BogieType.PASSENGER, capacity, weight);
        this.subType = subType;
    }

    /**
     * Backward-compatible constructor — weight defaults to capacity.
     * Used by UC-01 through UC-11.
     *
     * @param bogieId  unique identifier
     * @param subType  class of service
     * @param capacity number of seats
     */
    public PassengerBogie(String bogieId, PassengerSubType subType, int capacity) {
        super(bogieId, BogieType.PASSENGER, capacity);
        this.subType = subType;
    }

    /**
     * Returns the PassengerSubType enum value.
     *
     * @return PassengerSubType (SLEEPER, AC_CHAIR, or FIRST_CLASS)
     */
    public PassengerSubType getPassengerSubType() { return subType; }

    /**
     * Returns the subtype name as a String — satisfies abstract method in Bogie.
     *
     * @return subtype name string
     */
    @Override
    public String getSubType() { return subType.name(); }
}
