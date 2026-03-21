import java.time.LocalDateTime;
import java.util.List;

/**
 * Prints the journey event log and replay results to the console (UC-06).
 *
 * <p>Two display modes:
 * <ul>
 *   <li>{@link #printLog} — prints all events in the log in insertion order.</li>
 *   <li>{@link #printReplay} — prints the reconstructed consist state at a past timestamp.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class JourneyLogPrinter {

    /**
     * Prints all events in the journey log in insertion order.
     * If the log is empty, prints a descriptive message instead.
     *
     * @param log the JourneyLog to display
     */
    public static void printLog(JourneyLog log) {
        System.out.println("\n========================================");
        System.out.println("     JOURNEY EVENT LOG — " + log.getTrainNumber());
        System.out.println("========================================");

        if (log.isEmpty()) {
            // UC-06 edge flow: no events recorded yet
            System.out.println("  No events recorded for this consist.");
        } else {
            List<JourneyEvent> events = log.getEvents();
            System.out.println("  Total events: " + events.size());
            System.out.println("----------------------------------------");
            for (int i = 0; i < events.size(); i++) {
                // Print 1-based index alongside each event for readability
                System.out.printf("  #%-3d %s%n", (i + 1), events.get(i));
            }
        }

        System.out.println("========================================\n");
    }

    /**
     * Prints the reconstructed consist state at the given cutoff timestamp.
     * The state is derived by replaying all events up to and including that moment.
     *
     * @param log   the JourneyLog to replay
     * @param upTo  the cutoff timestamp for the replay
     */
    public static void printReplay(JourneyLog log, LocalDateTime upTo) {
        System.out.println("\n========================================");
        System.out.println("     CONSIST STATE AT: " + upTo);
        System.out.println("========================================");

        List<Bogie> reconstructed = log.replayUpTo(upTo);

        if (reconstructed.isEmpty()) {
            // Either no events before the cutoff, or all bogies were detached by then
            System.out.println("  Consist was empty at this point in time.");
        } else {
            System.out.println("  Bogies in consist (" + reconstructed.size() + "):");
            System.out.println("----------------------------------------");
            for (int i = 0; i < reconstructed.size(); i++) {
                System.out.printf("  [%d] %s%n", i, reconstructed.get(i).getBogieId());
            }
        }

        System.out.println("========================================\n");
    }
}
