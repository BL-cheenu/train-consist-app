import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console menu for station schedule and freight manifest (UC-11).
 *
 * <p>Presents three choices:
 * <ul>
 *   <li><b>ALPHABETICAL</b> — print freight manifest in A→Z station order.</li>
 *   <li><b>BY_TIME</b> — print schedule in departure-time order.</li>
 *   <li><b>DONE</b> — exit the schedule menu.</li>
 * </ul>
 *
 * <p>The schedule is built once from operator-entered station stops at startup.
 * Both TreeMaps are populated in one O(n log n) pass.</p>
 *
 * <p>All methods are static; a shared Scanner reads from System.in.</p>
 */
public class ScheduleMenu {

    /** Shared Scanner instance bound to System.in. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Runs the schedule menu — first reads station stops from the operator,
     * builds the RouteSchedule, then loops until DONE is selected.
     *
     * @param consist the TrainConsist whose bogies can be assigned to station stops
     */
    public static void run(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("   STATION SCHEDULE — UC-11");
        System.out.println("========================================");

        // Read station stops from operator — build schedule once
        List<StationStop> stops = readStops(consist);
        if (stops.isEmpty()) {
            System.out.println("  No stations entered. Skipping schedule.\n");
            return;
        }

        RouteSchedule schedule = ScheduleBuilder.build(stops);
        System.out.println("  Schedule built: " + schedule.size() + " stations.");

        while (true) {
            System.out.println("Actions: ALPHABETICAL | BY_TIME | DONE");
            System.out.print("Enter action: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "ALPHABETICAL":
                    SchedulePrinter.printAlphabetical(schedule);
                    break;

                case "BY_TIME":
                    SchedulePrinter.printByDepartureTime(schedule);
                    break;

                case "DONE":
                    System.out.println("\n  Schedule session complete.\n");
                    return;

                default:
                    System.out.println("  Invalid action. Choose ALPHABETICAL, BY_TIME, or DONE.");
            }
        }
    }

    /**
     * Reads station stop details from the operator.
     * For each stop: station name, departure time (HH:mm), and bogie IDs to detach.
     *
     * @param consist used to resolve bogie IDs to Bogie objects
     * @return list of StationStop objects built from operator input
     */
    private static List<StationStop> readStops(TrainConsist consist) {
        int count = readStopCount();
        List<StationStop> stops = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            System.out.println("\n--- Station " + (i + 1) + " ---");

            System.out.print("  Station name: ");
            String name = scanner.nextLine().trim();

            LocalTime departureTime = readDepartureTime();

            List<Bogie> detached = readDetachedBogies(consist);

            stops.add(new StationStop(name, departureTime, detached));
        }

        return stops;
    }

    /**
     * Reads and validates the number of station stops.
     *
     * @return validated stop count (>= 0)
     */
    private static int readStopCount() {
        while (true) {
            System.out.print("Enter number of station stops: ");
            String input = scanner.nextLine().trim();
            try {
                int count = Integer.parseInt(input);
                if (count >= 0) return count;
                System.out.println("  Count cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Reads and validates a departure time in HH:mm format.
     *
     * @return validated LocalTime
     */
    private static LocalTime readDepartureTime() {
        while (true) {
            System.out.print("  Departure time (HH:mm): ");
            String input = scanner.nextLine().trim();
            try {
                return LocalTime.parse(input,
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time format. Use HH:mm (e.g. 09:30).");
            }
        }
    }

    /**
     * Reads bogie IDs to detach at this station and resolves them from the consist.
     * Accepts comma-separated IDs or empty input for no detachments.
     *
     * @param consist the consist to look up bogies by ID
     * @return list of Bogie objects to detach (may be empty)
     */
    private static List<Bogie> readDetachedBogies(TrainConsist consist) {
        System.out.print("  Bogie IDs to detach (comma-separated, or ENTER for none): ");
        String input = scanner.nextLine().trim();

        List<Bogie> detached = new ArrayList<>();
        if (input.isEmpty()) return detached;

        // Build a quick lookup map from consist
        java.util.Map<String, Bogie> bogieMap = new java.util.HashMap<>();
        for (Bogie b : consist.getBogies()) {
            bogieMap.put(b.getBogieId(), b);
        }

        for (String id : input.split(",")) {
            String trimmed = id.trim();
            Bogie b = bogieMap.get(trimmed);
            if (b != null) {
                detached.add(b);
            } else {
                System.out.println("  WARNING: Bogie '" + trimmed + "' not found — skipped.");
            }
        }

        return detached;
    }
}
