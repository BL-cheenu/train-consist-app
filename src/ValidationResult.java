public final class ValidationResult {

    private final int passengerCapacity;
    private final int freightTonnage;
    private final String[] violations;
    private final boolean isValid;

    public ValidationResult(int passengerCapacity, int freightTonnage, String[] violations) {
        this.passengerCapacity = passengerCapacity;
        this.freightTonnage = freightTonnage;
        this.violations = violations;
        this.isValid = violations.length == 0;
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public int getFreightTonnage() {
        return freightTonnage;
    }

    public String[] getViolations() {
        return violations;
    }

    public boolean isValid() {
        return isValid;
    }
}
