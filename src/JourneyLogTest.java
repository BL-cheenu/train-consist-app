import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JourneyLogTest {

    private JourneyLog log;
    private List<Bogie> bogies;

    @BeforeEach
    void setUp() {
        log = new JourneyLog("TN-2201");
        bogies = new ArrayList<>();
        bogies.add(new PassengerBogie("BG-01", PassengerSubType.SLEEPER, 72));
        bogies.add(new PassengerBogie("BG-02", PassengerSubType.AC_CHAIR, 64));
        bogies.add(new GoodsBogie("BG-03", GoodsSubType.RECTANGULAR, 100));
    }

    // ── Event recording ───────────────────────────────────────────────────────

    @Test
    void testAttachRecordsAttachedEvent() {
        Bogie newBogie = new PassengerBogie("BG-04", PassengerSubType.FIRST_CLASS, 48);
        ConsistOperationsService.attach(bogies, new AttachRequest(newBogie, -1), log, "YARD_OP");

        assertEquals(1, log.getEvents().size());
        assertEquals(EventType.ATTACHED, log.getEvents().get(0).getEventType());
        assertEquals("BG-04", log.getEvents().get(0).getBogieId());
        assertEquals("YARD_OP", log.getEvents().get(0).getActorId());
    }

    @Test
    void testDetachRecordsDetachedEvent() {
        ConsistOperationsService.detach(bogies, new DetachRequest("BG-02"), log, "YARD_OP");

        assertEquals(1, log.getEvents().size());
        assertEquals(EventType.DETACHED, log.getEvents().get(0).getEventType());
        assertEquals("BG-02", log.getEvents().get(0).getBogieId());
        assertEquals(-1, log.getEvents().get(0).getPosition());
    }

    @Test
    void testSwapRecordsSwappedEvent() {
        ShuntingService.swapAdjacent(bogies, 0, log, "SHUNTER");

        assertEquals(1, log.getEvents().size());
        assertEquals(EventType.SWAPPED, log.getEvents().get(0).getEventType());
        assertEquals(0, log.getEvents().get(0).getPosition());
    }

    @Test
    void testMoveToFrontRecordsEvent() {
        ShuntingService.moveToFront(bogies, "BG-03", log, "SHUNTER");

        assertEquals(1, log.getEvents().size());
        assertEquals(EventType.MOVED_TO_FRONT, log.getEvents().get(0).getEventType());
        assertEquals("BG-03", log.getEvents().get(0).getBogieId());
        assertEquals(0, log.getEvents().get(0).getPosition());
    }

    @Test
    void testAddBrakeVanRecordsEvent() {
        Bogie brakeVan = new GoodsBogie("BV-01", GoodsSubType.RECTANGULAR, 10);
        ShuntingService.addBrakeVan(bogies, brakeVan, log, "SHUNTER");

        assertEquals(1, log.getEvents().size());
        assertEquals(EventType.BRAKE_VAN_ADDED, log.getEvents().get(0).getEventType());
        assertEquals("BV-01", log.getEvents().get(0).getBogieId());
    }

    // ── Insertion order preserved ─────────────────────────────────────────────

    @Test
    void testEventsPreserveInsertionOrder() {
        Bogie newBogie = new PassengerBogie("BG-04", PassengerSubType.SLEEPER, 60);
        ConsistOperationsService.attach(bogies, new AttachRequest(newBogie, -1), log, "OP1");
        ConsistOperationsService.detach(bogies, new DetachRequest("BG-01"), log, "OP2");
        ShuntingService.swapAdjacent(bogies, 0, log, "OP3");

        List<JourneyEvent> events = log.getEvents();
        assertEquals(3, events.size());
        assertEquals(EventType.ATTACHED, events.get(0).getEventType());
        assertEquals(EventType.DETACHED, events.get(1).getEventType());
        assertEquals(EventType.SWAPPED,  events.get(2).getEventType());
    }

    // ── Failed detach does not record event ───────────────────────────────────

    @Test
    void testDetachMissingBogieDoesNotRecordEvent() {
        boolean removed = ConsistOperationsService.detach(
                bogies, new DetachRequest("BG-99"), log, "OP");
        assertFalse(removed);
        assertTrue(log.isEmpty());
    }

    // ── Empty log ─────────────────────────────────────────────────────────────

    @Test
    void testEmptyLogIsEmpty() {
        assertTrue(log.isEmpty());
        assertEquals(0, log.getEvents().size());
    }

    // ── Replay: timestamp before first event returns empty ────────────────────

    @Test
    void testReplayBeforeFirstEventReturnsEmpty() {
        LocalDateTime past = LocalDateTime.now().minusYears(10);

        // Record an event now (after past)
        ConsistOperationsService.attach(bogies,
                new AttachRequest(new PassengerBogie("BG-04", PassengerSubType.SLEEPER, 60), -1),
                log, "OP");

        List<Bogie> reconstructed = log.replayUpTo(past);
        assertTrue(reconstructed.isEmpty());
    }

    // ── Replay: NOW includes all events ───────────────────────────────────────

    @Test
    void testReplayUpToNowIncludesAllEvents() {
        ConsistOperationsService.attach(bogies,
                new AttachRequest(new PassengerBogie("BG-04", PassengerSubType.SLEEPER, 60), -1),
                log, "OP");
        ConsistOperationsService.attach(bogies,
                new AttachRequest(new PassengerBogie("BG-05", PassengerSubType.AC_CHAIR, 64), -1),
                log, "OP");

        List<Bogie> reconstructed = log.replayUpTo(LocalDateTime.now());
        assertEquals(2, reconstructed.size());
    }

    // ── Replay: inconsistent detach is skipped with warning ───────────────────

    @Test
    void testReplaySkipsInconsistentDetach() {
        // Record a DETACH for a bogie that was never attached in the log
        log.record(new JourneyEvent(
                EventType.DETACHED, "GHOST-01", -1, "OP", LocalDateTime.now().minusSeconds(1)));

        // Should not throw — just prints a warning and continues
        List<Bogie> reconstructed = log.replayUpTo(LocalDateTime.now());
        assertTrue(reconstructed.isEmpty()); // GHOST-01 was never attached, so nothing to show
    }

    // ── Immutability: getEvents returns a copy ────────────────────────────────

    @Test
    void testGetEventsReturnsCopy() {
        ConsistOperationsService.attach(bogies,
                new AttachRequest(new PassengerBogie("BG-04", PassengerSubType.SLEEPER, 60), -1),
                log, "OP");

        List<JourneyEvent> copy = log.getEvents();
        copy.clear(); // mutate the returned list

        // Original log should be unaffected
        assertEquals(1, log.getEvents().size());
    }

    // ── JourneyEvent immutability ─────────────────────────────────────────────

    @Test
    void testJourneyEventFieldsAreCorrect() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 30, 0);
        JourneyEvent event = new JourneyEvent(
                EventType.ATTACHED, "BG-01", 2, "YARD_OP", ts);

        assertEquals(EventType.ATTACHED, event.getEventType());
        assertEquals("BG-01", event.getBogieId());
        assertEquals(2, event.getPosition());
        assertEquals("YARD_OP", event.getActorId());
        assertEquals(ts, event.getTimestamp());
    }
}
