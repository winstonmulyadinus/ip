package carlo.task;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Event#occursOn(LocalDate)}.
 */
class EventTest {

    @Test
    void occursOn_dateWithinRange_returnsTrue() {
        Event event = new Event("trip", "2019-12-01", "2019-12-05");
        assertTrue(event.occursOn(LocalDate.of(2019, 12, 3)));
    }

    @Test
    void occursOn_dateEqualsStart_returnsTrue() {
        Event event = new Event("trip", "2019-12-01", "2019-12-05");
        assertTrue(event.occursOn(LocalDate.of(2019, 12, 1)));
    }

    @Test
    void occursOn_dateEqualsEnd_returnsTrue() {
        Event event = new Event("trip", "2019-12-01", "2019-12-05");
        assertTrue(event.occursOn(LocalDate.of(2019, 12, 5)));
    }

    @Test
    void occursOn_dateBeforeStart_returnsFalse() {
        Event event = new Event("trip", "2019-12-01", "2019-12-05");
        assertFalse(event.occursOn(LocalDate.of(2019, 11, 30)));
    }

    @Test
    void occursOn_dateAfterEnd_returnsFalse() {
        Event event = new Event("trip", "2019-12-01", "2019-12-05");
        assertFalse(event.occursOn(LocalDate.of(2019, 12, 6)));
    }

    @Test
    void occursOn_onlyStartParseable_comparesAgainstStartOnly() {
        Event event = new Event("trip", "2019-12-01", "whenever it ends");
        assertTrue(event.occursOn(LocalDate.of(2019, 12, 1)));
        assertFalse(event.occursOn(LocalDate.of(2019, 12, 2)));
    }

    @Test
    void occursOn_onlyEndParseable_comparesAgainstEndOnly() {
        Event event = new Event("trip", "whenever it starts", "2019-12-05");
        assertTrue(event.occursOn(LocalDate.of(2019, 12, 5)));
        assertFalse(event.occursOn(LocalDate.of(2019, 12, 4)));
    }

    @Test
    void occursOn_neitherDateParseable_returnsFalse() {
        Event event = new Event("trip", "someday", "some other day");
        assertFalse(event.occursOn(LocalDate.of(2019, 12, 1)));
    }
}