import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O(1) bogie lookup index backed by a {@link HashMap} (UC-10).
 *
 * <p>Built once from the live bogie list at startup — not rebuilt on every operation.
 * Because the HashMap stores references to the same {@link Bogie} objects as the List,
 * updating a bogie through the index is immediately reflected in the list too.
 * This is the reference semantics guarantee: one object, two handles.</p>
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>{@code build()} populates the map in O(n) — done once at startup.</li>
 *   <li>{@code lookup()} uses {@code HashMap.get()} — O(1) average.</li>
 *   <li>{@code update()} modifies the Bogie in-place via setters — no map.put() needed
 *       because the reference already points to the same object.</li>
 *   <li>{@code getOrDefault(id, null)} handles missing IDs cleanly
 *       without null-check boilerplate at call sites.</li>
 * </ul>
 */
public class BogieIndex {

    /**
     * The internal HashMap: bogieId → Bogie reference.
     * Provides O(1) average lookup, insert, and update.
     * Stores references — the same Bogie objects live in both this map and the List.
     */
    private final Map<String, Bogie> index;

    /**
     * Private constructor — use {@link #build(List)} to create an instance.
     */
    private BogieIndex() {
        this.index = new HashMap<>();
    }

    /**
     * Builds a BogieIndex from the given bogie list in O(n).
     * Each bogie's ID is mapped to its reference — the List is not copied.
     *
     * <p>The index must be built once and reused — not rebuilt on every operation.</p>
     *
     * @param bogies the live bogie list from TrainConsist
     * @return a populated BogieIndex ready for O(1) lookups
     */
    public static BogieIndex build(List<Bogie> bogies) {
        BogieIndex bogieIndex = new BogieIndex();
        for (Bogie b : bogies) {
            // Store reference — same object as in the List
            bogieIndex.index.put(b.getBogieId(), b);
        }
        return bogieIndex;
    }

    /**
     * Looks up a bogie by ID in O(1) average time.
     * Returns null if the ID is not in the index — use {@link #contains(String)} to check first.
     *
     * @param bogieId the bogie ID to look up
     * @return the Bogie reference, or null if not found
     */
    public Bogie lookup(String bogieId) {
        // getOrDefault returns null for missing IDs — no NPE risk at call sites
        return index.getOrDefault(bogieId, null);
    }

    /**
     * Updates the cargo details of a bogie in-place via O(1) HashMap lookup.
     *
     * <p>Because the map holds a reference to the same Bogie object as the List,
     * calling setters on the retrieved Bogie is immediately visible in both structures.
     * No {@code map.put()} is needed — the reference is unchanged.</p>
     *
     * @param update the CargoUpdate containing the target ID and new cargo details
     * @return true if the bogie was found and updated, false if ID not in index
     */
    public boolean update(CargoUpdate update) {
        Bogie bogie = index.getOrDefault(update.getBogieId(), null);

        if (bogie == null) {
            return false;
        }

        // Mutate in-place — reference semantics ensure List stays in sync automatically
        bogie.setCargoWeight(update.getNewCargoWeight());
        bogie.setCargoDesc(update.getNewCargoDesc());
        return true;
    }

    /**
     * Returns true if the given bogie ID is present in the index.
     *
     * @param bogieId the bogie ID to check
     * @return true if the ID is indexed
     */
    public boolean contains(String bogieId) {
        return index.containsKey(bogieId);
    }

    /**
     * Returns the total number of bogies in this index.
     *
     * @return count of indexed bogies
     */
    public int size() {
        return index.size();
    }
}
