public class ValidationPrinter {

    public void print(ValidationResult result) {
        System.out.println("\n========================================");
        System.out.println("        CAPACITY & SAFETY REPORT");
        System.out.println("========================================");

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
            System.out.println("Violations:");
            for (String violation : result.getViolations()) {
                System.out.println("  [!] " + violation);
            }
        }

        System.out.println("========================================\n");
    }
}
