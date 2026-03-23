import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service implementing ad-hoc consist queries using Optional, Predicate composition,
 * and method references (UC-14).
 *
 * <p>Key design contracts:
 * <ul>
 *   <li>All single-result methods return {@link Optional} — never null.</li>
 *   <li>Predicates are named variables before composition — readable and testable.</li>
 *   <li>Method references replace lambdas wherever a lambda just delegates to one method.</li>
 *   <li>{@code flatMap} is used in the Optional chain in {@link #safeCargoUpdate}.</li>
 * </ul>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class ConsistQueryService {

    /**
     * Finds the bogie with the highest capacity matching the given predicate.
     *
     * <p>Pipeline:
     * <pre>
     *   bogies.stream()
     *         .filter(predicate)
     *         .max(Comparator.comparingInt(Bogie::getCapacity))
     * </pre>
     * Returns {@link Optional#empty()} if no bogies match — never returns null.</p>
     *
     * @param bogies    the bogie list to search
     * @param predicate the filter to apply before finding the maximum
     * @return QueryResult wrapping Optional<Bogie> — present if found, empty if none match
     */
    public static QueryResult<Bogie> findHeaviest(List<Bogie> bogies, Predicate<Bogie> predicate) {
        long startNs = System.nanoTime();

        Optional<Bogie> result = bogies.stream()
                .filter(predicate)
                // Method reference replaces b -> b.getCapacity()
                .max(Comparator.comparingInt(Bogie::getCapacity));

        long durationNs = System.nanoTime() - startNs;
        return new QueryResult<>(result, "Heaviest bogie matching predicate", durationNs);
    }

    /**
     * Checks whether any bogie in a distance window around the target violates
     * proximity safety rules (CYLINDRICAL within N positions of PASSENGER).
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Find target bogie position by linear scan.</li>
     *   <li>Clamp window to [max(0, pos-distance), min(size-1, pos+distance)].</li>
     *   <li>For each bogie in window (excluding target): check for CYLINDRICAL↔PASSENGER conflict.</li>
     *   <li>Return first violation as Optional, or empty if window is clear.</li>
     * </ol>
     *
     * <p>Returns {@link Optional#empty()} if bogie not found OR no violations in window.</p>
     *
     * @param bogies   the ordered bogie list
     * @param bogieId  the target bogie ID to check proximity around
     * @param distance the window radius (number of positions either side)
     * @return QueryResult wrapping Optional<ProximityViolation>
     */
    public static QueryResult<ProximityViolation> proximityCheck(
            List<Bogie> bogies, String bogieId, int distance) {

        long startNs = System.nanoTime();

        // Find target position by linear scan
        int targetPos = -1;
        for (int i = 0; i < bogies.size(); i++) {
            if (bogies.get(i).getBogieId().equals(bogieId)) {
                targetPos = i;
                break;
            }
        }

        if (targetPos == -1) {
            long durationNs = System.nanoTime() - startNs;
            return new QueryResult<>(Optional.empty(),
                    "Proximity check — bogie '" + bogieId + "' not found", durationNs);
        }

        Bogie target = bogies.get(targetPos);

        // Clamp window to valid indices — position 0 has no left neighbour
        int windowStart = Math.max(0, targetPos - distance);
        int windowEnd = Math.min(bogies.size() - 1, targetPos + distance);

        // Predicates — named before use
        Predicate<Bogie> isCylindrical = b ->
                b.getBogieType() == BogieType.GOODS
                && GoodsSubType.CYLINDRICAL.name().equals(b.getSubType());
        Predicate<Bogie> isPassenger = b -> b.getBogieType() == BogieType.PASSENGER;

        // Scan window for violations
        for (int i = windowStart; i <= windowEnd; i++) {
            if (i == targetPos) continue; // skip the target itself

            Bogie neighbour = bogies.get(i);

            // CYLINDRICAL target near PASSENGER neighbour
            if (isCylindrical.test(target) && isPassenger.test(neighbour)) {
                ProximityViolation violation = new ProximityViolation(
                        bogieId, neighbour.getBogieId(),
                        targetPos, i, distance,
                        "CYLINDRICAL bogie within " + distance + " positions of PASSENGER bogie");
                long durationNs = System.nanoTime() - startNs;
                return new QueryResult<>(Optional.of(violation),
                        "Proximity check around '" + bogieId + "'", durationNs);
            }

            // PASSENGER target near CYLINDRICAL neighbour
            if (isPassenger.test(target) && isCylindrical.test(neighbour)) {
                ProximityViolation violation = new ProximityViolation(
                        bogieId, neighbour.getBogieId(),
                        targetPos, i, distance,
                        "PASSENGER bogie within " + distance + " positions of CYLINDRICAL bogie");
                long durationNs = System.nanoTime() - startNs;
                return new QueryResult<>(Optional.of(violation),
                        "Proximity check around '" + bogieId + "'", durationNs);
            }
        }

        long durationNs = System.nanoTime() - startNs;
        return new QueryResult<>(Optional.empty(),
                "Proximity check around '" + bogieId + "' — clear", durationNs);
    }

    /**
     * Returns all bogies matching the compound predicate.
     *
     * <p>The predicate is typically built with {@link QueryBuilder} using
     * {@code Predicate.and()} to compose multiple criteria.</p>
     *
     * @param bogies    the bogie list to filter
     * @param predicate the composed compound predicate
     * @return list of matching bogies (empty if none match — no exception)
     */
    public static List<Bogie> findAll(List<Bogie> bogies, Predicate<Bogie> predicate) {
        return bogies.stream()
                .filter(predicate)          // composed predicate applied here
                .collect(Collectors.toList());
    }

    /**
     * Safely updates a bogie's cargo using Optional.ifPresent — no null check needed.
     *
     * <p>Uses {@code flatMap} in the Optional chain:
     * <pre>
     *   Optional.of(bogieId)
     *           .map(index::lookup)           // String → Bogie (may be null)
     *           .flatMap(Optional::ofNullable) // Bogie → Optional&lt;Bogie&gt; (flatten)
     *           .ifPresent(b -> { ... })       // update only if present
     * </pre>
     * If the bogie is not in the index, {@code ifPresent} body never runs — no NPE.</p>
     *
     * @param index  the BogieIndex for O(1) lookup
     * @param update the CargoUpdate with new weight and description
     * @return Optional<Bogie> — the updated bogie if found, empty if not
     */
    public static Optional<Bogie> safeCargoUpdate(BogieIndex index, CargoUpdate update) {
        // flatMap demonstrates Optional chaining — unwraps nested Optional
        return Optional.of(update.getBogieId())
                .map(index::lookup)                   // method reference: String → Bogie|null
                .flatMap(Optional::ofNullable)        // flatMap: wrap null in empty Optional
                .map(bogie -> {
                    // ifPresent-style: runs only if bogie is present
                    bogie.setCargoWeight(update.getNewCargoWeight());
                    bogie.setCargoDesc(update.getNewCargoDesc());
                    return bogie;
                });
    }
}
