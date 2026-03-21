import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MarshalServiceTest {

    private FleetRegistry registry;
    private TrainConsist consistA;
    private TrainConsist consistB;

    @BeforeEach
    void setUp() {
        // Reset singleton before each test — prevents state leaking between tests
        FleetRegistry.resetForTesting();
        registry = FleetRegistry.getInstance();

        // Build consist A with 3 bogies — pre-register them in the fleet
        Bogie[] bogiesA = {
            new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72),
            new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64),
            new GoodsBogie("BG-03", GoodsSubType.RECTANGULAR, 100)
        };
        consistA = new TrainConsist("TN-2201", "Chennai → Coimbatore", bogiesA);
        MarshalService.registerAll(consistA, registry);

        // Build consist B with 2 bogies — no conflicts initially
        Bogie[] bogiesB = {
            new PassengerBogie("BG-04", PassengerSubType.FIRST_CLASS, 48),
            new GoodsBogie("BG-05", GoodsSubType.CYLINDRICAL, 80)
        };
        consistB = new TrainConsist("TN-3301", "Madurai → Chennai", bogiesB);
    }

    // ── Merge with no conflicts ───────────────────────────────────────────────

    @Test
    void testMergeNoConflictsSucceeds() {
        MarshalResult result = MarshalService.merge(consistA, consistB, registry);

        assertTrue(result.isSuccess());
        assertTrue(result.getConflicts().isEmpty());
        assertNotNull(result.getMergedConsist());
    }

    @Test
    void testMergeNoConflictsAddsAllBogiesToA() {
        MarshalService.merge(consistA, consistB, registry);

        // Consist A should now have 3 + 2 = 5 bogies
        assertEquals(5, consistA.getBogieList().size());
    }

    @Test
    void testMergeNoConflictsRegistersNewIds() {
        MarshalService.merge(consistA, consistB, registry);

        // All B bogies should now be in the fleet registry
        assertTrue(registry.contains("BG-04"));
        assertTrue(registry.contains("BG-05"));
    }

    @Test
    void testMergePreservesOrderBogiesAppendedToRear() {
        MarshalService.merge(consistA, consistB, registry);

        List<Bogie> merged = consistA.getBogieList();
        assertEquals("BG-01", merged.get(0).getBogieId());
        assertEquals("BG-02", merged.get(1).getBogieId());
        assertEquals("BG-03", merged.get(2).getBogieId());
        assertEquals("BG-04", merged.get(3).getBogieId()); // B bogies appended at rear
        assertEquals("BG-05", merged.get(4).getBogieId());
    }

    // ── Merge with single conflict ────────────────────────────────────────────

    @Test
    void testSingleDuplicateDetected() {
        // Add a duplicate ID to consist B
        Bogie[] bogiesB_conflict = {
            new PassengerBogie("BG-02", PassengerSubType.SLEEPER, 60), // BG-02 already in A
            new GoodsBogie("BG-06", GoodsSubType.RECTANGULAR, 90)
        };
        TrainConsist consistBConflict = new TrainConsist("TN-4401", "Salem → Chennai", bogiesB_conflict);

        MarshalResult result = MarshalService.merge(consistA, consistBConflict, registry);

        assertFalse(result.isSuccess());
        assertEquals(1, result.getConflicts().size());
        assertTrue(result.getConflicts().contains("BG-02"));
    }

    // ── Merge with multiple conflicts ─────────────────────────────────────────

    @Test
    void testMergeWithConflictsAbortsEntireMerge() {
        // Both bogies in B conflict with A
        Bogie[] bogiesB_conflicts = {
            new PassengerBogie("BG-01", PassengerSubType.AC_CHAIR, 60),
            new PassengerBogie("BG-03", PassengerSubType.SLEEPER, 72)
        };
        TrainConsist consistBConflicts = new TrainConsist("TN-5501", "Test Route", bogiesB_conflicts);

        MarshalResult result = MarshalService.merge(consistA, consistBConflicts, registry);

        assertFalse(result.isSuccess());
        assertEquals(2, result.getConflicts().size());
        assertTrue(result.getConflicts().contains("BG-01"));
        assertTrue(result.getConflicts().contains("BG-03"));
    }

    @Test
    void testMergeAbortedDoesNotModifyConsistA() {
        Bogie[] bogiesB_conflict = {
            new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72) // conflict
        };
        TrainConsist consistBConflict = new TrainConsist("TN-6601", "Test", bogiesB_conflict);

        // Consist A has 3 bogies before attempted merge
        int sizeBefore = consistA.getBogieList().size();

        MarshalService.merge(consistA, consistBConflict, registry);

        // Consist A must be unchanged — atomic rollback
        assertEquals(sizeBefore, consistA.getBogieList().size());
    }

    @Test
    void testMergeAbortedDoesNotUpdateRegistry() {
        Bogie[] bogiesB_mixed = {
            new PassengerBogie("BG-07", PassengerSubType.SLEEPER, 60), // no conflict
            new PassengerBogie("BG-01", PassengerSubType.AC_CHAIR, 48)  // conflict
        };
        TrainConsist consistBMixed = new TrainConsist("TN-7701", "Test", bogiesB_mixed);

        MarshalService.merge(consistA, consistBMixed, registry);

        // BG-07 should NOT be registered — the entire merge was rolled back
        assertFalse(registry.contains("BG-07"));
    }

    // ── Full duplicate reject ─────────────────────────────────────────────────

    @Test
    void testFullDuplicateRejectAllConflictsReported() {
        // Every bogie in B is already in A
        Bogie[] bogiesB_all_dup = {
            new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72),
            new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64),
            new GoodsBogie("BG-03", GoodsSubType.RECTANGULAR, 100)
        };
        TrainConsist consistBAllDup = new TrainConsist("TN-8801", "All Dup", bogiesB_all_dup);

        MarshalResult result = MarshalService.merge(consistA, consistBAllDup, registry);

        assertFalse(result.isSuccess());
        assertEquals(3, result.getConflicts().size());
        assertNull(result.getMergedConsist());
    }

    // ── FleetRegistry standalone tests ───────────────────────────────────────

    @Test
    void testRegistryContainsAfterRegister() {
        registry.register("BG-99");
        assertTrue(registry.contains("BG-99"));
    }

    @Test
    void testRegistryNotContainsUnregisteredId() {
        assertFalse(registry.contains("BG-UNKNOWN"));
    }

    @Test
    void testRegistryDeregister() {
        registry.register("BG-99");
        boolean removed = registry.deregister("BG-99");
        assertTrue(removed);
        assertFalse(registry.contains("BG-99"));
    }

    @Test
    void testRegistryReRegister() {
        registry.register("BG-OLD");
        boolean result = registry.reRegister("BG-OLD", "BG-NEW");
        assertTrue(result);
        assertFalse(registry.contains("BG-OLD"));
        assertTrue(registry.contains("BG-NEW"));
    }

    @Test
    void testRegistrySizeAfterMerge() {
        int sizeBefore = registry.size(); // 3 from consistA
        MarshalService.merge(consistA, consistB, registry);
        assertEquals(sizeBefore + 2, registry.size()); // +2 from consistB
    }

    @Test
    void testSingletonReturnsSameInstance() {
        FleetRegistry r1 = FleetRegistry.getInstance();
        FleetRegistry r2 = FleetRegistry.getInstance();
        assertSame(r1, r2);
    }
}
