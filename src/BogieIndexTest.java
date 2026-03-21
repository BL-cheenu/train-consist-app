import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BogieIndexTest {

    private List<Bogie> bogies;
    private BogieIndex index;

    @BeforeEach
    void setUp() {
        bogies = new ArrayList<>();
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72));
        bogies.add(new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64));
        bogies.add(new GoodsBogie("BG-03", GoodsSubType.RECTANGULAR, 100));
        bogies.add(new GoodsBogie("BG-04", GoodsSubType.CYLINDRICAL, 80));
        bogies.add(new PassengerBogie("BG-05", PassengerSubType.FIRST_CLASS, 48));

        // Build index once — not rebuilt per lookup
        index = BogieIndex.build(bogies);
    }

    // ── Lookup hit ────────────────────────────────────────────────────────────

    @Test
    void testLookupHitReturnsCorrectBogie() {
        Bogie found = index.lookup("BG-03");
        assertNotNull(found);
        assertEquals("BG-03", found.getBogieId());
        assertEquals(BogieType.GOODS, found.getBogieType());
    }

    @Test
    void testLookupHitFirstBogie() {
        Bogie found = index.lookup("BG-01");
        assertNotNull(found);
        assertEquals("BG-01", found.getBogieId());
    }

    @Test
    void testLookupHitLastBogie() {
        Bogie found = index.lookup("BG-05");
        assertNotNull(found);
        assertEquals("BG-05", found.getBogieId());
    }

    // ── Lookup miss ───────────────────────────────────────────────────────────

    @Test
    void testLookupMissReturnsNull() {
        Bogie found = index.lookup("BG-99");
        assertNull(found);
    }

    @Test
    void testLookupMissOnEmptyId() {
        Bogie found = index.lookup("");
        assertNull(found);
    }

    @Test
    void testContainsReturnsFalseForMissingId() {
        assertFalse(index.contains("BG-UNKNOWN"));
    }

    @Test
    void testContainsReturnsTrueForIndexedId() {
        assertTrue(index.contains("BG-02"));
    }

    // ── Update reflected in List (reference semantics) ────────────────────────

    @Test
    void testUpdateReflectedInList() {
        CargoUpdate update = new CargoUpdate("BG-03", 75.5, "Coal");
        boolean updated = index.update(update);

        assertTrue(updated);

        // Find the same bogie in the original List — should show updated cargo
        Bogie inList = null;
        for (Bogie b : bogies) {
            if (b.getBogieId().equals("BG-03")) { inList = b; break; }
        }

        assertNotNull(inList);
        // Reference semantics: update via index is reflected in List automatically
        assertEquals(75.5, inList.getCargoWeight(), 0.001);
        assertEquals("Coal", inList.getCargoDesc());
    }

    @Test
    void testUpdateReturnsFalseForMissingId() {
        CargoUpdate update = new CargoUpdate("BG-99", 50.0, "Steel");
        boolean updated = index.update(update);
        assertFalse(updated);
    }

    @Test
    void testUpdateZeroCargoWeightAllowed() {
        CargoUpdate update = new CargoUpdate("BG-01", 0.0, "Empty");
        boolean updated = index.update(update);
        assertTrue(updated);
        assertEquals(0.0, index.lookup("BG-01").getCargoWeight(), 0.001);
    }

    @Test
    void testMultipleUpdatesOnSameBogie() {
        index.update(new CargoUpdate("BG-02", 30.0, "Grain"));
        index.update(new CargoUpdate("BG-02", 45.0, "Rice"));

        Bogie bogie = index.lookup("BG-02");
        assertEquals(45.0, bogie.getCargoWeight(), 0.001);
        assertEquals("Rice", bogie.getCargoDesc());
    }

    // ── Index size ────────────────────────────────────────────────────────────

    @Test
    void testIndexSizeMatchesList() {
        assertEquals(bogies.size(), index.size());
    }

    // ── groupByType — computeIfAbsent pattern ─────────────────────────────────

    @Test
    void testGroupCountsCorrect() {
        FleetGrouping grouping = FleetDashboardService.groupByType(bogies);

        // 3 PASSENGER bogies (BG-01, BG-02, BG-05), 2 GOODS bogies (BG-03, BG-04)
        assertEquals(3, grouping.countByType(BogieType.PASSENGER));
        assertEquals(2, grouping.countByType(BogieType.GOODS));
    }

    @Test
    void testGroupTotalCapacityCorrect() {
        FleetGrouping grouping = FleetDashboardService.groupByType(bogies);

        // PASSENGER: 72 + 64 + 48 = 184
        assertEquals(184, grouping.totalCapacityByType(BogieType.PASSENGER));

        // GOODS: 100 + 80 = 180
        assertEquals(180, grouping.totalCapacityByType(BogieType.GOODS));
    }

    @Test
    void testComputeIfAbsentOnMissingType() {
        // All bogies are PASSENGER — GOODS group should return empty list, not NPE
        List<Bogie> passengerOnly = new ArrayList<>();
        passengerOnly.add(new PassengerBogie("BG-A", PassengerSubType.SLEEPER, 60));
        passengerOnly.add(new PassengerBogie("BG-B", PassengerSubType.AC_CHAIR, 48));

        FleetGrouping grouping = FleetDashboardService.groupByType(passengerOnly);

        // GOODS type has no bogies — getGroup should return empty list, not throw NPE
        assertNotNull(grouping.getGroup(BogieType.GOODS));
        assertEquals(0, grouping.countByType(BogieType.GOODS));
        assertEquals(0, grouping.totalCapacityByType(BogieType.GOODS));
    }

    @Test
    void testGroupContainsCorrectBogieIds() {
        FleetGrouping grouping = FleetDashboardService.groupByType(bogies);

        List<String> goodsIds = new ArrayList<>();
        for (Bogie b : grouping.getGroup(BogieType.GOODS)) {
            goodsIds.add(b.getBogieId());
        }

        assertTrue(goodsIds.contains("BG-03"));
        assertTrue(goodsIds.contains("BG-04"));
        assertFalse(goodsIds.contains("BG-01")); // PASSENGER — should not be in GOODS group
    }

    @Test
    void testEmptyListProducesEmptyGrouping() {
        FleetGrouping grouping = FleetDashboardService.groupByType(new ArrayList<>());
        assertTrue(grouping.getGroups().isEmpty());
        assertEquals(0, grouping.countByType(BogieType.PASSENGER));
        assertEquals(0, grouping.countByType(BogieType.GOODS));
    }

    // ── Bogie cargo fields default to zero and empty ──────────────────────────

    @Test
    void testCargoFieldsDefaultToZeroAndEmpty() {
        Bogie bogie = new PassengerBogie("BG-NEW", PassengerSubType.SLEEPER, 60);
        assertEquals(0.0, bogie.getCargoWeight(), 0.001);
        assertEquals("", bogie.getCargoDesc());
    }
}
