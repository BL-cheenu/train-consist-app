import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Service that performs yard shunting operations on a consist's bogie list (UC-05).
 *
 * <p>Supports four operations:
 * <ul>
 *   <li>{@link #moveToFront} — remove a bogie by ID and re-insert at index 0.</li>
 *   <li>{@link #addBrakeVan} — append a brake van to the rear, using Deque.addLast()
 *       for LinkedList and List.add() for ArrayList.</li>
 *   <li>{@link #swapAdjacent} — swap bogies at positions i and i+1 using
 *       {@code Collections.swap}.</li>
 *   <li>{@link #benchmark} — time 10,000 head inserts and random gets on both
 *       ArrayList and LinkedList to demonstrate their Big-O difference.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ShuntingService {

    /**
     * Moves the bogie with the given ID to the front (index 0) of the consist.
     *
     * <p>Steps: linear scan to find the bogie → remove from current position →
     * insert at index 0. If the bogie is already at index 0, the operation is
     * skipped and the operator is notified.</p>
     *
     * @param bogies  the live bogie list to modify
     * @param bogieId the ID of the bogie to move to the front
     * @return true if the bogie was moved, false if already first or not found
     */
    public static boolean moveToFront(List<Bogie> bogies, String bogieId) {
        if (bogies.isEmpty()) {
            System.out.println("  ERROR: Consist is empty.");
            return false;
        }

        // Skip if already at the front — no-op guard
        if (bogies.get(0).getBogieId().equals(bogieId)) {
            System.out.println("  INFO: Bogie '" + bogieId + "' is already at position 0. Skipping.");
            return false;
        }

        // Linear scan to locate the target bogie
        Bogie target = null;
        for (Bogie b : bogies) {
            if (b.getBogieId().equals(bogieId)) {
                target = b;
                break;
            }
        }

        if (target == null) {
            System.out.println("  ERROR: Bogie '" + bogieId + "' not found in consist.");
            return false;
        }

        bogies.remove(target);     // remove from current position
        bogies.add(0, target);     // re-insert at head
        return true;
    }

    /**
     * Appends a brake van bogie to the rear of the consist.
     *
     * <p>Uses {@code Deque.addLast()} when the list is a {@link LinkedList}
     * (O(1) tail operation), and falls back to {@code List.add()} for ArrayList.</p>
     *
     * @param bogies   the live bogie list to modify
     * @param brakeVan the brake van bogie to append
     */
    public static void addBrakeVan(List<Bogie> bogies, Bogie brakeVan) {
        if (bogies instanceof Deque) {
            // LinkedList implements Deque — addLast() is an O(1) tail pointer operation
            ((Deque<Bogie>) bogies).addLast(brakeVan);
        } else {
            // ArrayList.add() — amortized O(1) append
            bogies.add(brakeVan);
        }
    }

    /**
     * Swaps the bogies at positions {@code i} and {@code i+1} in the consist.
     *
     * <p>Uses {@code Collections.swap} which performs the three-step exchange
     * (temp = a; a = b; b = temp) using indexed access.</p>
     *
     * <p>Rejects the operation if {@code position} is negative or equal to
     * the last index (no {@code i+1} exists).</p>
     *
     * @param bogies   the live bogie list to modify
     * @param position zero-based index of the first bogie to swap
     * @return true if the swap succeeded, false if position is out of range
     */
    public static boolean swapAdjacent(List<Bogie> bogies, int position) {
        // Validate: position must be in range [0, size-2] so that i+1 exists
        if (position < 0 || position >= bogies.size() - 1) {
            System.out.println("  ERROR: Cannot swap at position " + position
                    + ". Valid range: 0 to " + (bogies.size() - 2) + ".");
            return false;
        }
        Collections.swap(bogies, position, position + 1);
        return true;
    }

    /**
     * Runs a performance benchmark comparing ArrayList vs LinkedList for two access patterns.
     *
     * <p>Operations timed:
     * <ul>
     *   <li><b>HEAD_INSERT:</b> ArrayList.add(0, e) vs LinkedList.addFirst(e) — {@code iterations} times.
     *       ArrayList shifts all elements right (O(n)); LinkedList rewires one pointer (O(1)).</li>
     *   <li><b>RANDOM_GET:</b> ArrayList.get(i) vs LinkedList.get(i) — {@code iterations} times.
     *       ArrayList jumps directly to the index (O(1)); LinkedList traverses from head (O(n)).</li>
     * </ul>
     *
     * <p>Timing uses {@code System.nanoTime()} converted to milliseconds for readable output.</p>
     *
     * @param iterations number of operations per collection per test (typically 10,000)
     * @return list of four BenchmarkResult objects in order:
     *         HEAD_INSERT/ArrayList, HEAD_INSERT/LinkedList, RANDOM_GET/ArrayList, RANDOM_GET/LinkedList
     */
    public static List<BenchmarkResult> benchmark(int iterations) {
        List<BenchmarkResult> results = new ArrayList<>();

        // --- HEAD_INSERT: ArrayList (O(n) per insert — shifts all elements right) ---
        ArrayList<Bogie> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            arrayList.add(0, new PassengerBogie("B-" + i, PassengerSubType.SLEEPER, 60));
        }
        results.add(new BenchmarkResult("HEAD_INSERT", "ArrayList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        // --- HEAD_INSERT: LinkedList (O(1) per insert — rewires head pointer only) ---
        LinkedList<Bogie> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            linkedList.addFirst(new PassengerBogie("B-" + i, PassengerSubType.SLEEPER, 60));
        }
        results.add(new BenchmarkResult("HEAD_INSERT", "LinkedList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        // --- RANDOM_GET: ArrayList (O(1) — direct index arithmetic on backing array) ---
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            arrayList.get(i % arrayList.size());
        }
        results.add(new BenchmarkResult("RANDOM_GET", "ArrayList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        // --- RANDOM_GET: LinkedList (O(n) — traverses from head to reach index i) ---
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            linkedList.get(i % linkedList.size());
        }
        results.add(new BenchmarkResult("RANDOM_GET", "LinkedList", iterations,
                (System.nanoTime() - start) / 1_000_000));

        return results;
    }
}
