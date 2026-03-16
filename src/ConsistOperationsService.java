import java.util.List;

/**
 * Service that performs attach and detach operations on a consist's bogie list (UC-04).
 *
 * <p>Uses {@code List<Bogie>} (backed by ArrayList) so all operations benefit from
 * the Collection interface contract — callers don't need to know the underlying impl.</p>
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>{@code attach} clamps out-of-range positions to the rear rather than throwing.</li>
 *   <li>{@code detach} uses {@code List.remove(Object)} which relies on {@link Bogie#equals}
 *       being based on {@code bogieId} — without a correct equals(), remove() would silently fail.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ConsistOperationsService {

    /**
     * Inserts a bogie into the consist at the position specified by the {@link AttachRequest}.
     *
     * <p>Position handling:
     * <ul>
     *   <li>{@code -1} (rear flag): appends to end of list.</li>
     *   <li>{@code 0} or negative: inserts at head (index 0).</li>
     *   <li>Beyond current size: clamps to rear and notifies the operator.</li>
     *   <li>Valid mid-range: inserts at exact index using {@code list.add(index, bogie)}.</li>
     * </ul>
     *
     * @param bogies  the live bogie list from TrainConsist
     * @param request AttachRequest containing the bogie and target position
     */
    public static void attach(List<Bogie> bogies, AttachRequest request) {
        int position = request.getTargetPosition();

        if (request.isRear() || position >= bogies.size()) {
            // Notify operator if position was explicitly out of range (not just a rear request)
            if (!request.isRear() && position >= bogies.size()) {
                System.out.println("  INFO: Position " + position
                        + " exceeds consist size (" + bogies.size() + "). Bogie attached at rear.");
            }
            bogies.add(request.getBogie()); // append to end
        } else if (position <= 0) {
            bogies.add(0, request.getBogie()); // insert at head
        } else {
            bogies.add(position, request.getBogie()); // insert at exact mid-range index
        }
    }

    /**
     * Removes the bogie identified by the given {@link DetachRequest} from the consist.
     *
     * <p>Uses a linear scan to find the bogie by ID, then calls {@code list.remove(Object)}.
     * The remove works correctly because {@link Bogie#equals} is based on {@code bogieId}.</p>
     *
     * <p>If the consist becomes empty after removal, a warning is printed.</p>
     *
     * @param bogies  the live bogie list from TrainConsist
     * @param request DetachRequest containing the ID of the bogie to remove
     * @return true if the bogie was found and removed, false if not found
     */
    public static boolean detach(List<Bogie> bogies, DetachRequest request) {
        Bogie target = findById(bogies, request.getBogieId());

        if (target == null) {
            System.out.println("  ERROR: Bogie '" + request.getBogieId() + "' not found in consist.");
            return false;
        }

        bogies.remove(target); // equals() on bogieId ensures the right element is removed

        if (bogies.isEmpty()) {
            System.out.println("  WARNING: Consist is now empty.");
        }

        return true;
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
