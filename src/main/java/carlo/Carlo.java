package carlo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

import carlo.exception.CarloException;
import carlo.storage.Storage;
import carlo.task.CarloDateTime;
import carlo.task.Deadline;
import carlo.task.Event;
import carlo.task.Task;
import carlo.task.Todo;
import carlo.ui.Ui;



/**
 * Provides a command-line task list that stores, displays, and marks tasks,
 * saving them to disk so that they persist between runs.
 */
public class Carlo {
    private static final String FILE_PATH = "./data/carlo.txt";

    private final Storage storage;
    private final List<Task> tasks;

    /**
     * Creates a Carlo task manager and loads saved tasks from disk.
     */
    public Carlo() {
        storage = new Storage(FILE_PATH);
        tasks = storage.load();
    }

    /**
     * Adds a todo task and saves the updated task list.
     *
     * @param description the description of the todo task
     * @throws CarloException if the description is empty
     */
    public void addTodo(String description) throws CarloException {
        if (description == null || description.isBlank()) {
            throw new CarloException("The todo description cannot be empty.");
        }

        tasks.add(new Todo(description));
        storage.save(tasks);
    }

    /**
     * Adds a deadline task and saves the updated task list.
     *
     * @param description the description of the deadline
     * @param dueTime the deadline date or time
     * @throws CarloException if the description or due time is empty
     */
    public void addDeadline(String description, String dueTime)
            throws CarloException {
        if (description == null || description.isBlank()) {
            throw new CarloException(
                    "Hmm... I need to know what the deadline is for..."
            );
        }

        if (dueTime == null || dueTime.isBlank()) {
            throw new CarloException("When is this deadline due?");
        }

        tasks.add(new Deadline(description, dueTime));
        storage.save(tasks);
    }

    /**
     * Adds an event task and saves the updated task list.
     *
     * @param description the description of the event
     * @param from the event start date or time
     * @param to the event end date or time
     * @throws CarloException if any input is empty
     */
    public void addEvent(String description, String from, String to)
            throws CarloException {
        if (description == null || description.isBlank()) {
            throw new CarloException(
                    "Hmm... I need to know what the event is..."
            );
        }

        if (from == null || from.isBlank()) {
            throw new CarloException("When does this event start?");
        }

        if (to == null || to.isBlank()) {
            throw new CarloException("When does this event end?");
        }

        tasks.add(new Event(description, from, to));
        storage.save(tasks);
    }

    /**
     * Marks a task as completed and saves the updated task list.
     *
     * @param index the zero-based index of the task
     * @throws CarloException if the index is invalid
     */
    public void markTask(int index) throws CarloException {
        validateTaskIndex(index);
        tasks.get(index).markAsDone();
        storage.save(tasks);
    }

    /**
     * Marks a task as incomplete and saves the updated task list.
     *
     * @param index the zero-based index of the task
     * @throws CarloException if the index is invalid
     */
    public void unmarkTask(int index) throws CarloException {
        validateTaskIndex(index);
        tasks.get(index).markAsNotDone();
        storage.save(tasks);
    }

    /**
     * Deletes a task and saves the updated task list.
     *
     * @param index the zero-based index of the task
     * @throws CarloException if the index is invalid
     */
    public void deleteTask(int index) throws CarloException {
        validateTaskIndex(index);
        tasks.remove(index);
        storage.save(tasks);
    }

    /**
     * Checks whether a zero-based task index is valid.
     *
     * @param index the task index to check
     * @throws CarloException if the index does not identify a task
     */
    private void validateTaskIndex(int index) throws CarloException {
        if (index < 0 || index >= tasks.size()) {
            throw new CarloException("That task does not exist.");
        }
    }

    /**
     * Returns the tasks currently stored by Carlo.
     *
     * @return the mutable list of tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Starts the Carlo command-line application.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        Carlo carlo = new Carlo();
        Ui ui = new Ui();

        ui.showGreeting();

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();

            ui.showLine();
            try {
                if (command.equals("bye")) {
                    ui.showGoodbye();
                    ui.showLine();
                    break;
                } else if (command.equals("list")) {
                    ui.showTaskList(carlo.tasks);
                } else if (command.equals("on") || command.startsWith("on ")) {
                    printTasksOnDate(command, carlo.tasks, ui);
                } else if (command.equals("find") || command.startsWith("find ")) {
                    findTasks(command, carlo.tasks, ui);
                } else if (command.equals("mark") || command.startsWith("mark ")) {
                    int taskIndex = getTaskIndex(command, "mark", carlo.tasks.size());

                    carlo.markTask(taskIndex);
                    ui.showTaskMarked(carlo.tasks.get(taskIndex));
                } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                    int taskIndex = getTaskIndex(command, "unmark", carlo.tasks.size());

                    carlo.unmarkTask(taskIndex);
                    ui.showTaskUnmarked(carlo.tasks.get(taskIndex));
                } else if (command.equals("delete") || command.startsWith("delete ")) {
                    int taskIndex = getTaskIndex(command, "delete", carlo.tasks.size());
                    Task deletedTask = carlo.tasks.get(taskIndex);

                    carlo.deleteTask(taskIndex);
                    ui.showTaskDeleted(deletedTask, carlo.tasks.size());
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring("todo".length()).trim();

                    if (description.isEmpty()) {
                        throw new CarloException("hmm... there's nothing to do...");
                    }

                    carlo.addTodo(description);
                    ui.showTaskAdded(carlo.tasks.getLast(), carlo.tasks.size());
                } else if (command.equals("deadline")
                        || command.startsWith("deadline ")) {
                    Deadline deadline = createDeadline(command);

                    carlo.tasks.add(deadline);
                    carlo.storage.save(carlo.tasks);

                    ui.showTaskAdded(carlo.tasks.getLast(), carlo.tasks.size());
                } else if (command.equals("event")
                        || command.startsWith("event ")) {
                    Event event = createEvent(command);

                    carlo.tasks.add(event);
                    carlo.storage.save(carlo.tasks);

                    ui.showTaskAdded(carlo.tasks.getLast(), carlo.tasks.size());
                } else {
                    throw new CarloException(
                            "I'm not too sure what you mean actually...");
                }
            } catch (CarloException e) {
                ui.showError(e.getMessage());
            }

            ui.showLine();
        }
    }

    /**
     * Extracts and validates the one-based task number supplied in a command.
     *
     * @param command the complete command entered by the user
     * @param commandName the command prefix, such as {@code mark}
     * @param taskCount the number of tasks currently stored
     * @return the corresponding zero-based index in the task list
     * @throws CarloException if the task number is missing, not a whole number,
     *         or does not identify a stored task
     */
    static int getTaskIndex(String command, String commandName, int taskCount)
            throws CarloException {
        String numberText = command.substring(commandName.length()).trim();

        if (numberText.isEmpty()) {
            throw new CarloException(
                    "I don't know which task you are referring too... Could you specify the number? Thanku!"
            );
        }

        try {
            int taskIndex = Integer.parseInt(numberText) - 1;

            if (taskIndex < 0 || taskIndex >= taskCount) {
                throw new CarloException("I think that task number does not exist!!! haha");
            }

            return taskIndex;
        } catch (NumberFormatException e) {
            throw new CarloException("whole numbers only please!");
        }
    }

    /**
     * Prints the deadlines and events that fall on the date given in an
     * {@code on} command.
     *
     * @param command the complete on command
     * @param tasks the tasks to search through
     * @param ui the ui to print through
     * @throws CarloException if no date was given or it could not be understood
     */
    private static void printTasksOnDate(String command, List<Task> tasks, Ui ui) throws CarloException {
        String dateText = command.substring("on".length()).trim();

        if (dateText.isEmpty()) {
            throw new CarloException("which date do you mean? try something like 'on 2019-12-02'!");
        }

        LocalDate date;
        try {
            date = CarloDateTime.parseDate(dateText);
        } catch (DateTimeParseException e) {
            throw new CarloException("I couldn't understand that date! try yyyy-mm-dd, like 2019-12-02");
        }

        boolean[] matches = new boolean[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            matches[i] = occursOnDate(tasks.get(i), date);
        }

        ui.showTasksOnDate(date, tasks, matches);
    }

    /**
     * Returns whether the given task falls on the given date.
     *
     * @param task the task to check
     * @param date the date to check against
     * @return {@code true} if {@code task} is a deadline due on {@code date}
     *         or an event occurring on {@code date}
     */
    private static boolean occursOnDate(Task task, LocalDate date) {
        if (task instanceof Deadline deadline) {
            return deadline.getDueDateTime().isOnDate(date);
        } else if (task instanceof Event event) {
            return event.occursOn(date);
        }
        return false;
    }

    /**
     * Creates a deadline from a command using the {@code /by} separator.
     *
     * @param command the complete deadline command
     * @return the deadline described by the command
     * @throws CarloException if the deadline description is empty
     */
    private static Deadline createDeadline(String command) throws CarloException {
        String details = command.substring("deadline".length()).trim();
        int byIndex = details.indexOf("/by");

        String description;
        String by;

        if (byIndex == -1) {
            description = details.trim();
            by = "today";
        } else {
            description = details.substring(0, byIndex).trim();
            by = details.substring(byIndex + "/by".length()).trim();

            if (by.isEmpty()) {
                by = "today";
            }
        }

        if (description.isEmpty()) {
            throw new CarloException("hmm... I need to know what the deadline is for...");
        }

        return new Deadline(description, by);
    }

    /**
     * Creates an event from a command using the {@code /from} and {@code /to} separators.
     *
     * @param command the complete event command
     * @return the event described by the command
     * @throws CarloException if the event description is empty
     */
    private static Event createEvent(String command) throws CarloException {
        String details = command.substring("event".length()).trim();
        int fromIndex = details.indexOf("/from");

        if (fromIndex == -1) {
            if (details.isEmpty()) {
                throw new CarloException("hmm... I need to know what the event is...");
            }
            return new Event(details.trim(), "today", "today");
        }

        String description = details.substring(0, fromIndex).trim();
        String timeDetails = details.substring(fromIndex + "/from".length()).trim();
        int toIndex = timeDetails.indexOf("/to");

        String from;
        String to;

        if (toIndex == -1) {
            from = timeDetails.isEmpty() ? "today" : timeDetails;
            to = "today";
        } else {
            from = timeDetails.substring(0, toIndex).trim();
            to = timeDetails.substring(toIndex + "/to".length()).trim();

            if (from.isEmpty()) {
                from = "today";
            }
            if (to.isEmpty()) {
                to = "today";
            }
        }

        if (description.isEmpty()) {
            throw new CarloException("hmm... I need to know what the event is...");
        }

        return new Event(description, from, to);
    }

    /**
     * Displays tasks whose descriptions contain the keyword from a CLI command.
     *
     * @param command the complete find command
     * @param tasks the tasks to search through
     * @param ui the command-line user interface
     * @throws CarloException if no keyword was provided
     */
    private static void findTasks(
            String command,
            List<Task> tasks,
            Ui ui
    ) throws CarloException {
        String keyword = command.substring("find".length()).trim();

        if (keyword.isEmpty()) {
            throw new CarloException(
                    "what should I look for?\nTry 'find book'!"
            );
        }

        ui.showMatchingTasks(tasks, findMatches(tasks, keyword));
    }

    /**
     * Finds tasks whose descriptions contain the given keyword.
     *
     * @param keyword the keyword to search for
     * @return a list of matching tasks
     * @throws CarloException if the keyword is empty
     */
    public List<Task> findTasks(String keyword) throws CarloException {
        if (keyword == null || keyword.isBlank()) {
            throw new CarloException(
                    "What should I look for?\nTry searching for a keyword!"
            );
        }

        String lowerKeyword = keyword.toLowerCase();

        List<Task> matches = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDescription().toLowerCase().contains(lowerKeyword)) {
                matches.add(task);
            }
        }

        return matches;
    }

    /**
     * Returns tasks that occur on a given date.
     *
     * @param date the date to search for
     * @return tasks occurring on the date
     */
    public List<Task> getTasksOnDate(LocalDate date) {
        List<Task> matches = new ArrayList<>();

        for (Task task : tasks) {
            if (occursOnDate(task, date)) {
                matches.add(task);
            }
        }

        return matches;
    }

    /**
     * Returns which of the given tasks have a description containing the
     * given keyword, using a case-insensitive substring match.
     *
     * @param tasks the tasks to search through
     * @param keyword the keyword to search for
     * @return a boolean for each task in {@code tasks}, in the same order,
     *         indicating whether that task's description contains {@code keyword}
     */
    static boolean[] findMatches(List<Task> tasks, String keyword) {
        boolean[] matches = new boolean[tasks.size()];
        String lowerKeyword = keyword.toLowerCase();

        for (int i = 0; i < tasks.size(); i++) {
            matches[i] = tasks.get(i).getDescription().toLowerCase().contains(lowerKeyword);
        }

        return matches;
    }
}