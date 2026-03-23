import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

public class ConsistQueryServiceTest {

    private List<Bogie> bogies;
    private BogieIndex index;

    @BeforeEach
    void setUp() {
        bogies = new ArrayList<>();
        // Mixed consist: PASSENGER and GOODS with varying capacities/weights
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72, 45));
        bogies.add(new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64, 40));
        bogies.add(new GoodsBogie("BG-03", GoodsSubType.RECTANGULAR, 100, 80));
        bogies.add(new PassengerBogie("BG-04", PassengerSubType.FIRST_CLASS, 48, 35));
        bogies.add(new GoodsBogie("BG-05", GoodsSubType.CYLINDRICAL, 80, 65));
        bogies.add(new PassengerBogie("BG-06", PassengerSubType.SLEEPER, 72, 50));

        index = BogieIndex.build(bogies);
    }

    // ── findHeaviest — Optional present ──────────────────────────────────────

    @Test
    void testFindHeaviestPassengerReturnsOptionalPresent() {
        Predicate<Bogie> isPassenger = new QueryBuilder()
                .withType(BogieType.PASSENGER).build();

        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, isPassenger);

        assertTrue(result.isFound());
        // Heaviest PASSENGER: BG-01 and BG-06 both have capacity 72 — tiebreak by ID
        assertEquals(72, result.getResult().get().getCapacity());
    }

    @Test
    void testFindHeaviestGoodsReturnsCorrectBogie() {
        Predicate<Bogie> isGoods = new QueryBuilder()
                .withType(BogieType.GOODS).build();

        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, isGoods);

        assertTrue(result.isFound());
        assertEquals("BG-03", result.getResult().get().getBogieId()); // capacity 100
    }

    // ── findHeaviest — Optional empty ─────────────────────────────────────────

    @Test
    void testFindHeaviestNoMatchReturnsEmpty() {
        // Filter with impossible condition — no bogies have capacity > 999
        Predicate<Bogie> impossible = new QueryBuilder()
                .withMinCapacity(999).build();

        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, impossible);

        assertFalse(result.isFound());
        assertFalse(result.getResult().isPresent());
    }

    @Test
    void testFindHeaviestEmptyListReturnsEmpty() {
        Predicate<Bogie> anyBogie = b -> true;
        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(new ArrayList<>(), anyBogie);
        assertFalse(result.isFound());
    }

    // ── Proximity check — violation detected ─────────────────────────────────

    @Test
    void testProximityCheckDetectsViolation() {
        // BG-05 is CYLINDRICAL at index 4; BG-06 is PASSENGER at index 5
        // Window of 2 around BG-05: indices 2–5 — includes BG-06
        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, "BG-05", 2);

        assertTrue(result.isFound());
        ProximityViolation v = result.getResult().get();
        assertEquals("BG-05", v.getSourceBogieId());
        assertEquals("BG-06", v.getConflictingBogieId());
    }

    @Test
    void testProximityCheckViolationContainsReason() {
        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, "BG-05", 2);

        assertTrue(result.isFound());
        assertNotNull(result.getResult().get().getReason());
        assertFalse(result.getResult().get().getReason().isEmpty());
    }

    // ── Proximity check — no violation ───────────────────────────────────────

    @Test
    void testProximityCheckNoViolationReturnsEmpty() {
        // BG-03 is RECTANGULAR (not CYLINDRICAL) — no conflict possible
        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, "BG-03", 3);

        assertFalse(result.isFound());
    }

    @Test
    void testProximityCheckBogieNotFoundReturnsEmpty() {
        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, "BG-99", 3);

        assertFalse(result.isFound());
    }

    @Test
    void testProximityCheckWindowClampedAtBoundary() {
        // BG-01 is PASSENGER at position 0 — no left neighbour
        // BG-05 (CYLINDRICAL) is at index 4 — outside window of 2 from pos 0
        QueryResult<ProximityViolation> result =
                ConsistQueryService.proximityCheck(bogies, "BG-01", 2);

        // BG-05 at index 4 is outside window [0, 2] — no violation
        assertFalse(result.isFound());
    }

    // ── Predicate.and() — compound filter ─────────────────────────────────────

    @Test
    void testCompoundFilterAndComposition() {
        Predicate<Bogie> composed = new QueryBuilder()
                .withType(BogieType.PASSENGER)
                .withMinCapacity(65)
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, composed);

        // PASSENGER with capacity >= 65: BG-01(72), BG-02(64→no), BG-06(72)
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getBogieType() == BogieType.PASSENGER));
        assertTrue(result.stream().allMatch(b -> b.getCapacity() >= 65));
    }

    @Test
    void testCompoundFilterReturnsEmptyWhenNoMatch() {
        Predicate<Bogie> impossible = new QueryBuilder()
                .withType(BogieType.GOODS)
                .withMinCapacity(999)
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, impossible);
        assertTrue(result.isEmpty());
    }

    // ── Predicate.or() ────────────────────────────────────────────────────────

    @Test
    void testPredicateOrComposition() {
        Predicate<Bogie> eitherType = new QueryBuilder()
                .withTypeOr(BogieType.PASSENGER, BogieType.GOODS)
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, eitherType);

        // All bogies match — OR accepts both types
        assertEquals(bogies.size(), result.size());
    }

    // ── Predicate.negate() ────────────────────────────────────────────────────

    @Test
    void testPredicateNegateExcludesGoods() {
        Predicate<Bogie> noGoods = new QueryBuilder()
                .excludingType(BogieType.GOODS)
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, noGoods);

        // Should return only PASSENGER bogies
        assertEquals(4, result.size());
        assertTrue(result.stream().noneMatch(b -> b.getBogieType() == BogieType.GOODS));
    }

    // ── Optional.flatMap in safeCargoUpdate ───────────────────────────────────

    @Test
    void testSafeCargoUpdateFlatMapFoundBogie() {
        CargoUpdate update = new CargoUpdate("BG-03", 55.5, "Grain");
        Optional<Bogie> result = ConsistQueryService.safeCargoUpdate(index, update);

        // flatMap chain: bogieId → lookup → ofNullable → map(update) → present
        assertTrue(result.isPresent());
        assertEquals(55.5, result.get().getCargoWeight(), 0.001);
        assertEquals("Grain", result.get().getCargoDesc());
    }

    @Test
    void testSafeCargoUpdateFlatMapMissingIdReturnsEmpty() {
        CargoUpdate update = new CargoUpdate("BG-GHOST", 30.0, "Air");
        Optional<Bogie> result = ConsistQueryService.safeCargoUpdate(index, update);

        // flatMap: ofNullable(null) returns empty — ifPresent body never runs, no NPE
        assertFalse(result.isPresent());
    }

    @Test
    void testSafeCargoUpdateDoesNotThrowOnMissingId() {
        CargoUpdate update = new CargoUpdate("NONEXISTENT", 10.0, "Test");
        assertDoesNotThrow(() -> ConsistQueryService.safeCargoUpdate(index, update));
    }

    // ── QueryBuilder fluent chain ─────────────────────────────────────────────

    @Test
    void testQueryBuilderChainWithThreeCriteria() {
        Predicate<Bogie> complex = new QueryBuilder()
                .withType(BogieType.GOODS)
                .withMinCapacity(60)
                .withMaxWeight(90)
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, complex);

        // GOODS with capacity >= 60 and weight <= 90: BG-05(cap=80, wt=65)
        assertEquals(1, result.size());
        assertEquals("BG-05", result.get(0).getBogieId());
    }

    @Test
    void testQueryBuilderSubTypeFilter() {
        Predicate<Bogie> acChair = new QueryBuilder()
                .withSubType("AC_CHAIR")
                .build();

        List<Bogie> result = ConsistQueryService.findAll(bogies, acChair);

        assertEquals(1, result.size());
        assertEquals("BG-02", result.get(0).getBogieId());
    }

    // ── QueryResult telemetry ─────────────────────────────────────────────────

    @Test
    void testQueryResultExecutionTimeIsNonNegative() {
        Predicate<Bogie> anyBogie = b -> true;
        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, anyBogie);
        assertTrue(result.getExecutionTimeNs() >= 0);
    }

    @Test
    void testQueryResultDescriptionIsNotEmpty() {
        Predicate<Bogie> anyBogie = b -> true;
        QueryResult<Bogie> result = ConsistQueryService.findHeaviest(bogies, anyBogie);
        assertNotNull(result.getQueryDescription());
        assertFalse(result.getQueryDescription().isEmpty());
    }
}
