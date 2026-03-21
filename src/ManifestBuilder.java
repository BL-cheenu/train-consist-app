import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Builds a {@link SafetyManifest} by replaying the {@link JourneyLog} (UC-08).
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Iterate all events in insertion order (preserved by ArrayList in JourneyLog).</li>
 *   <li>On ATTACHED or BRAKE_VAN_ADDED: insert the bogie ID into the LinkedHashSet.
 *       If the ID is already present (re-attach after detach), LinkedHashSet.add()
 *       does <em>not</em> update its position — we must remove first, then re-add
 *       so the re-attached bogie appears at the end.</li>
 *   <li>On DETACHED: remove the bogie ID from the set.
 *       Bogies not in the set are silently ignored.</li>
 *   <li>Other event types (SWAPPED, MOVED_TO_FRONT) do not affect manifest membership.</li>
 * </ol>
 *
 * <p>The snapshot is taken at the moment {@link #build} is called — mid-journey
 * changes after that point are not reflected.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ManifestBuilder {

    /**
     * Builds a SafetyManifest for the given train by reading the journey log.
     *
     * <p>Reads ATTACHED and DETACHED events to determine which bogies are currently
     * part of the consist and in what order they were first attached.</p>
     *
     * @param log     the JourneyLog containing the attachment history
     * @param consist the TrainConsist used to resolve bogie details at print time
     * @return a SafetyManifest snapshot taken at the current moment
     */
    public static SafetyManifest build(JourneyLog log, TrainConsist consist) {
        // LinkedHashSet: guarantees uniqueness + preserves insertion order
        LinkedHashSet<String> bogiesInOrder = new LinkedHashSet<>();

        List<JourneyEvent> events = log.getEvents();

        for (JourneyEvent event : events) {
            switch (event.getEventType()) {

                case ATTACHED:
                case BRAKE_VAN_ADDED:
                    // UC-08 key requirement: re-attached bogie appears at end (new insertion)
                    // LinkedHashSet.add() does NOT move an existing element, so we must
                    // remove first to ensure the re-attach is treated as a new insertion.
                    if (bogiesInOrder.contains(event.getBogieId())) {
                        bogiesInOrder.remove(event.getBogieId());
                    }
                    bogiesInOrder.add(event.getBogieId());
                    break;

                case DETACHED:
                    // Remove from manifest — detached bogies are no longer in the consist
                    bogiesInOrder.remove(event.getBogieId());
                    break;

                case SWAPPED:
                case MOVED_TO_FRONT:
                    // Position changes — manifest membership is unaffected
                    break;
            }
        }

        return new SafetyManifest(
                bogiesInOrder,
                log.getTrainNumber(),
                LocalDateTime.now(),
                consist);
    }
}
