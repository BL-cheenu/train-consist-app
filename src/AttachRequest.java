/**
 * Immutable value object that encapsulates the parameters for a bogie attach operation (UC-04).
 *
 * <p>Carries the bogie to insert and the desired target position.
 * A {@code targetPosition} of {@code -1} signals "attach at rear" — this avoids
 * a separate boolean flag and keeps the API minimal.</p>
 */
public final class AttachRequest {

    /** The bogie to be inserted into the consist. */
    private final Bogie bogie;

    /**
     * The zero-based index at which to insert the bogie.
     * Special value {@code -1} means append to the rear of the consist.
     */
    private final int targetPosition;

    /**
     * Constructs an AttachRequest for the given bogie and target position.
     *
     * @param bogie          the bogie to attach
     * @param targetPosition insertion index, or -1 for rear
     */
    public AttachRequest(Bogie bogie, int targetPosition) {
        this.bogie = bogie;
        this.targetPosition = targetPosition;
    }

    /**
     * Returns the bogie to be attached.
     *
     * @return the bogie instance
     */
    public Bogie getBogie() {
        return bogie;
    }

    /**
     * Returns the target position index for insertion.
     * Returns {@code -1} if the bogie should be appended to the rear.
     *
     * @return target index or -1 for rear
     */
    public int getTargetPosition() {
        return targetPosition;
    }

    /**
     * Convenience method — returns {@code true} if the bogie should be added to the rear.
     *
     * @return true when targetPosition == -1
     */
    public boolean isRear() {
        return targetPosition == -1;
    }
}
