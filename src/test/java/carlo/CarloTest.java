package carlo;

import carlo.exception.CarloException;
import carlo.task.Task;
import org.junit.jupiter.api.Test;
import carlo.task.Todo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Carlo#getTaskIndex(String, String, int)} and
 * {@link Carlo#findMatches(List, String)}.
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

    @Test
    void findMatches_singleMatch_returnsOnlyThatTaskTrue() {
        List<Task> tasks = List.of(new Todo("read book"), new Todo("buy milk"));
        boolean[] expected = {true, false};
        assertArrayEquals(expected, Carlo.findMatches(tasks, "book"));
    }

    @Test
    void findMatches_multipleMatches_returnsAllMatchingTrue() {
        List<Task> tasks = List.of(
                new Todo("read book"),
                new Todo("return book"),
                new Todo("buy milk"));
        boolean[] expected = {true, true, false};
        assertArrayEquals(expected, Carlo.findMatches(tasks, "book"));
    }

    @Test
    void findMatches_caseInsensitive_matchesRegardlessOfCase() {
        List<Task> tasks = List.of(new Todo("Read Book"));
        boolean[] expected = {true};
        assertArrayEquals(expected, Carlo.findMatches(tasks, "BOOK"));
    }

    @Test
    void findMatches_noMatches_returnsAllFalse() {
        List<Task> tasks = List.of(new Todo("buy milk"), new Todo("walk dog"));
        boolean[] expected = {false, false};
        assertArrayEquals(expected, Carlo.findMatches(tasks, "book"));
    }

    @Test
    void findMatches_emptyTaskList_returnsEmptyArray() {
        assertArrayEquals(new boolean[0], Carlo.findMatches(List.of(), "book"));
    }

    @Test
    void findMatches_keywordMatchesPartialWord_returnsTrue() {
        List<Task> tasks = List.of(new Todo("submit textbook order"));
        boolean[] expected = {true};
        assertArrayEquals(expected, Carlo.findMatches(tasks, "book"));
    }
}