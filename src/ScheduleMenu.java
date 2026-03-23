import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interactive console menu for station schedule and freight manifest (UC-11).
 * Updated in UC-13 to expose the built station list for analytics pipelines.
 */
public class ScheduleMenu {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Original UC-11 entry point — builds schedule and runs the menu.
     *
     * @param consist the TrainConsist whose bogies can be assigned to stops
     */
    public static void run(TrainConsist consist) {
        runWithStations(consist, new ArrayList<>());
    }

    /**
     * UC-13 entry point — builds schedule, runs menu, and fills the given
     * stations list so the analytics dashboard can use it.
     *
     * @param consist  the TrainConsist
     * @param stations output list — populated with the built StationStops
     */
    public static void runWithStations(TrainConsist consist, List<StationStop> stations) {
        System.out.println("\n========================================");
        System.out.println("   STATION SCHEDULE — UC-11");
        System.out.println("========================================");

        List<StationStop> stops = readStops(consist);
        if (stops.isEmpty()) {
            System.out.println("  No stations entered. Skipping schedule.\n");
            return;
        }

        // Expose stations to caller for UC-13 analytics
        stations.addAll(stops);

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

    private static List<Bogie> readDetachedBogies(TrainConsist consist) {
        System.out.print("  Bogie IDs to detach (comma-separated, or ENTER for none): ");
        String input = scanner.nextLine().trim();
        List<Bogie> detached = new ArrayList<>();
        if (input.isEmpty()) return detached;

        Map<String, Bogie> bogieMap = new HashMap<>();
        for (Bogie b : consist.getBogies()) bogieMap.put(b.getBogieId(), b);

        for (String id : input.split(",")) {
            String trimmed = id.trim();
            Bogie b = bogieMap.get(trimmed);
            if (b != null) detached.add(b);
            else System.out.println("  WARNING: Bogie '" + trimmed + "' not found — skipped.");
        }

        return detached;
    }
}
