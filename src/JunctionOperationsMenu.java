import java.util.List;
import java.util.Scanner;

public class JunctionOperationsMenu {

    private final ConsistOperationsService operationsService = new ConsistOperationsService();
    private final SafetyValidator validator = new SafetyValidator();
    private final ConsistPrinter printer = new ConsistPrinter();
    private final ValidationPrinter validationPrinter = new ValidationPrinter();
    private final ConsistBuilder consistBuilder;

    public JunctionOperationsMenu(Scanner scanner) {
        this.consistBuilder = new ConsistBuilder(scanner);
    }

    public void run(TrainConsist consist, RouteType routeType, Scanner scanner) {
        List<Bogie> bogies = consist.getBogieList();

        System.out.println("\n========================================");
        System.out.println("       JUNCTION OPERATIONS — UC-04");
        System.out.println("========================================");

        while (true) {
            printer.print(consist);

            System.out.println("Actions: ATTACH | DETACH | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "ATTACH":
                    handleAttach(bogies, scanner);
                    break;
                case "DETACH":
                    handleDetach(bogies, scanner);
                    break;
                case "DONE":
                    System.out.println("\n--- Final Validation ---");
                    ValidationResult result = validator.validate(bogies, routeType);
                    validationPrinter.print(result);
                    return;
                default:
                    System.out.println("  Invalid action. Choose ATTACH, DETACH, or DONE.");
                    continue;
            }

            // Re-run safety validation after each operation
            if (!bogies.isEmpty()) {
                ValidationResult result = validator.validate(bogies, routeType);
                if (!result.isValid()) {
                    System.out.println("  [!] Safety check after operation:");
                    for (String v : result.getViolations()) {
                        System.out.println("      " + v);
                    }
                    System.out.println("  Operator override accepted — operation kept.\n");
                } else {
                    System.out.println("  Safety check: PASSED\n");
                }
            }
        }
    }

    private void handleAttach(List<Bogie> bogies, Scanner scanner) {
        System.out.println("\n-- Attach Bogie --");
        Bogie newBogie = consistBuilder.buildSingleBogie(scanner);

        System.out.print("  Enter position to attach (0 to " + bogies.size() + ", or REAR): ");
        String posInput = scanner.nextLine().trim().toUpperCase();

        int position;
        if (posInput.equals("REAR")) {
            position = -1;
        } else {
            try {
                position = Integer.parseInt(posInput);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid position. Attaching at rear.");
                position = -1;
            }
        }

        AttachRequest request = new AttachRequest(newBogie, position);
        operationsService.attach(bogies, request);
        System.out.println("  Bogie '" + newBogie.getBogieId() + "' attached successfully.");
    }

    private void handleDetach(List<Bogie> bogies, Scanner scanner) {
        System.out.print("\n-- Detach Bogie --\n  Enter Bogie ID to detach: ");
        String bogieId = scanner.nextLine().trim();

        if (bogieId.isEmpty()) {
            System.out.println("  Bogie ID cannot be empty.");
            return;
        }

        DetachRequest request = new DetachRequest(bogieId);
        boolean removed = operationsService.detach(bogies, request);
        if (removed) {
            System.out.println("  Bogie '" + bogieId + "' detached successfully.");
        }
    }
}
