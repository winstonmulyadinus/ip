package carlo.task;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Deadline}.
 */
class DeadlineTest {

    @Test
    void toString_parsedDate_showsFormattedDate() {
        Deadline deadline = new Deadline("submit report", "2019-12-02");
        assertEquals("[D][ ] submit report (by: Dec 2 2019)", deadline.toString());
    }

    @Test
    void toString_unparseableDueTime_showsRawText() {
        Deadline deadline = new Deadline("submit report", "whenever");
        assertEquals("[D][ ] submit report (by: whenever)", deadline.toString());
    }

    @Test
    void toString_doneDeadline_showsDoneStatusIcon() {
        Deadline deadline = new Deadline("submit report", "2019-12-02");
        deadline.markAsDone();
        assertEquals("[D][X] submit report (by: Dec 2 2019)", deadline.toString());
    }

    @Test
    void toFileFormat_parsedDate_usesIsoDateAndNotDoneFlag() {
        Deadline deadline = new Deadline("submit report", "2019-12-02");
        assertEquals("D | 0 | submit report | 2019-12-02", deadline.toFileFormat());
    }

    @Test
    void toFileFormat_doneDeadline_usesDoneFlag() {
        Deadline deadline = new Deadline("submit report", "2019-12-02");
        deadline.markAsDone();
        assertEquals("D | 1 | submit report | 2019-12-02", deadline.toFileFormat());
    }

    @Test
    void getDueDateTime_parsedDate_matchesCorrectCalendarDay() {
        Deadline deadline = new Deadline("submit report", "2019-12-02");
        assertTrue(deadline.getDueDateTime().isOnDate(LocalDate.of(2019, 12, 2)));
        assertFalse(deadline.getDueDateTime().isOnDate(LocalDate.of(2019, 12, 3)));
    }
}