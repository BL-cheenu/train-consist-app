import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;

public class LoadPlanServiceTest {

    private List<Bogie> bogies;

    @BeforeEach
    void setUp() {
        bogies = new ArrayList<>();
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72));
        bogies.add(new GoodsBogie("BG-02", GoodsSubType.RECTANGULAR, 100));
        bogies.add(new PassengerBogie("BG-03", PassengerSubType.AC_CHAIR, 48));
        bogies.add(new GoodsBogie("BG-04", GoodsSubType.CYLINDRICAL, 80));
        bogies.add(new PassengerBogie("BG-05", PassengerSubType.FIRST_CLASS, 30));
    }

    // ── Natural order: capacity ascending ────────────────────────────────────

    @Test
    void testNaturalOrderCapacityAscending() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        Iterator<Bogie> it = plan.getSortedBogies().iterator();

        // Expected order: 30, 48, 72, 80, 100
        assertEquals(30, it.next().getCapacity());
        assertEquals(48, it.next().getCapacity());
        assertEquals(72, it.next().getCapacity());
        assertEquals(80, it.next().getCapacity());
        assertEquals(100, it.next().getCapacity());
        assertFalse(it.hasNext());
    }

    @Test
    void testNaturalOrderContainsAllBogies() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        assertEquals(5, plan.getSortedBogies().size());
    }

    // ── Custom order: GOODS first, then capacity, then bogieId ───────────────

    @Test
    void testCustomOrderGoodsBeforePassenger() {
        LoadPlan plan = LoadPlanService.buildCustomPlan(bogies);
        Iterator<Bogie> it = plan.getSortedBogies().iterator();

        // First two should be GOODS bogies (BG-02 capacity=100, BG-04 capacity=80)
        // GOODS sorted by capacity ascending: BG-04(80) then BG-02(100)
        Bogie first = it.next();
        Bogie second = it.next();
        assertEquals(BogieType.GOODS, first.getBogieType());
        assertEquals(BogieType.GOODS, second.getBogieType());
        assertEquals(80, first.getCapacity());
        assertEquals(100, second.getCapacity());
    }

    @Test
    void testCustomOrderPassengerAfterGoods() {
        LoadPlan plan = LoadPlanService.buildCustomPlan(bogies);
        List<Bogie> sorted = new ArrayList<>(plan.getSortedBogies());

        // Last three should be PASSENGER bogies
        assertEquals(BogieType.PASSENGER, sorted.get(2).getBogieType());
        assertEquals(BogieType.PASSENGER, sorted.get(3).getBogieType());
        assertEquals(BogieType.PASSENGER, sorted.get(4).getBogieType());
    }

    @Test
    void testCustomOrderPassengerGroupCapacityAscending() {
        LoadPlan plan = LoadPlanService.buildCustomPlan(bogies);
        List<Bogie> sorted = new ArrayList<>(plan.getSortedBogies());

        // PASSENGER group: BG-05(30), BG-03(48), BG-01(72)
        assertEquals(30, sorted.get(2).getCapacity());
        assertEquals(48, sorted.get(3).getCapacity());
        assertEquals(72, sorted.get(4).getCapacity());
    }

    // ── Tiebreaker: equal capacity bogies both preserved ─────────────────────

    @Test
    void testTiebreakerPreservesBothBogiesWithEqualCapacity() {
        // Add two passenger bogies with same capacity — tiebreaker must prevent deduplication
        bogies.clear();
        bogies.add(new PassengerBogie("BG-A", PassengerSubType.SLEEPER, 60));
        bogies.add(new PassengerBogie("BG-B", PassengerSubType.AC_CHAIR, 60));
        bogies.add(new PassengerBogie("BG-C", PassengerSubType.FIRST_CLASS, 60));

        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);

        // All 3 bogies must be present — tiebreaker on bogieId prevents deduplication
        assertEquals(3, plan.getSortedBogies().size());
    }

    @Test
    void testTiebreakerOrdersByBogieIdOnEqualCapacity() {
        bogies.clear();
        bogies.add(new PassengerBogie("BG-C", PassengerSubType.SLEEPER, 60));
        bogies.add(new PassengerBogie("BG-A", PassengerSubType.AC_CHAIR, 60));
        bogies.add(new PassengerBogie("BG-B", PassengerSubType.FIRST_CLASS, 60));

        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        Iterator<Bogie> it = plan.getSortedBogies().iterator();

        // Same capacity — ordered alphabetically by bogieId
        assertEquals("BG-A", it.next().getBogieId());
        assertEquals("BG-B", it.next().getBogieId());
        assertEquals("BG-C", it.next().getBogieId());
    }

    // ── tailSet heavy-haul check ──────────────────────────────────────────────

    @Test
    void testHeavyHaulAboveThreshold() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        SortedSet<Bogie> heavyHaul = plan.getHeavyHaulBogies();

        // Bogies with capacity > 60: BG-01(72), BG-04(80), BG-02(100)
        assertEquals(3, heavyHaul.size());
    }

    @Test
    void testHeavyHaulContainsCorrectBogies() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        SortedSet<Bogie> heavyHaul = plan.getHeavyHaulBogies();

        List<String> ids = new ArrayList<>();
        for (Bogie b : heavyHaul) ids.add(b.getBogieId());

        assertTrue(ids.contains("BG-01")); // capacity 72
        assertTrue(ids.contains("BG-04")); // capacity 80
        assertTrue(ids.contains("BG-02")); // capacity 100
        assertFalse(ids.contains("BG-03")); // capacity 48 — below threshold
        assertFalse(ids.contains("BG-05")); // capacity 30 — below threshold
    }

    @Test
    void testHeavyHaulThresholdExceedsAllCapacitiesReturnsEmpty() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        SortedSet<Bogie> heavyHaul = LoadPlanService.heavyHaulCheck(
                plan.getSortedBogies(), 999);

        assertTrue(heavyHaul.isEmpty());
    }

    @Test
    void testHeavyHaulThresholdBelowAllCapacitiesReturnsAll() {
        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);
        SortedSet<Bogie> heavyHaul = LoadPlanService.heavyHaulCheck(
                plan.getSortedBogies(), 0);

        assertEquals(5, heavyHaul.size());
    }

    // ── Single bogie consist ──────────────────────────────────────────────────

    @Test
    void testSingleBogieConsist() {
        bogies.clear();
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72));

        LoadPlan plan = LoadPlanService.buildNaturalPlan(bogies);

        assertEquals(1, plan.getSortedBogies().size());
        assertEquals("BG-01", plan.getSortedBogies().first().getBogieId());
    }

    // ── Bogie.compareTo consistent with equals ────────────────────────────────

    @Test
    void testCompareToReturnZeroOnlyForSameBogieId() {
        Bogie a = new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72);
        Bogie b = new PassengerBogie("BG-01", PassengerSubType.AC_CHAIR, 72);
        Bogie c = new PassengerBogie("BG-02", PassengerSubType.SLEEPER, 72);

        // Same bogieId — compareTo should return 0
        assertEquals(0, a.compareTo(b));

        // Different bogieId, same capacity — compareTo should NOT return 0
        assertNotEquals(0, a.compareTo(c));
    }

    // ── BogieTypeCapacityComparator is a named class ──────────────────────────

    @Test
    void testComparatorIsNamedClass() {
        BogieTypeCapacityComparator comparator = new BogieTypeCapacityComparator();
        assertNotNull(comparator);
        assertInstanceOf(BogieTypeCapacityComparator.class, comparator);
    }

    @Test
    void testComparatorGoodsBeforePassengerSameCapacity() {
        BogieTypeCapacityComparator comparator = new BogieTypeCapacityComparator();
        Bogie goods = new GoodsBogie("BG-G", GoodsSubType.RECTANGULAR, 60);
        Bogie passenger = new PassengerBogie("BG-P", PassengerSubType.SLEEPER, 60);

        // GOODS should come before PASSENGER
        assertTrue(comparator.compare(goods, passenger) < 0);
        assertTrue(comparator.compare(passenger, goods) > 0);
    }
}
