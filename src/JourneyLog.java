import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Ordered audit log of every consist-mutation event for a specific train (UC-06).
 *
 * <p>Uses an {@link ArrayList} internally to preserve exact insertion order and allow
 * duplicate events (the same bogie can be attached and detached multiple times).
 * A {@link LinkedHashSet} would be wrong here — it deduplicates, which breaks the audit trail.</p>
 *
 * <p>Supports two read operations:
 * <ul>
 *   <li>{@link #getEvents()} — returns all events in insertion order for display.</li>
 *   <li>{@link #replayUpTo(LocalDateTime)} — applies events sequentially up to a given
 *       timestamp to reconstruct the consist state at that past point in time.
 *       Uses {@link ListIterator} for traversal, enabling bidirectional stepping.</li>
 * </ul>
 *
 * <p>Replay never modifies the live consist — it always works on a fresh copy.</p>
 */
public class JourneyLog {

    /**
     * Ordered list of events in insertion order.
     * ArrayList is used deliberately — preserves order, allows duplicates,
     * supports index-based ListIterator access.
     */
    private final List<JourneyEvent> events;

    /** The train number this log belongs to — used in display and error messages. */
    private final String trainNumber;

    /**
     * Constructs an empty JourneyLog for the given train.
     *
     * @param trainNumber the train number this log tracks (e.g. "TN-2201")
     */
    public JourneyLog(String trainNumber) {
        this.trainNumber = trainNumber;
        this.events = new ArrayList<>();
    }

    /**
     * Appends a new event to the log.
     * Events are always added at the end — preserving insertion order.
     *
     * @param event the immutable JourneyEvent to record
     */
    public void record(JourneyEvent event) {
        events.add(event);
    }

    /**
     * Returns the full list of events in insertion order.
     * The returned list is a defensive copy — callers cannot mutate the log.
     *
     * @return ordered list of all recorded JourneyEvents
     */
    public List<JourneyEvent> getEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Returns the train number this log belongs to.
     *
     * @return train number string
     */
    public String getTrainNumber() {
        return trainNumber;
    }

    /**
     * Returns true if no events have been recorded yet.
     *
     * @return true when the event list is empty
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Reconstructs the consist state as it was at the given timestamp by replaying
     * all events recorded up to and including that moment.
     *
     * <p>Uses a {@link ListIterator} to traverse events forward — this enables
     * bidirectional stepping if future use cases require reverse replay.</p>
     *
     * <p>Replay applies events to a fresh empty list — the live consist is never touched.
     * If an event is inconsistent (e.g. DETACH on a bogie that doesn't exist in the
     * reconstructed state), a warning is logged and that event is skipped.</p>
     *
     * <p>Edge case: if the cutoff timestamp is before the first event,
     * an empty list is returned.</p>
     *
     * @param upTo the cutoff timestamp — events after this are ignored
     * @return reconstructed ordered bogie list at the given point in time
     */
    public List<Bogie> replayUpTo(LocalDateTime upTo) {
        // Work on a fresh list — replay must never modify the live consist
        List<Bogie> reconstructed = new ArrayList<>();

        // ListIterator enables bidirectional traversal — forward used here
        ListIterator<JourneyEvent> iterator = events.listIterator();

        while (iterator.hasNext()) {
            JourneyEvent event = iterator.next();

            // Stop processing events that occurred after the cutoff timestamp
            if (event.getTimestamp().isAfter(upTo)) {
                break;
            }

            applyEvent(event, reconstructed);
        }

        return reconstructed;
    }

    /**
     * Applies a single event to the reconstructed bogie list.
     *
     * <p>Each event type maps to the same list operation that the live service performs,
     * but on the reconstructed list instead of the real consist.</p>
     *
     * <p>Inconsistent events (e.g. DETACH on a bogie not in the reconstructed list)
     * are skipped with a console warning — they do not abort the replay.</p>
     *
     * @param event         the event to apply
     * @param reconstructed the working bogie list being built during replay
     */
    private void applyEvent(JourneyEvent event, List<Bogie> reconstructed) {
        switch (event.getEventType()) {

            case ATTACHED:
            case BRAKE_VAN_ADDED:
                // Re-create a placeholder bogie for the replayed attach
                // In a full system this would look up the bogie from a registry
                Bogie placeholder = new PassengerBogie(
                        event.getBogieId(), PassengerSubType.SLEEPER, 0);
                int pos = event.getPosition();
                if (pos < 0 || pos >= reconstructed.size()) {
                    reconstructed.add(placeholder); // clamp to rear
                } else {
                    reconstructed.add(pos, placeholder);
                }
                break;

            case DETACHED:
                // Find by bogieId and remove — consistent with live detach
                boolean removed = reconstructed.removeIf(
                        b -> b.getBogieId().equals(event.getBogieId()));
                if (!removed) {
                    System.out.println("  [REPLAY WARNING] DETACH skipped — bogie '"
                            + event.getBogieId() + "' not found in reconstructed state.");
                }
                break;

            case SWAPPED:
                // Swap position i with i+1
                int swapPos = event.getPosition();
                if (swapPos >= 0 && swapPos < reconstructed.size() - 1) {
                    java.util.Collections.swap(reconstructed, swapPos, swapPos + 1);
                } else {
                    System.out.println("  [REPLAY WARNING] SWAP skipped — position "
                            + swapPos + " out of range in reconstructed state.");
                }
                break;

            case MOVED_TO_FRONT:
                // Find bogie and move to index 0
                Bogie toMove = null;
                for (Bogie b : reconstructed) {
                    if (b.getBogieId().equals(event.getBogieId())) {
                        toMove = b;
                        break;
                    }
                }
                if (toMove != null) {
                    reconstructed.remove(toMove);
                    reconstructed.add(0, toMove);
                } else {
                    System.out.println("  [REPLAY WARNING] MOVE_TO_FRONT skipped — bogie '"
                            + event.getBogieId() + "' not found in reconstructed state.");
                }
                break;
        }
    }
}
