import java.util.List;

/**
 * Immutable value object returned by {@link MarshalService#merge} after a marshal operation.
 *
 * <p>Carries three pieces of information:
 * <ul>
 *   <li>{@code success} — whether the merge was committed or rejected.</li>
 *   <li>{@code conflicts} — list of duplicate bogie IDs found (empty if no conflicts).</li>
 *   <li>{@code mergedConsist} — the resulting consist after a successful merge,
 *       or {@code null} if the merge was rejected.</li>
 * </ul>
 *
 * <p>Being immutable, this object is safe to pass around and inspect without side effects.</p>
 */
public final class MarshalResult {

    /**
     * True if the merge was committed successfully (no duplicate IDs found).
     * False if any conflict was detected — in that case the merge was entirely rolled back.
     */
    private final boolean success;

    /**
     * List of bogie IDs that caused a conflict (already existed in the fleet registry).
     * Empty when {@code success} is true.
     */
    private final List<String> conflicts;

    /**
     * The merged TrainConsist containing bogies from both consists A and B.
     * Null when {@code success} is false — the merge was rejected.
     */
    private final TrainConsist mergedConsist;

    /**
     * Constructs a MarshalResult with all fields set at creation time.
     *
     * @param success       true if the merge was committed, false if rejected
     * @param conflicts     list of conflicting bogie IDs (empty on success)
     * @param mergedConsist the merged consist (null on failure)
     */
    public MarshalResult(boolean success, List<String> conflicts, TrainConsist mergedConsist) {
        this.success = success;
        this.conflicts = conflicts;
        this.mergedConsist = mergedConsist;
    }

    /**
     * Returns true if the merge was committed successfully.
     *
     * @return true on success, false if conflicts caused a rollback
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the list of conflicting bogie IDs detected during the merge check.
     *
     * @return list of duplicate bogie ID strings (empty if no conflicts)
     */
    public List<String> getConflicts() {
        return conflicts;
    }

    /**
     * Returns the merged TrainConsist on success, or null if the merge was rejected.
     *
     * @return merged consist, or null
     */
    public TrainConsist getMergedConsist() {
        return mergedConsist;
    }
}
