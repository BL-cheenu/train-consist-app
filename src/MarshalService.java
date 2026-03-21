import java.util.ArrayList;
import java.util.List;

/**
 * Service that merges two train consists into one with atomic conflict detection (UC-07).
 *
 * <p><b>Atomicity guarantee:</b> Conflicts are collected in a first pass before any bogie
 * is added to consist A or the fleet registry. Only if the conflict list is empty does the
 * service commit — either all bogies from B are merged, or none are. This prevents partial
 * merges that would leave the fleet registry in an inconsistent state.</p>
 *
 * <p><b>O(1) conflict check:</b> {@link FleetRegistry#contains} uses a HashSet internally,
 * giving O(1) average-case lookup per bogie. The full scan over consist B is O(n).</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class MarshalService {

    /**
     * Attempts to merge all bogies from consist B into consist A.
     *
     * <p><b>Algorithm (two-phase commit):</b>
     * <ol>
     *   <li><b>Check phase:</b> Scan every bogie in B against the {@link FleetRegistry}.
     *       Collect all conflicting IDs. Do NOT modify consist A or the registry.</li>
     *   <li><b>Commit phase:</b> Only if no conflicts were found — add all B bogies to
     *       the end of A's list and register each new ID in the fleet registry.</li>
     *   <li><b>Rollback:</b> If any conflict exists — return failure immediately without
     *       touching consist A or the registry.</li>
     * </ol>
     *
     * @param consistA the target consist — bogies from B will be appended here on success
     * @param consistB the source consist — its bogies are checked for duplicate IDs
     * @param registry the singleton fleet registry used for O(1) duplicate detection
     * @return MarshalResult with success flag, conflict list, and merged consist (or null)
     */
    public static MarshalResult merge(TrainConsist consistA, TrainConsist consistB,
                                      FleetRegistry registry) {

        List<String> conflicts = new ArrayList<>();

        // ── Phase 1: Check — scan all B bogies for ID conflicts (read-only) ───
        for (Bogie b : consistB.getBogieList()) {
            if (registry.contains(b.getBogieId())) {
                // Collect every conflict — never abort on the first one
                conflicts.add(b.getBogieId());
            }
        }

        // ── Rollback: one or more conflicts — reject the entire merge ─────────
        if (!conflicts.isEmpty()) {
            return new MarshalResult(false, conflicts, null);
        }

        // ── Phase 2: Commit — add all B bogies to A and register each ID ─────
        for (Bogie b : consistB.getBogieList()) {
            consistA.getBogieList().add(b);   // append to end of consist A
            registry.register(b.getBogieId()); // register in fleet — no duplicates possible now
        }

        return new MarshalResult(true, conflicts, consistA);
    }

    /**
     * Registers all bogies in a consist with the fleet registry.
     * Called during initial consist creation to seed the registry.
     *
     * <p>If a bogie ID is already registered (e.g. from a previous session),
     * {@link FleetRegistry#register} silently ignores it — this method does not
     * check for conflicts. Use only for trusted initial data.</p>
     *
     * @param consist  the consist whose bogies should be registered
     * @param registry the fleet registry to register into
     */
    public static void registerAll(TrainConsist consist, FleetRegistry registry) {
        for (Bogie b : consist.getBogieList()) {
            registry.register(b.getBogieId());
        }
    }
}
