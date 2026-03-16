/**
 * Represents the top-level classification of a bogie in the train consist.
 * Every bogie is either a passenger-carrying coach or a goods-carrying wagon.
 */
public enum BogieType {

    /** Carries passengers — paired with PassengerSubType (SLEEPER, AC_CHAIR, FIRST_CLASS). */
    PASSENGER,

    /** Carries freight — paired with GoodsSubType (RECTANGULAR, CYLINDRICAL). */
    GOODS
}
