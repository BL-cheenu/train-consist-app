/**
 * Immutable value object describing a proximity violation found during a safety check (UC-14).
 *
 * <p>Returned as {@code Optional<ProximityViolation>} from
 * {@link ConsistQueryService#proximityCheck} — present means a violation was found,
 * empty means the window is clear.</p>
 */
public final class ProximityViolation {

    /** The bogie whose proximity was checked (the query origin). */
    private final String sourceBogieId;

    /** The conflicting bogie found within the distance window. */
    private final String conflictingBogieId;

    /** Position of the source bogie in the consist list. */
    private final int sourcePosition;

    /** Position of the conflicting bogie in the consist list. */
    private final int conflictPosition;

    /** The distance window (number of positions either side) that was scanned. */
    private final int distance;

    /** Human-readable description of why this is a violation. */
    private final String reason;

    /**
     * Constructs a ProximityViolation with all fields.
     *
     * @param sourceBogieId      ID of the bogie being checked
     * @param conflictingBogieId ID of the conflicting bogie found nearby
     * @param sourcePosition     index of the source bogie
     * @param conflictPosition   index of the conflicting bogie
     * @param distance           the scan window radius
     * @param reason             description of the conflict
     */
    public ProximityViolation(String sourceBogieId, String conflictingBogieId,
                              int sourcePosition, int conflictPosition,
                              int distance, String reason) {
        this.sourceBogieId = sourceBogieId;
        this.conflictingBogieId = conflictingBogieId;
        this.sourcePosition = sourcePosition;
        this.conflictPosition = conflictPosition;
        this.distance = distance;
        this.reason = reason;
    }

    /** @return ID of the bogie being checked */
    public String getSourceBogieId()      { return sourceBogieId; }

    /** @return ID of the conflicting bogie */
    public String getConflictingBogieId() { return conflictingBogieId; }

    /** @return position index of the source bogie */
    public int getSourcePosition()        { return sourcePosition; }

    /** @return position index of the conflicting bogie */
    public int getConflictPosition()      { return conflictPosition; }

    /** @return scan distance radius used */
    public int getDistance()              { return distance; }

    /** @return human-readable violation description */
    public String getReason()             { return reason; }

    @Override
    public String toString() {
        return String.format(
                "ProximityViolation{source='%s'@%d, conflict='%s'@%d, window=%d, reason='%s'}",
                sourceBogieId, sourcePosition, conflictingBogieId,
                conflictPosition, distance, reason);
    }
}
