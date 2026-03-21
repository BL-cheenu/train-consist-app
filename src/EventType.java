/**
 * Enumerates every type of consist mutation that the journey log can record.
 *
 * <p>Each constant maps to one operation that mutates the bogie list.
 * {@link JourneyEvent} carries one of these values to describe what happened.</p>
 */
public enum EventType {

    /** A new bogie was inserted into the consist at a specific position. */
    ATTACHED,

    /** An existing bogie was removed from the consist by ID. */
    DETACHED,

    /** Two adjacent bogies at positions i and i+1 were exchanged. */
    SWAPPED,

    /** A bogie was relocated from its current position to index 0. */
    MOVED_TO_FRONT,

    /** A brake van bogie was appended to the rear of the consist. */
    BRAKE_VAN_ADDED
}
