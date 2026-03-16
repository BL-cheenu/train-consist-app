/**
 * Prints the capacity and safety validation report to the console (UC-02).
 *
 * <p>Displays total passenger seating capacity, freight tonnage, and the
 * overall safety status. If violations exist, each one is listed with a
 * warning prefix. If no passenger coaches exist, a descriptive message is
 * shown instead of a zero count.</p>
 *
 * <p>{@link #printReport} is static for direct call from {@link Main}.
 * {@link #print} is an instance alias kept for UC-04 internal use.</p>
 */
public class ValidationPrinter {

    /**
     * Prints the full capacity and safety report for the given ValidationResult.
     *
     * <p>Format:
     * <pre>
     * Passenger Capacity : X seats  (or "No passenger coaches" if 0)
     * Freight Tonnage    : Y tons
     * Safety Status      : PASSED / FAILED
     * Violations         : [listed if any]
     * </pre>
     *
     * @param result the ValidationResult returned by SafetyValidator
     */
    public static void printReport(ValidationResult result) {
        System.out.println("\n========================================");
        System.out.println("        CAPACITY & SAFETY REPORT");
        System.out.println("========================================");

        // UC-02 edge flow: no PASSENGER bogies — show descriptive message instead of 0
        if (result.getPassengerCapacity() == 0) {
            System.out.println("Passenger Capacity : No passenger coaches");
        } else {
            System.out.println("Passenger Capacity : " + result.getPassengerCapacity() + " seats");
        }

        System.out.println("Freight Tonnage    : " + result.getFreightTonnage() + " tons");
        System.out.println("----------------------------------------");

        if (result.isValid()) {
            System.out.println("Safety Status      : PASSED — No violations found");
        } else {
            System.out.println("Safety Status      : FAILED");
            System.out.println("\nViolations:");
            // Print all violations — UC-02 collects every breach, not just the first
            for (String v : result.getViolations()) {
                System.out.println("  [!] " + v);
            }
        }

        System.out.println("========================================\n");
    }

    /**
     * Instance method alias for {@link #printReport}.
     * Used by {@link JunctionOperationsMenu} (UC-04) which calls {@code printer.print(result)}.
     *
     * @param result the ValidationResult to display
     */
    public void print(ValidationResult result) {
        printReport(result);
    }
}
