public final class AttachRequest {

    private final Bogie bogie;
    private final int targetPosition; // -1 means rear

    public AttachRequest(Bogie bogie, int targetPosition) {
        this.bogie = bogie;
        this.targetPosition = targetPosition;
    }

    public Bogie getBogie() {
        return bogie;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public boolean isRear() {
        return targetPosition == -1;
    }
}
