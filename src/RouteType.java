/**
 * Represents the category of a train route and its associated maximum consist length.
 *
 * <p>Each route type imposes a hard limit on the number of bogies allowed.
 * {@link SafetyValidator} uses this to enforce the length constraint.</p>
 */
public enum RouteType {

    /** Short urban/commuter route — maximum 12 bogies. */
    SUBURBAN(12),

    /** Long-distance inter-city route — maximum 20 bogies. */
    EXPRESS(20),

    /** Dedicated freight corridor — maximum 30 bogies. */
    FREIGHT(30);

    /** Maximum number of bogies permitted for this route type. */
    private final int maxLength;

    /**
     * Constructs a RouteType with the given maximum consist length.
     *
     * @param maxLength maximum bogies allowed on this route
     */
    RouteType(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns the maximum number of bogies allowed for this route type.
     *
     * @return max consist length as a positive integer
     */
    public int getMaxLength() {
        return maxLength;
    }
}
