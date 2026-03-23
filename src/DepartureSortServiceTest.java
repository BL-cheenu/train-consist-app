import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DepartureSortServiceTest {

    private List<Bogie> bogies;

    @BeforeEach
    void setUp() {
        bogies = new ArrayList<>();
        // 6 bogies with varying weights and types
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72, 45));
        bogies.add(new GoodsBogie("BG-02", GoodsSubType.RECTANGULAR, 100, 80));
        bogies.add(new PassengerBogie("BG-03", PassengerSubType.AC_CHAIR, 64, 30));
        bogies.add(new GoodsBogie("BG-04", GoodsSubType.CYLINDRICAL, 80, 65));
        bogies.add(new PassengerBogie("BG-05", PassengerSubType.FIRST_CLASS, 48, 55));
        bogies.add(new GoodsBogie("BG-06", GoodsSubType.RECTANGULAR, 90, 20));
    }

    // ── Bubble sort matches TimSort result ────────────────────────────────────

    @Test
    void testBubbleSortMatchesTimSortWeightOrder() {
        Comparator<Bogie> comp = DepartureSortService.weightComparator();

        DepartureSortResult bubbleResult = BubbleSortService.sort(bogies, comp);
        DepartureSortResult timsortResult = DepartureSortService.sort(bogies, comp);

        List<Bogie> bubble = bubbleResult.getSortedConsist();
        List<Bogie> timsort = timsortResult.getSortedConsist();

        assertEquals(bubble.size(), timsort.size());
        for (int i = 0; i < bubble.size(); i++) {
            assertEquals(bubble.get(i).getBogieId(), timsort.get(i).getBogieId());
        }
    }

    @Test
    void testWeightSortAscendingOrder() {
        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = DepartureSortService.sort(bogies, comp);
        List<Bogie> sorted = result.getSortedConsist();

        // Expected weight order: 20, 30, 45, 55, 65, 80
        assertEquals(20, sorted.get(0).getWeight());
        assertEquals(30, sorted.get(1).getWeight());
        assertEquals(45, sorted.get(2).getWeight());
        assertEquals(55, sorted.get(3).getWeight());
        assertEquals(65, sorted.get(4).getWeight());
        assertEquals(80, sorted.get(5).getWeight());
    }

    // ── Zero swap early exit ──────────────────────────────────────────────────

    @Test
    void testAlreadySortedProducesZeroSwaps() {
        // Create already weight-sorted list
        List<Bogie> sorted = new ArrayList<>();
        sorted.add(new GoodsBogie("BG-A", GoodsSubType.RECTANGULAR, 100, 10));
        sorted.add(new PassengerBogie("BG-B", PassengerSubType.SLEEPER, 60, 20));
        sorted.add(new GoodsBogie("BG-C", GoodsSubType.CYLINDRICAL, 80, 30));

        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = BubbleSortService.sort(sorted, comp);

        // Already sorted — bubble sort should exit after first pass with zero swaps
        assertEquals(0, result.getTelemetry().getSwaps());
        assertEquals(1, result.getTelemetry().getPasses()); // one pass to confirm sorted
    }

    @Test
    void testAllSameWeightZeroSwaps() {
        List<Bogie> sameWeight = new ArrayList<>();
        sameWeight.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 60, 50));
        sameWeight.add(new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64, 50));
        sameWeight.add(new PassengerBogie("BG-03", PassengerSubType.FIRST_CLASS, 48, 50));

        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = BubbleSortService.sort(sameWeight, comp);

        assertEquals(0, result.getTelemetry().getSwaps());
    }

    // ── Single-bogie consist ──────────────────────────────────────────────────

    @Test
    void testSingleBogieConsistBubbleSort() {
        List<Bogie> single = new ArrayList<>();
        single.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72, 45));

        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = BubbleSortService.sort(single, comp);

        assertEquals(1, result.getSortedConsist().size());
        assertEquals(0, result.getTelemetry().getPasses());
        assertEquals(0, result.getTelemetry().getSwaps());
    }

    // ── Multi-key sort correct ────────────────────────────────────────────────

    @Test
    void testMultiKeySortPassengerBeforeGoods() {
        Comparator<Bogie> comp = DepartureSortService.multiKeyComparator();
        DepartureSortResult result = DepartureSortService.sort(bogies, comp);
        List<Bogie> sorted = result.getSortedConsist();

        // First 3 should be PASSENGER, last 3 should be GOODS
        assertEquals(BogieType.PASSENGER, sorted.get(0).getBogieType());
        assertEquals(BogieType.PASSENGER, sorted.get(1).getBogieType());
        assertEquals(BogieType.PASSENGER, sorted.get(2).getBogieType());
        assertEquals(BogieType.GOODS, sorted.get(3).getBogieType());
        assertEquals(BogieType.GOODS, sorted.get(4).getBogieType());
        assertEquals(BogieType.GOODS, sorted.get(5).getBogieType());
    }

    @Test
    void testMultiKeySortPassengerGroupByWeightAscending() {
        Comparator<Bogie> comp = DepartureSortService.multiKeyComparator();
        DepartureSortResult result = DepartureSortService.sort(bogies, comp);
        List<Bogie> sorted = result.getSortedConsist();

        // PASSENGER group (indices 0-2): weights 30, 45, 55
        assertEquals(30, sorted.get(0).getWeight()); // BG-03
        assertEquals(45, sorted.get(1).getWeight()); // BG-01
        assertEquals(55, sorted.get(2).getWeight()); // BG-05
    }

    @Test
    void testMultiKeySortGoodsGroupByWeightAscending() {
        Comparator<Bogie> comp = DepartureSortService.multiKeyComparator();
        DepartureSortResult result = DepartureSortService.sort(bogies, comp);
        List<Bogie> sorted = result.getSortedConsist();

        // GOODS group (indices 3-5): weights 20, 65, 80
        assertEquals(20, sorted.get(3).getWeight()); // BG-06
        assertEquals(65, sorted.get(4).getWeight()); // BG-04
        assertEquals(80, sorted.get(5).getWeight()); // BG-02
    }

    // ── Stability: equal-weight bogies preserve original relative order ────────

    @Test
    void testBubbleSortIsStableForEqualWeights() {
        List<Bogie> equalWeight = new ArrayList<>();
        equalWeight.add(new PassengerBogie("BG-X", PassengerSubType.SLEEPER, 60, 50));
        equalWeight.add(new PassengerBogie("BG-Y", PassengerSubType.AC_CHAIR, 64, 50));
        equalWeight.add(new PassengerBogie("BG-Z", PassengerSubType.FIRST_CLASS, 48, 50));

        Comparator<Bogie> weightOnly = Comparator.comparingInt(Bogie::getWeight);
        DepartureSortResult result = BubbleSortService.sort(equalWeight, weightOnly);
        List<Bogie> sorted = result.getSortedConsist();

        // Stable sort: equal weights preserve original order BG-X, BG-Y, BG-Z
        assertEquals("BG-X", sorted.get(0).getBogieId());
        assertEquals("BG-Y", sorted.get(1).getBogieId());
        assertEquals("BG-Z", sorted.get(2).getBogieId());
    }

    @Test
    void testTimSortIsStableForEqualWeights() {
        List<Bogie> equalWeight = new ArrayList<>();
        equalWeight.add(new PassengerBogie("BG-X", PassengerSubType.SLEEPER, 60, 50));
        equalWeight.add(new PassengerBogie("BG-Y", PassengerSubType.AC_CHAIR, 64, 50));
        equalWeight.add(new PassengerBogie("BG-Z", PassengerSubType.FIRST_CLASS, 48, 50));

        Comparator<Bogie> weightOnly = Comparator.comparingInt(Bogie::getWeight);
        DepartureSortResult result = DepartureSortService.sort(equalWeight, weightOnly);
        List<Bogie> sorted = result.getSortedConsist();

        // Collections.sort is guaranteed stable
        assertEquals("BG-X", sorted.get(0).getBogieId());
        assertEquals("BG-Y", sorted.get(1).getBogieId());
        assertEquals("BG-Z", sorted.get(2).getBogieId());
    }

    // ── Original list not modified ─────────────────────────────────────────────

    @Test
    void testOriginalListUnmodifiedAfterBubbleSort() {
        String firstId = bogies.get(0).getBogieId();
        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        BubbleSortService.sort(bogies, comp);
        // Original list first element unchanged
        assertEquals(firstId, bogies.get(0).getBogieId());
    }

    @Test
    void testOriginalListUnmodifiedAfterTimSort() {
        String firstId = bogies.get(0).getBogieId();
        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortService.sort(bogies, comp);
        assertEquals(firstId, bogies.get(0).getBogieId());
    }

    // ── Telemetry fields ──────────────────────────────────────────────────────

    @Test
    void testBubbleSortTelemetryPassesAndSwapsTracked() {
        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = BubbleSortService.sort(bogies, comp);
        SortTelemetry t = result.getTelemetry();

        assertTrue(t.getPasses() > 0);
        assertTrue(t.getSwaps() >= 0);
        assertTrue(t.getDurationNs() >= 0);
        assertEquals("BubbleSort", t.getAlgorithm());
    }

    @Test
    void testTimSortTelemetryPassesAndSwapsAreMinusOne() {
        Comparator<Bogie> comp = DepartureSortService.weightComparator();
        DepartureSortResult result = DepartureSortService.sort(bogies, comp);
        SortTelemetry t = result.getTelemetry();

        assertEquals(-1, t.getPasses());
        assertEquals(-1, t.getSwaps());
        assertTrue(t.getDurationNs() >= 0);
    }
}
