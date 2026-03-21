import java.time.LocalDateTime;
import java.util.LinkedHashSet;

/**
 * Represents the safety manifest for a train consist — the guard's hand-off document
 * listing every bogie in the exact order it was attached (UC-08).
 *
 * <p>Uses a {@link LinkedHashSet} to store bogie IDs because:
 * <ul>
 *   <li><b>Uniqueness:</b> a bogie can only appear once in the manifest
 *       (ArrayList would allow duplicates).</li>
 *   <li><b>Insertion order:</b> iterating always yields IDs in the order they were
 *       first inserted (HashSet gives unpredictable order).</li>
 * </ul>
 *
 * <p>This is an immutable snapshot — it captures consist state at the moment
 * {@link ManifestBuilder#build} was called. Changes to the live consist after
 * that point are not reflected here.</p>
 */
public final class SafetyManifest {

    /**
     * Ordered set of bogie IDs in attachment order.
     * LinkedHashSet guarantees: no duplicates + insertion order preserved.
     * Detached bogies are removed at build time — only currently-attached bogies appear.
     */
    private final LinkedHashSet<String> bogiesInOrder;

    /** The train number this manifest belongs to (e.g. "TN-2201"). */
    private final String trainNumber;

    /**
     * The exact moment this manifest snapshot was generated.
     * Printed on the manifest so the guard knows when it was issued.
     */
    private final LocalDateTime generatedAt;

    /**
     * Reference to the live consist — used by ManifestPrinter to look up
     * full bogie details (type, subtype, capacity) by ID.
     */
    private final TrainConsist consist;

    /**
     * Constructs a SafetyManifest snapshot.
     *
     * @param bogiesInOrder ordered set of currently-attached bogie IDs in attachment order
     * @param trainNumber   the train number this manifest covers
     * @param generatedAt   the timestamp when this snapshot was taken
     * @param consist       the live consist used to resolve bogie details by ID
     */
    public SafetyManifest(LinkedHashSet<String> bogiesInOrder, String trainNumber,
                          LocalDateTime generatedAt, TrainConsist consist) {
        this.bogiesInOrder = bogiesInOrder;
        this.trainNumber = trainNumber;
        this.generatedAt = generatedAt;
        this.consist = consist;
    }

    /**
     * Returns the ordered set of bogie IDs in attachment order.
     * Iterating this set always yields IDs in the order they were first inserted.
     *
     * @return LinkedHashSet of bogie IDs (no duplicates, insertion-ordered)
     */
    public LinkedHashSet<String> getBogiesInOrder() {
        return bogiesInOrder;
    }

    /**
     * Returns the train number this manifest belongs to.
     *
     * @return train number string
     */
    public String getTrainNumber() {
        return trainNumber;
    }

    /**
     * Returns the timestamp when this manifest snapshot was generated.
     *
     * @return LocalDateTime of snapshot creation
     */
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    /**
     * Returns the consist reference used to look up full bogie details.
     *
     * @return the TrainConsist associated with this manifest
     */
    public TrainConsist getConsist() {
        return consist;
    }

    /**
     * Returns the number of bogies currently listed in this manifest.
     *
     * @return count of bogies in the manifest
     */
    public int size() {
        return bogiesInOrder.size();
    }

    /**
     * Returns true if no bogies are listed in this manifest.
     *
     * @return true when bogiesInOrder is empty
     */
    public boolean isEmpty() {
        return bogiesInOrder.isEmpty();
    }
}
