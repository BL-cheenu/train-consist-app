/**
 * Concrete bogie representing a PASSENGER coach.
 *
 * <p>Extends {@link Bogie} with a {@link PassengerSubType} field that specifies
 * the class of service (SLEEPER, AC_CHAIR, or FIRST_CLASS).
 * The {@code capacity} field inherited from Bogie represents the number of seats.</p>
 */
public class PassengerBogie extends Bogie {

    /** The class of service for this passenger coach (SLEEPER, AC_CHAIR, FIRST_CLASS). */
    private PassengerSubType subType;

    /**
     * Constructs a PassengerBogie with the given ID, subtype, and seat capacity.
     *
     * @param bogieId  unique identifier (e.g. "BG-01")
     * @param subType  class of service for this coach
     * @param capacity number of seats in this coach
     */
    public PassengerBogie(String bogieId, PassengerSubType subType, int capacity) {
        super(bogieId, BogieType.PASSENGER, capacity);
        this.subType = subType;
    }

    /**
     * Returns the PassengerSubType enum value for this coach.
     * Use this when you need the typed enum (e.g. in tests or switch statements).
     *
     * @return PassengerSubType enum (SLEEPER, AC_CHAIR, or FIRST_CLASS)
     */
    public PassengerSubType getPassengerSubType() {
        return subType;
    }

    /**
     * Returns the subtype name as a String — satisfies the abstract method in {@link Bogie}.
     * Used by printers and validators for display without casting.
     *
     * @return subtype name (e.g. "SLEEPER", "AC_CHAIR", "FIRST_CLASS")
     */
    @Override
    public String getSubType() {
        return subType.name();
    }
}
