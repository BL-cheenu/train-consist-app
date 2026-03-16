import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     TRAIN CONSIST MANAGEMENT APP");
        System.out.println("========================================");

        // UC-01: Build consist
        ConsistBuilder builder = new ConsistBuilder(scanner);
        TrainConsist consist = builder.build();

        ConsistPrinter printer = new ConsistPrinter();
        printer.print(consist);

        // UC-02: Route type and validation
        RouteType routeType = readRouteType(scanner);

        SafetyValidator validator = new SafetyValidator();
        ValidationResult result = validator.validate(consist.getBogies(), routeType);

        ValidationPrinter validationPrinter = new ValidationPrinter();
        validationPrinter.print(result);

        scanner.close();
    }

    private static RouteType readRouteType(Scanner scanner) {
        while (true) {
            System.out.print("Enter Route Type (SUBURBAN / EXPRESS / FREIGHT): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return RouteType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid route type. Valid options: SUBURBAN, EXPRESS, FREIGHT");
            }
        }
    }
}
