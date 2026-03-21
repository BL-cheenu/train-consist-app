import java.util.HashSet;
import java.util.Set;

/**
 * Singleton fleet registry that enforces globally unique bogie IDs across all consists (UC-07).
 *
 * <p>Uses a {@link HashSet} internally for O(1) average-case {@code contains()} checks.
 * The uniqueness guarantee depends on {@link Bogie#equals} and {@link Bogie#hashCode}
 * both being based on {@code bogieId} — if they are inconsistent, two bogies with the
 * same ID could be stored silently.</p>
 *
 * <p>Singleton pattern: only one FleetRegistry exists per JVM.
 * All consists share this single instance, so a bogie ID registered in one consist
 * is visible when validating another.</p>
 *
 * <p>Thread safety is not required for this CLI application — the singleton is
 * accessed sequentially.</p>
 */
public class FleetRegistry {

    /** The single instance — created once, shared across all consists. */
    private static FleetRegistry instance;

    /**
     * The set of all registered bogie IDs.
     * HashSet gives O(1) average for add(), contains(), and remove().
     * Uniqueness is enforced automatically — adding a duplicate ID is silently ignored
     * by HashSet.add(), which is why we check contains() explicitly before committing.
     */
    private final Set<String> registeredIds;

    /** Private constructor — prevents external instantiation. */
    private FleetRegistry() {
        this.registeredIds = new HashSet<>();
    }

    /**
     * Returns the single FleetRegistry instance, creating it on the first call.
     *
     * @return the singleton FleetRegistry
     */
    public static FleetRegistry getInstance() {
        if (instance == null) {
            instance = new FleetRegistry();
        }
        return instance;
    }

    /**
     * Resets the singleton for testing purposes — clears all registered IDs
     * and destroys the current instance so the next call to {@link #getInstance()}
     * creates a fresh registry.
     *
     * <p>Must only be called from test setup methods — never in production code.</p>
     */
    public static void resetForTesting() {
        instance = null;
    }

    /**
     * Registers a bogie ID in the fleet registry.
     * If the ID is already registered, this call is a no-op (HashSet silently rejects duplicates).
     * Use {@link #contains(String)} before calling this to detect conflicts explicitly.
     *
     * @param bogieId the bogie ID to register
     */
    public void register(String bogieId) {
        registeredIds.add(bogieId);
    }

    /**
     * Returns true if the given bogie ID is already registered in the fleet.
     * O(1) average-case lookup via HashSet.
     *
     * @param bogieId the bogie ID to check
     * @return true if this ID is already present in the registry
     */
    public boolean contains(String bogieId) {
        return registeredIds.contains(bogieId);
    }

    /**
     * Removes a bogie ID from the fleet registry.
     * Used when a bogie is permanently decommissioned or re-registered with a new ID.
     *
     * @param bogieId the bogie ID to deregister
     * @return true if the ID was present and removed, false if it was not registered
     */
    public boolean deregister(String bogieId) {
        return registeredIds.remove(bogieId);
    }

    /**
     * Re-registers a bogie under a new ID — removes the old ID and adds the new one.
     * Used when the operator force-assigns a new ID to resolve a conflict.
     *
     * @param oldBogieId the existing registered ID to remove
     * @param newBogieId the new ID to register in its place
     * @return true if the old ID was found and replaced, false if old ID was not registered
     */
    public boolean reRegister(String oldBogieId, String newBogieId) {
        if (!registeredIds.contains(oldBogieId)) {
            return false;
        }
        registeredIds.remove(oldBogieId);
        registeredIds.add(newBogieId);
        return true;
    }

    /**
     * Returns the total number of bogie IDs currently registered in the fleet.
     *
     * @return count of registered IDs
     */
    public int size() {
        return registeredIds.size();
    }

    /**
     * Returns true if no bogie IDs are currently registered.
     *
     * @return true when the registry is empty
     */
    public boolean isEmpty() {
        return registeredIds.isEmpty();
    }
}
