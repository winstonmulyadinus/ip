package carlo;

import carlo.exception.CarloException;
import carlo.storage.Storage;
import carlo.task.CarloDateTime;
import carlo.task.Deadline;
import carlo.task.Event;
import carlo.task.Task;
import carlo.task.Todo;
import carlo.ui.Ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Provides a command-line task list that stores, displays, and marks tasks,
 * saving them to disk so that they persist between runs.
 */
public class Carlo {
    private static final String FILE_PATH = "./data/carlo.txt";

    /**
     * Starts the Carlo command-line application.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage(FILE_PATH);
        List<Task> tasks = storage.load();

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
                    ui.showTaskList(tasks);
                } else if (command.equals("on") || command.startsWith("on ")) {
                    printTasksOnDate(command, tasks, ui);
                } else if (command.equals("find") || command.startsWith("find ")) {
                    findTasks(command, tasks, ui);
                } else if (command.equals("mark") || command.startsWith("mark ")) {
                    int taskIndex = getTaskIndex(command, "mark", tasks.size());
                    tasks.get(taskIndex).markAsDone();
                    storage.save(tasks);
                    ui.showTaskMarked(tasks.get(taskIndex));
                } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                    int taskIndex = getTaskIndex(command, "unmark", tasks.size());
                    tasks.get(taskIndex).markAsNotDone();
                    storage.save(tasks);
                    ui.showTaskUnmarked(tasks.get(taskIndex));
                } else if (command.equals("delete") || command.startsWith("delete ")) {
                    int taskIndex = getTaskIndex(command, "delete", tasks.size());
                    Task deletedTask = tasks.remove(taskIndex);
                    storage.save(tasks);
                    ui.showTaskDeleted(deletedTask, tasks.size());
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring("todo".length()).trim();

                    if (description.isEmpty()) {
                        throw new CarloException("hmm... there's nothing to do...");
                    }

                    tasks.add(new Todo(description));
                    storage.save(tasks);
                    ui.showTaskAdded(tasks.getLast(), tasks.size());
                } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                    tasks.add(createDeadline(command));
                    storage.save(tasks);
                    ui.showTaskAdded(tasks.getLast(), tasks.size());
                } else if (command.equals("event") || command.startsWith("event ")) {
                    tasks.add(createEvent(command));
                    storage.save(tasks);
                    ui.showTaskAdded(tasks.getLast(), tasks.size());
                } else {
                    throw new CarloException("I'm not too sure what you mean actually...");
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
            throw new CarloException("I don't know which task you are referring too... Could you specify the number? Thanku!");
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
     * Prints the tasks whose description contains the keyword given in a
     * {@code find} command.
     *
     * @param command the complete find command
     * @param tasks the tasks to search through
     * @param ui the ui to print through
     * @throws CarloException if no keyword was given
     */
    private static void findTasks(String command, List<Task> tasks, Ui ui) throws CarloException {
        String keyword = command.substring("find".length()).trim();

        if (keyword.isEmpty()) {
            throw new CarloException("what should I look for? try 'find book'!");
        }

        ui.showMatchingTasks(tasks, findMatches(tasks, keyword));
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