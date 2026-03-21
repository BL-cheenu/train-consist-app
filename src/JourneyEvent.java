import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record of a single consist-change event in the journey log (UC-06).
 *
 * <p>Captures what happened ({@code eventType}), which bogie was affected ({@code bogieId}),
 * where in the consist ({@code position}), who triggered it ({@code actorId}),
 * and exactly when ({@code timestamp}).</p>
 *
 * <p>Once constructed, no field can be changed — this is essential for a trustworthy
 * audit trail. The event sourcing pattern depends on events being immutable facts.</p>
 */
public final class JourneyEvent {

    /** Formatter used in toString() for human-readable timestamp output. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The type of operation that triggered this event. */
    private final EventType eventType;

    /**
     * The ID of the bogie affected by this event.
     * For SWAPPED events, this holds the ID of the bogie at position i.
     */
    private final String bogieId;

    /**
     * The position index relevant to this event.
     * For ATTACHED: the index where the bogie was inserted.
     * For DETACHED: -1 (position no longer meaningful after removal).
     * For SWAPPED: the lower index i (bogies[i] and bogies[i+1] were exchanged).
     * For MOVED_TO_FRONT: 0 (always the front).
     * For BRAKE_VAN_ADDED: the index it was appended to (last position).
     */
    private final int position;

    /** The ID of the actor (operator or system) who triggered this event. */
    private final String actorId;

    /** The exact date-time when this event was recorded. Set at construction time. */
    private final LocalDateTime timestamp;

    /**
     * Constructs an immutable JourneyEvent with all fields set at creation time.
     *
     * @param eventType the type of mutation that occurred
     * @param bogieId   the ID of the bogie involved
     * @param position  the position index relevant to this event (or -1 if not applicable)
     * @param actorId   the ID of the actor who triggered the operation
     * @param timestamp the exact moment this event was recorded
     */
    public JourneyEvent(EventType eventType, String bogieId, int position,
                        String actorId, LocalDateTime timestamp) {
        this.eventType = eventType;
        this.bogieId = bogieId;
        this.position = position;
        this.actorId = actorId;
        this.timestamp = timestamp;
    }

    /**
     * Returns the type of operation this event represents.
     *
     * @return the EventType enum constant
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Returns the ID of the bogie affected by this event.
     *
     * @return bogie ID string
     */
    public String getBogieId() {
        return bogieId;
    }

    /**
     * Returns the position index relevant to this event.
     *
     * @return position index, or -1 if not applicable (e.g. DETACHED)
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the ID of the actor who triggered this event.
     *
     * @return actor ID string
     */
    public String getActorId() {
        return actorId;
    }

    /**
     * Returns the timestamp when this event was recorded.
     *
     * @return LocalDateTime of the event
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a human-readable summary of this event.
     * Format: {@code [timestamp] EVENT_TYPE | bogieId | pos: N | actor: X}
     *
     * @return formatted event string
     */
    @Override
    public String toString() {
        return String.format("[%s] %-18s | bogie: %-8s | pos: %2d | actor: %s",
                timestamp.format(FORMATTER), eventType, bogieId, position, actorId);
    }
}
