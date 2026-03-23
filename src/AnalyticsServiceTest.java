import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    private List<Bogie> bogies;
    private List<StationStop> stations;

    @BeforeEach
    void setUp() {
        bogies = new ArrayList<>();
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72));
        bogies.add(new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64));
        bogies.add(new PassengerBogie("BG-03", PassengerSubType.FIRST_CLASS, 48));
        bogies.add(new GoodsBogie("BG-04", GoodsSubType.RECTANGULAR, 100));
        bogies.add(new GoodsBogie("BG-05", GoodsSubType.CYLINDRICAL, 80));
        bogies.add(new GoodsBogie("BG-06", GoodsSubType.RECTANGULAR, 60));

        // Build station stops for topFreightStation test
        stations = new ArrayList<>();
        List<Bogie> s1 = List.of(bogies.get(3), bogies.get(4)); // 100 + 80 = 180t
        List<Bogie> s2 = List.of(bogies.get(5));                // 60t
        List<Bogie> s3 = new ArrayList<>();                     // 0t

        stations.add(new StationStop("Salem",            LocalTime.of(14, 0), s1));
        stations.add(new StationStop("Chennai Central",  LocalTime.of(8,  0), s2));
        stations.add(new StationStop("Coimbatore",       LocalTime.of(16, 0), s3));
    }

    // ── capacityByType — groupingBy + summingInt ──────────────────────────────

    @Test
    void testCapacityByTypePassengerCorrect() {
        Map<BogieType, Integer> result = AnalyticsService.capacityByType(bogies);
        // PASSENGER: 72 + 64 + 48 = 184
        assertEquals(184, result.get(BogieType.PASSENGER));
    }

    @Test
    void testCapacityByTypeGoodsCorrect() {
        Map<BogieType, Integer> result = AnalyticsService.capacityByType(bogies);
        // GOODS: 100 + 80 + 60 = 240
        assertEquals(240, result.get(BogieType.GOODS));
    }

    @Test
    void testCapacityByTypeContainsBothTypes() {
        Map<BogieType, Integer> result = AnalyticsService.capacityByType(bogies);
        assertTrue(result.containsKey(BogieType.PASSENGER));
        assertTrue(result.containsKey(BogieType.GOODS));
    }

    @Test
    void testCapacityByTypeEmptyFleet() {
        Map<BogieType, Integer> result = AnalyticsService.capacityByType(new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    // ── averageFreightTonnage ─────────────────────────────────────────────────

    @Test
    void testAverageFreightTonnageCorrect() {
        OptionalDouble avg = AnalyticsService.averageFreightTonnage(bogies);
        assertTrue(avg.isPresent());
        // GOODS: (100 + 80 + 60) / 3 = 80.0
        assertEquals(80.0, avg.getAsDouble(), 0.001);
    }

    @Test
    void testAverageFreightTonnageNoGoodsBogiesReturnsEmpty() {
        // Only PASSENGER bogies — no GOODS
        List<Bogie> passOnly = new ArrayList<>();
        passOnly.add(new PassengerBogie("BG-P1", PassengerSubType.SLEEPER, 72));
        passOnly.add(new PassengerBogie("BG-P2", PassengerSubType.AC_CHAIR, 64));

        OptionalDouble avg = AnalyticsService.averageFreightTonnage(passOnly);

        // UC-13 edge flow: empty OptionalDouble — dashboard prints N/A
        assertFalse(avg.isPresent());
    }

    @Test
    void testAverageFreightTonnageEmptyFleet() {
        OptionalDouble avg = AnalyticsService.averageFreightTonnage(new ArrayList<>());
        assertFalse(avg.isPresent());
    }

    // ── findOverloaded ────────────────────────────────────────────────────────

    @Test
    void testFindOverloadedAboveThreshold() {
        List<Bogie> overloaded = AnalyticsService.findOverloaded(bogies, 70);
        // Bogies above 70: BG-01(72), BG-04(100), BG-05(80)
        assertEquals(3, overloaded.size());
    }

    @Test
    void testFindOverloadedIdsCorrect() {
        List<Bogie> overloaded = AnalyticsService.findOverloaded(bogies, 70);
        List<String> ids = new ArrayList<>();
        for (Bogie b : overloaded) ids.add(b.getBogieId());

        assertTrue(ids.contains("BG-01")); // 72 > 70
        assertTrue(ids.contains("BG-04")); // 100 > 70
        assertTrue(ids.contains("BG-05")); // 80 > 70
        assertFalse(ids.contains("BG-02")); // 64 <= 70
        assertFalse(ids.contains("BG-03")); // 48 <= 70
    }

    @Test
    void testFindOverloadedAllWithinLimits() {
        // Threshold higher than all capacities
        List<Bogie> overloaded = AnalyticsService.findOverloaded(bogies, 999);
        // UC-13 edge flow: empty list — dashboard prints "All within limits"
        assertTrue(overloaded.isEmpty());
    }

    @Test
    void testFindOverloadedAllExceedThreshold() {
        List<Bogie> overloaded = AnalyticsService.findOverloaded(bogies, 0);
        assertEquals(bogies.size(), overloaded.size());
    }

    @Test
    void testFindOverloadedEmptyFleet() {
        List<Bogie> overloaded = AnalyticsService.findOverloaded(new ArrayList<>(), 60);
        assertTrue(overloaded.isEmpty());
    }

    // ── topFreightStation ─────────────────────────────────────────────────────

    @Test
    void testTopFreightStationCorrect() {
        Optional<StationStop> top = AnalyticsService.topFreightStation(stations);
        assertTrue(top.isPresent());
        // Salem has 100 + 80 = 180t (highest)
        assertEquals("Salem", top.get().getStationName());
        assertEquals(180, top.get().totalTonnage());
    }

    @Test
    void testTopFreightStationEmptyCollectionReturnsEmpty() {
        Optional<StationStop> top = AnalyticsService.topFreightStation(new ArrayList<>());
        // UC-13 edge flow: empty Optional
        assertFalse(top.isPresent());
    }

    @Test
    void testTopFreightStationSingleStation() {
        List<StationStop> single = new ArrayList<>();
        single.add(new StationStop("Only Station", LocalTime.of(9, 0),
                List.of(bogies.get(3)))); // 100t

        Optional<StationStop> top = AnalyticsService.topFreightStation(single);
        assertTrue(top.isPresent());
        assertEquals("Only Station", top.get().getStationName());
    }

    @Test
    void testTopFreightStationAllZeroTonnage() {
        List<StationStop> zeroStations = new ArrayList<>();
        zeroStations.add(new StationStop("A", LocalTime.of(8, 0), new ArrayList<>()));
        zeroStations.add(new StationStop("B", LocalTime.of(10, 0), new ArrayList<>()));

        Optional<StationStop> top = AnalyticsService.topFreightStation(zeroStations);
        // Both have 0 — max() returns one of them (whichever stream picks first)
        assertTrue(top.isPresent());
        assertEquals(0, top.get().totalTonnage());
    }

    // ── buildReport produces all fields ───────────────────────────────────────

    @Test
    void testBuildReportAllFieldsPopulated() {
        DashboardReport report = AnalyticsService.buildReport(bogies, stations, 70);

        assertNotNull(report.getCapacityByType());
        assertNotNull(report.getAvgFreight());
        assertNotNull(report.getOverloaded());
        assertNotNull(report.getTopStation());
        assertEquals(70, report.getOverloadThreshold());
    }

    @Test
    void testBuildReportThresholdPreserved() {
        DashboardReport report = AnalyticsService.buildReport(bogies, stations, 85);
        assertEquals(85, report.getOverloadThreshold());
    }

    // ── Stream pipelines use correct method references ────────────────────────

    @Test
    void testPrintSortedManifestDoesNotThrow() {
        // Just verify it doesn't throw — output goes to System.out
        assertDoesNotThrow(() -> AnalyticsService.printSortedManifest(bogies));
    }

    @Test
    void testCapacityByTypeOnlyPassenger() {
        List<Bogie> passOnly = new ArrayList<>();
        passOnly.add(new PassengerBogie("BG-P1", PassengerSubType.SLEEPER, 72));
        passOnly.add(new PassengerBogie("BG-P2", PassengerSubType.AC_CHAIR, 64));

        Map<BogieType, Integer> result = AnalyticsService.capacityByType(passOnly);
        assertEquals(136, result.get(BogieType.PASSENGER));
        assertFalse(result.containsKey(BogieType.GOODS));
    }
}
