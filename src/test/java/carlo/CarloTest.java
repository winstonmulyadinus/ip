package carlo;

import carlo.exception.CarloException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Carlo#getTaskIndex(String, String, int)}.
 */
class CarloTest {

    @Test
    void getTaskIndex_firstTask_returnsZero() throws CarloException {
        assertEquals(0, Carlo.getTaskIndex("mark 1", "mark", 3));
    }

    @Test
    void getTaskIndex_lastTask_returnsLastZeroBasedIndex() throws CarloException {
        assertEquals(2, Carlo.getTaskIndex("mark 3", "mark", 3));
    }

    @Test
    void getTaskIndex_numberBelowRange_throwsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark 0", "mark", 3));
    }

    @Test
    void getTaskIndex_numberAboveRange_throwsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark 4", "mark", 3));
    }

    @Test
    void getTaskIndex_negativeNumber_throwsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark -1", "mark", 3));
    }

    @Test
    void getTaskIndex_missingNumber_throwsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark", "mark", 3));
    }

    @Test
    void getTaskIndex_nonNumericInput_throwsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark abc", "mark", 3));
    }

    @Test
    void getTaskIndex_emptyTaskList_anyNumberThrowsCarloException() {
        assertThrows(CarloException.class, () -> Carlo.getTaskIndex("mark 1", "mark", 0));
    }
}