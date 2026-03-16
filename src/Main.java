import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     TRAIN CONSIST MANAGEMENT APP");
        System.out.println("========================================\n");

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

        // UC-03: Search bogie by ID
        BogieSearchService searchService = new BogieSearchService();
        BogieSearchPrinter searchPrinter = new BogieSearchPrinter();

        System.out.println("========================================");
        System.out.println("         BOGIE SEARCH");
        System.out.println("========================================");

        while (true) {
            String bogieId = readBogieId(scanner);
            if (bogieId.equalsIgnoreCase("exit")) {
                break;
            }

            Optional<BogieSearchResult> searchResult =
                    searchService.findById(consist.getBogies(), bogieId);

            if (searchResult.isPresent()) {
                searchPrinter.printFound(searchResult.get(), consist.getBogies().length);
            } else {
                searchPrinter.printNotFound(bogieId, consist.getTrainNumber());
            }

            System.out.print("Search another bogie? (yes / no): ");
            String again = scanner.nextLine().trim().toLowerCase();
            if (!again.equals("yes")) {
                break;
            }
        }

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

    private static String readBogieId(Scanner scanner) {
        while (true) {
            System.out.print("Enter Bogie ID to search (or 'exit' to quit): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  Bogie ID cannot be empty. Please try again.");
        }
    }
}
