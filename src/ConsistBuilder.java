import java.util.Scanner;

/**
 * Reads operator input from the console to construct a {@link TrainConsist} (UC-01).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Prompt for train number and route name.</li>
 *   <li>Prompt for the number of bogies and allocate a fixed-size array.</li>
 *   <li>For each bogie, prompt for ID, type, subtype, and capacity with validation loops.</li>
 * </ul>
 *
 * <p>All methods are static and share a single {@link Scanner} instance bound to
 * {@code System.in}. Input validation re-prompts on invalid entries — it never
 * accepts unrecognised enum strings or non-numeric capacity values.</p>
 */
public class ConsistBuilder {

    /** Single Scanner instance shared across all static methods — reads from System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Entry point for UC-01 — reads all consist data from the console and returns
     * a fully populated {@link TrainConsist}.
     *
     * <p>Flow: train number → route → bogie count → per-bogie details (ID, type, subtype, capacity).</p>
     *
     * @return a new TrainConsist constructed from operator-entered data
     */
    public static TrainConsist buildConsist() {
        System.out.print("Enter Train Number: ");
        String trainNumber = scanner.nextLine().trim();

        System.out.print("Enter Route: ");
        String route = scanner.nextLine().trim();

        int bogieCount = readBogieCount();

        // Allocate fixed-size array — size is known after operator enters the count
        Bogie[] bogies = new Bogie[bogieCount];

        for (int i = 0; i < bogieCount; i++) {
            System.out.println("\n--- Bogie " + i + " ---");
            bogies[i] = buildSingleBogie();
        }

        return new TrainConsist(trainNumber, route, bogies);
    }

    /**
     * Builds a single bogie by reading its ID, type, subtype, and capacity from the console.
     * Also used by {@link JunctionOperationsMenu} when attaching a new bogie in UC-04.
     *
     * <p>Delegates to type-specific subtype readers based on the operator's BogieType choice.</p>
     *
     * @return a new PassengerBogie or GoodsBogie based on operator input
     */
    public static Bogie buildSingleBogie() {
        System.out.print("  Bogie ID: ");
        String bogieId = scanner.nextLine().trim();

        BogieType bogieType = readBogieType();

        if (bogieType == BogieType.PASSENGER) {
            PassengerSubType subType = readPassengerSubType();
            int capacity = readCapacity();
            return new PassengerBogie(bogieId, subType, capacity);
        } else {
            GoodsSubType subType = readGoodsSubType();
            int capacity = readCapacity();
            return new GoodsBogie(bogieId, subType, capacity);
        }
    }

    /**
     * Reads and validates the number of bogies to add to the consist.
     * Loops until the operator enters a non-negative integer.
     *
     * @return validated bogie count (0 or more)
     */
    private static int readBogieCount() {
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

    /**
     * Reads and validates a BogieType from the console.
     * Loops until the operator enters "PASSENGER" or "GOODS" (case-insensitive).
     *
     * @return validated BogieType enum value
     */
    private static BogieType readBogieType() {
        while (true) {
            System.out.print("  Bogie Type (PASSENGER / GOODS): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return BogieType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid type. Valid options: PASSENGER, GOODS");
            }
        }
    }

    /**
     * Reads and validates a PassengerSubType from the console.
     * Loops until the operator enters a recognised subtype (case-insensitive).
     *
     * @return validated PassengerSubType enum value
     */
    private static PassengerSubType readPassengerSubType() {
        while (true) {
            System.out.print("  Subtype (SLEEPER / AC_CHAIR / FIRST_CLASS): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return PassengerSubType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid subtype. Valid options: SLEEPER, AC_CHAIR, FIRST_CLASS");
            }
        }
    }

    /**
     * Reads and validates a GoodsSubType from the console.
     * Loops until the operator enters a recognised subtype (case-insensitive).
     *
     * @return validated GoodsSubType enum value
     */
    private static GoodsSubType readGoodsSubType() {
        while (true) {
            System.out.print("  Subtype (RECTANGULAR / CYLINDRICAL): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return GoodsSubType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("  Invalid subtype. Valid options: RECTANGULAR, CYLINDRICAL");
            }
        }
    }

    /**
     * Reads and validates a bogie capacity from the console.
     * Loops until the operator enters a positive integer.
     *
     * @return validated positive capacity value
     */
    private static int readCapacity() {
        while (true) {
            System.out.print("  Capacity: ");
            String input = scanner.nextLine().trim();
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
