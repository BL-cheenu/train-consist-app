import java.util.Scanner;

public class ConsistBuilder {

    private Scanner scanner;

    public ConsistBuilder(Scanner scanner) {
        this.scanner = scanner;
    }

    public TrainConsist build() {
        System.out.print("Enter Train Number: ");
        String trainNumber = scanner.nextLine().trim();

        System.out.print("Enter Route: ");
        String route = scanner.nextLine().trim();

        int bogieCount = readBogieCount();
        Bogie[] bogies = new Bogie[bogieCount];

        for (int i = 0; i < bogieCount; i++) {
            System.out.println("\n--- Bogie " + i + " ---");
            bogies[i] = buildSingleBogie(scanner);
        }

        return new TrainConsist(trainNumber, route, bogies);
    }

    public Bogie buildSingleBogie(Scanner sc) {
        System.out.print("  Bogie ID: ");
        String bogieId = sc.nextLine().trim();

        BogieType bogieType = readBogieType(sc);

        if (bogieType == BogieType.PASSENGER) {
            PassengerSubType subType = readPassengerSubType(sc);
            int capacity = readCapacity(sc);
            return new PassengerBogie(bogieId, subType, capacity);
        } else {
            GoodsSubType subType = readGoodsSubType(sc);
            int capacity = readCapacity(sc);
            return new GoodsBogie(bogieId, subType, capacity);
        }
    }

    private int readBogieCount() {
        while (true) {
            System.out.print("Enter number of bogies: ");
            String input = scanner.nextLine().trim();
            try {
                int count = Integer.parseInt(input);
                if (count >= 0) return count;
                System.out.println("  Number of bogies cannot be negative. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a whole number.");
            }
        }
    }

    private BogieType readBogieType(Scanner sc) {
        while (true) {
            System.out.print("  Bogie Type (PASSENGER / GOODS): ");
            String input = sc.nextLine().trim().toUpperCase();
            try {
                return BogieType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid type. Valid options: PASSENGER, GOODS");
            }
        }
    }

    private PassengerSubType readPassengerSubType(Scanner sc) {
        while (true) {
            System.out.print("  Subtype (SLEEPER / AC_CHAIR / FIRST_CLASS): ");
            String input = sc.nextLine().trim().toUpperCase();
            try {
                return PassengerSubType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid subtype. Valid options: SLEEPER, AC_CHAIR, FIRST_CLASS");
            }
        }
    }

    private GoodsSubType readGoodsSubType(Scanner sc) {
        while (true) {
            System.out.print("  Subtype (RECTANGULAR / CYLINDRICAL): ");
            String input = sc.nextLine().trim().toUpperCase();
            try {
                return GoodsSubType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid subtype. Valid options: RECTANGULAR, CYLINDRICAL");
            }
        }
    }

    private int readCapacity(Scanner sc) {
        while (true) {
            System.out.print("  Capacity: ");
            String input = sc.nextLine().trim();
            try {
                int cap = Integer.parseInt(input);
                if (cap > 0) return cap;
                System.out.println("  Capacity must be a positive number. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a numeric capacity.");
            }
        }
    }
}
