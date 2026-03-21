import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Service that performs yard shunting operations on a consist's bogie list (UC-05),
 * and publishes an immutable {@link JourneyEvent} to the {@link JourneyLog} after
 * every successful mutation (UC-06).
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ShuntingService {

    /**
     * Moves the bogie with the given ID to position 0 of the consist,
     * then records a MOVED_TO_FRONT event in the journey log.
     *
     * @param bogies  the live bogie list to modify
     * @param bogieId the ID of the bogie to move to front
     * @param log     the JourneyLog to record the event into
     * @param actorId the ID of the operator performing this action
     * @return true if the bogie was moved, false if already first or not found
     */
    public static boolean moveToFront(List<Bogie> bogies, String bogieId,
                                      JourneyLog log, String actorId) {
        if (bogies.isEmpty()) {
            System.out.println("  ERROR: Consist is empty.");
            return false;
        }

        // No-op guard — already at the front
        if (bogies.get(0).getBogieId().equals(bogieId)) {
            System.out.println("  INFO: Bogie '" + bogieId + "' is already at position 0. Skipping.");
            return false;
        }

        // Linear scan to locate the target bogie
        Bogie target = null;
        for (Bogie b : bogies) {
            if (b.getBogieId().equals(bogieId)) { target = b; break; }
        }

        if (target == null) {
            System.out.println("  ERROR: Bogie '" + bogieId + "' not found in consist.");
            return false;
        }

        bogies.remove(target);
        bogies.add(0, target);

        // Record MOVED_TO_FRONT — position is always 0
        log.record(new JourneyEvent(
                EventType.MOVED_TO_FRONT, bogieId, 0, actorId, LocalDateTime.now()));
        return true;
    }

    /**
     * Appends a brake van bogie to the rear of the consist,
     * then records a BRAKE_VAN_ADDED event in the journey log.
     *
     * @param bogies   the live bogie list to modify
     * @param brakeVan the brake van bogie to append
     * @param log      the JourneyLog to record the event into
     * @param actorId  the ID of the operator performing this action
     */
    public static void addBrakeVan(List<Bogie> bogies, Bogie brakeVan,
                                   JourneyLog log, String actorId) {
        if (bogies instanceof Deque) {
            ((Deque<Bogie>) bogies).addLast(brakeVan);
        } else {
            bogies.add(brakeVan);
        }

        // Record BRAKE_VAN_ADDED — position is the last index after append
        log.record(new JourneyEvent(
                EventType.BRAKE_VAN_ADDED,
                brakeVan.getBogieId(),
                bogies.size() - 1,
                actorId,
                LocalDateTime.now()));
    }

    /**
     * Swaps the bogies at positions i and i+1,
     * then records a SWAPPED event in the journey log.
     *
     * @param bogies   the live bogie list to modify
     * @param position zero-based index of the first bogie to swap
     * @param log      the JourneyLog to record the event into
     * @param actorId  the ID of the operator performing this action
     * @return true if the swap succeeded, false if position is out of range
     */
    public static boolean swapAdjacent(List<Bogie> bogies, int position,
                                       JourneyLog log, String actorId) {
        if (position < 0 || position >= bogies.size() - 1) {
            System.out.println("  ERROR: Cannot swap at position " + position
                    + ". Valid range: 0 to " + (bogies.size() - 2) + ".");
            return false;
        }

        Collections.swap(bogies, position, position + 1);

        // Record SWAPPED — bogieId is the bogie now at position i after swap
        log.record(new JourneyEvent(
                EventType.SWAPPED,
                bogies.get(position).getBogieId(),
                position,
                actorId,
                LocalDateTime.now()));
        return true;
    }

    // ── Backward-compatible overloads for UC-05 callers without a log ──────────

    /** Backward-compatible overload — delegates with no-op log and SYSTEM actor. */
    public static boolean moveToFront(List<Bogie> bogies, String bogieId) {
        return moveToFront(bogies, bogieId, new JourneyLog("NOLOG"), "SYSTEM");
    }

    /** Backward-compatible overload — delegates with no-op log and SYSTEM actor. */
    public static void addBrakeVan(List<Bogie> bogies, Bogie brakeVan) {
        addBrakeVan(bogies, brakeVan, new JourneyLog("NOLOG"), "SYSTEM");
    }

    /** Backward-compatible overload — delegates with no-op log and SYSTEM actor. */
    public static boolean swapAdjacent(List<Bogie> bogies, int position) {
        return swapAdjacent(bogies, position, new JourneyLog("NOLOG"), "SYSTEM");
    }

    /**
     * Runs a performance benchmark comparing ArrayList vs LinkedList for two access patterns.
     * (Unchanged from UC-05 — benchmark does not mutate the consist and needs no logging.)
     *
     * @param iterations number of operations per collection per test
     * @return list of four BenchmarkResult objects
     */
    public static List<BenchmarkResult> benchmark(int iterations) {
        List<BenchmarkResult> results = new ArrayList<>();

        // HEAD_INSERT — ArrayList O(n) vs LinkedList O(1)
        ArrayList<Bogie> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++)
            arrayList.add(0, new PassengerBogie("B-" + i, PassengerSubType.SLEEPER, 60));
        results.add(new BenchmarkResult("HEAD_INSERT", "ArrayList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        LinkedList<Bogie> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++)
            linkedList.addFirst(new PassengerBogie("B-" + i, PassengerSubType.SLEEPER, 60));
        results.add(new BenchmarkResult("HEAD_INSERT", "LinkedList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        // RANDOM_GET — ArrayList O(1) vs LinkedList O(n)
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) arrayList.get(i % arrayList.size());
        results.add(new BenchmarkResult("RANDOM_GET", "ArrayList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) linkedList.get(i % linkedList.size());
        results.add(new BenchmarkResult("RANDOM_GET", "LinkedList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        return results;
    }
}
