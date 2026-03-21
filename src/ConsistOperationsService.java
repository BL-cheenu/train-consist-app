import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that performs attach and detach operations on a consist's bogie list (UC-04),
 * and publishes an immutable {@link JourneyEvent} to the {@link JourneyLog} after
 * every successful mutation (UC-06).
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Every method accepts a {@code JourneyLog} and {@code actorId} — logging is
 *       mandatory, not optional. This ensures the audit trail is never missed.</li>
 *   <li>{@code attach} clamps out-of-range positions to rear rather than throwing.</li>
 *   <li>{@code detach} uses {@code List.remove(Object)} which relies on
 *       {@link Bogie#equals} being based on {@code bogieId}.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ConsistOperationsService {

    /**
     * Inserts a bogie into the consist at the position specified by the {@link AttachRequest},
     * then records an ATTACHED event in the journey log.
     *
     * <p>Position handling:
     * <ul>
     *   <li>{@code -1} (rear flag): appends to end of list.</li>
     *   <li>{@code 0} or negative: inserts at head (index 0).</li>
     *   <li>Beyond current size: clamps to rear and notifies the operator.</li>
     *   <li>Valid mid-range: inserts at exact index.</li>
     * </ul>
     *
     * @param bogies  the live bogie list from TrainConsist
     * @param request AttachRequest containing the bogie and target position
     * @param log     the JourneyLog to record the ATTACHED event into
     * @param actorId the ID of the operator performing this action
     */
    public static void attach(List<Bogie> bogies, AttachRequest request,
                              JourneyLog log, String actorId) {
        int position = request.getTargetPosition();
        int actualPosition;

        if (request.isRear() || position >= bogies.size()) {
            if (!request.isRear() && position >= bogies.size()) {
                System.out.println("  INFO: Position " + position
                        + " exceeds consist size (" + bogies.size() + "). Bogie attached at rear.");
            }
            bogies.add(request.getBogie());
            actualPosition = bogies.size() - 1; // record the actual rear index
        } else if (position <= 0) {
            bogies.add(0, request.getBogie());
            actualPosition = 0;
        } else {
            bogies.add(position, request.getBogie());
            actualPosition = position;
        }

        // Record ATTACHED event with the actual insertion position
        log.record(new JourneyEvent(
                EventType.ATTACHED,
                request.getBogie().getBogieId(),
                actualPosition,
                actorId,
                LocalDateTime.now()));
    }

    /**
     * Removes the bogie identified by the given {@link DetachRequest} from the consist,
     * then records a DETACHED event in the journey log.
     *
     * <p>If the bogie is not found, no event is recorded and false is returned.</p>
     *
     * @param bogies  the live bogie list from TrainConsist
     * @param request DetachRequest containing the ID of the bogie to remove
     * @param log     the JourneyLog to record the DETACHED event into
     * @param actorId the ID of the operator performing this action
     * @return true if the bogie was found and removed, false if not found
     */
    public static boolean detach(List<Bogie> bogies, DetachRequest request,
                                 JourneyLog log, String actorId) {
        Bogie target = findById(bogies, request.getBogieId());

        if (target == null) {
            System.out.println("  ERROR: Bogie '" + request.getBogieId() + "' not found in consist.");
            return false;
        }

        bogies.remove(target); // equals() on bogieId ensures the right element is removed

        if (bogies.isEmpty()) {
            System.out.println("  WARNING: Consist is now empty.");
        }

        // Record DETACHED event — position is -1 since the bogie no longer has one
        log.record(new JourneyEvent(
                EventType.DETACHED,
                request.getBogieId(),
                -1,
                actorId,
                LocalDateTime.now()));

        return true;
    }

    /**
     * Backward-compatible overloads for UC-04 callers that do not yet pass a log.
     * Delegates to the instrumented overload with a no-op log and "SYSTEM" actor.
     */
    public static void attach(List<Bogie> bogies, AttachRequest request) {
        attach(bogies, request, new JourneyLog("NOLOG"), "SYSTEM");
    }

    public static boolean detach(List<Bogie> bogies, DetachRequest request) {
        return detach(bogies, request, new JourneyLog("NOLOG"), "SYSTEM");
    }

    /**
     * Linear scan helper — finds the first bogie in the list matching the given ID.
     *
     * @param bogies  list to search
     * @param bogieId ID to look up
     * @return the matching Bogie, or null if not found
     */
    private static Bogie findById(List<Bogie> bogies, String bogieId) {
        for (Bogie b : bogies) {
            if (b.getBogieId().equals(bogieId)) return b;
        }
        return null;
    }
}
