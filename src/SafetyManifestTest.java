import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SafetyManifestTest {

    private JourneyLog log;
    private TrainConsist consist;
    private List<Bogie> bogies;

    @BeforeEach
    void setUp() {
        log = new JourneyLog("TN-2201");
        bogies = new ArrayList<>();
        consist = new TrainConsist("TN-2201", "Chennai → CBE", new Bogie[0]);
    }

    // Helper: attach a bogie to both the live list and log
    private void attach(String id, BogieType type) {
        Bogie b = type == BogieType.PASSENGER
                ? new PassengerBogie(id, PassengerSubType.SLEEPER, 60)
                : new GoodsBogie(id, GoodsSubType.RECTANGULAR, 100);
        bogies.add(b);
        consist = new TrainConsist("TN-2201", "Chennai → CBE", bogies.toArray(new Bogie[0]));
        ConsistOperationsService.attach(
                bogies, new AttachRequest(b, -1), log, "YARD_OP");
    }

    private void detach(String id) {
        bogies.removeIf(b -> b.getBogieId().equals(id));
        consist = new TrainConsist("TN-2201", "Chennai → CBE", bogies.toArray(new Bogie[0]));
        ConsistOperationsService.detach(bogies, new DetachRequest(id), log, "YARD_OP");
    }

    // ── Insertion order preserved ─────────────────────────────────────────────

    @Test
    void testInsertionOrderPreserved() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);
        attach("BG-03", BogieType.PASSENGER);

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        Iterator<String> it = manifest.getBogiesInOrder().iterator();
        assertEquals("BG-01", it.next());
        assertEquals("BG-02", it.next());
        assertEquals("BG-03", it.next());
        assertFalse(it.hasNext());
    }

    // ── Duplicate bogie rejected (no double entry) ────────────────────────────

    @Test
    void testDuplicateAttachIsRejected() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);

        // Manually record a second ATTACHED event for BG-01 without detaching
        // (simulates a data-error duplicate)
        log.record(new JourneyEvent(
                EventType.ATTACHED, "BG-01", 0, "YARD_OP", LocalDateTime.now()));

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        // BG-01 should appear only once — LinkedHashSet rejects duplicates
        assertEquals(2, manifest.size());

        // BG-01 should now be at the END (re-inserted after remove+add)
        Iterator<String> it = manifest.getBogiesInOrder().iterator();
        assertEquals("BG-02", it.next()); // BG-02 stays in its original position
        assertEquals("BG-01", it.next()); // BG-01 re-inserted at end
    }

    // ── Detached bogie removed from manifest ─────────────────────────────────

    @Test
    void testDetachedBogieRemovedFromManifest() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);
        attach("BG-03", BogieType.PASSENGER);
        detach("BG-02");

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        assertEquals(2, manifest.size());
        assertFalse(manifest.getBogiesInOrder().contains("BG-02"));
        assertTrue(manifest.getBogiesInOrder().contains("BG-01"));
        assertTrue(manifest.getBogiesInOrder().contains("BG-03"));
    }

    // ── Re-attached bogie appears at end ─────────────────────────────────────

    @Test
    void testReattachedBogieAppearsAtEnd() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);
        attach("BG-03", BogieType.PASSENGER);

        // Detach BG-01 and re-attach it
        detach("BG-01");
        attach("BG-01", BogieType.PASSENGER);

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        assertEquals(3, manifest.size());

        // BG-01 should now be at the END — new insertion after detach
        Iterator<String> it = manifest.getBogiesInOrder().iterator();
        assertEquals("BG-02", it.next());
        assertEquals("BG-03", it.next());
        assertEquals("BG-01", it.next()); // re-attached — appears last
    }

    // ── Empty log produces empty manifest ────────────────────────────────────

    @Test
    void testEmptyLogProducesEmptyManifest() {
        SafetyManifest manifest = ManifestBuilder.build(log, consist);
        assertTrue(manifest.isEmpty());
        assertEquals(0, manifest.size());
    }

    // ── All detached produces empty manifest ─────────────────────────────────

    @Test
    void testAllDetachedProducesEmptyManifest() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);
        detach("BG-01");
        detach("BG-02");

        SafetyManifest manifest = ManifestBuilder.build(log, consist);
        assertTrue(manifest.isEmpty());
    }

    // ── SWAPPED and MOVED_TO_FRONT don't affect manifest membership ───────────

    @Test
    void testSwapDoesNotAffectManifestMembership() {
        attach("BG-01", BogieType.PASSENGER);
        attach("BG-02", BogieType.GOODS);

        // Record a SWAPPED event
        log.record(new JourneyEvent(
                EventType.SWAPPED, "BG-01", 0, "SHUNTER", LocalDateTime.now()));

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        // Both bogies still in manifest — SWAP only changes position, not membership
        assertEquals(2, manifest.size());
        assertTrue(manifest.getBogiesInOrder().contains("BG-01"));
        assertTrue(manifest.getBogiesInOrder().contains("BG-02"));
    }

    // ── BRAKE_VAN_ADDED is treated as attachment ──────────────────────────────

    @Test
    void testBrakeVanAddedAppearsInManifest() {
        attach("BG-01", BogieType.PASSENGER);

        Bogie brakeVan = new GoodsBogie("BV-01", GoodsSubType.RECTANGULAR, 10);
        bogies.add(brakeVan);
        consist = new TrainConsist("TN-2201", "Chennai → CBE", bogies.toArray(new Bogie[0]));
        ShuntingService.addBrakeVan(bogies, brakeVan, log, "SHUNTER");

        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        assertEquals(2, manifest.size());
        assertTrue(manifest.getBogiesInOrder().contains("BV-01"));

        // BG-01 first, BV-01 second (brake van added after)
        Iterator<String> it = manifest.getBogiesInOrder().iterator();
        assertEquals("BG-01", it.next());
        assertEquals("BV-01", it.next());
    }

    // ── LinkedHashSet used (not HashSet or ArrayList) ─────────────────────────

    @Test
    void testManifestUsesLinkedHashSet() {
        attach("BG-01", BogieType.PASSENGER);
        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        // Verify the runtime type is LinkedHashSet
        assertInstanceOf(LinkedHashSet.class, manifest.getBogiesInOrder());
    }

    // ── Manifest metadata ────────────────────────────────────────────────────

    @Test
    void testManifestTrainNumberAndTimestamp() {
        SafetyManifest manifest = ManifestBuilder.build(log, consist);

        assertEquals("TN-2201", manifest.getTrainNumber());
        assertNotNull(manifest.getGeneratedAt());
        // Timestamp should be very recent (within 5 seconds of now)
        assertTrue(manifest.getGeneratedAt().isBefore(LocalDateTime.now().plusSeconds(5)));
    }
}
