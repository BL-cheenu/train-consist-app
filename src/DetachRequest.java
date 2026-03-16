/**
 * Immutable value object that encapsulates the parameters for a bogie detach operation (UC-04).
 *
 * <p>Carries only the ID of the bogie to remove.
 * {@link ConsistOperationsService#detach} uses this ID to locate the bogie via
 * {@code equals()} and remove it from the list.</p>
 */
public final class DetachRequest {

    /** The ID of the bogie to be removed from the consist. */
    private final String bogieId;

    /**
     * Constructs a DetachRequest for the bogie with the given ID.
     *
     * @param bogieId ID of the bogie to detach (must match an existing bogie's bogieId)
     */
    public DetachRequest(String bogieId) {
        this.bogieId = bogieId;
    }

    /**
     * Returns the ID of the bogie to be detached.
     *
     * @return bogie ID string
     */
    public String getBogieId() {
        return bogieId;
    }
}
