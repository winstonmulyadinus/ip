package carlo;

import carlo.exception.CarloException;
import carlo.storage.Storage;
import carlo.task.CarloDateTime;
import carlo.task.Deadline;
import carlo.task.Event;
import carlo.task.Task;
import carlo.task.Todo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Provides a command-line task list that stores, displays, and marks tasks,
 * saving them to disk so that they persist between runs.
 */
public class Carlo {
    private static final String FILE_PATH = "./data/carlo.txt";
    private static final DateTimeFormatter ON_DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy");

    /**
     * Starts the Carlo command-line application.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        final String GREETING = """
                ____________________________________________________________
                  ██████╗                     \s
                 ██╔════╝                     \s
                 ██║       █████╗ ██████╗ ██╗      ██████╗\s
                 ██║      ██╔══██╗██╔══██╗██║     ██╔═══██╗
                 ██║      ███████║██████╔╝██║     ██║   ██║
                 ██║      ██╔══██║██╔══██╗██║     ██║   ██║
                 ╚███████╗██║  ██║██║  ██║███████╗╚██████╔╝
                  ╚══════╝╚═╝  ╚═╝╚═╝  ╚══════╝ ╚═════╝\s
                Cheers! My name is Carlo!
                I can help you to list down anything!
                ____________________________________________________________""";
        final String HELP = """
                Input your items in order! ({task} {name} {time})
                    possible list items {task}:
                        "todo": tasks without any date/time attached
                        "deadline": for tasks that need to be done by a specific time (do specify using /by ___ )
                        "event": for tasks that start and end at specific times (do specify using /from ___ /to ___)
                    Dates can be given as yyyy-mm-dd or yyyy-mm-dd HHmm, e.g. 2019-12-02 or 2019-12-02 1800!
                Say 'list' and I will show you your list!
                Say 'on {date}' (e.g. on 2019-12-02, or on today/tomorrow/yesterday) to see what's happening that day!
                When you're done, just say 'bye'!""";

        Storage storage = new Storage(FILE_PATH);
        List<Task> tasks = storage.load();

        System.out.println(GREETING);
        System.out.println(HELP + "\n____________________________________________________________");


        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {

                String command = scanner.nextLine();

                System.out.println("____________________________________________________________");
                try {
                    if (command.equals("bye")) {
                        System.out.println(" Byeeee! Love always!");
                        System.out.println("____________________________________________________________");
                        break;
                    } else if (command.equals("list")) {
                        System.out.println(" Here's your list!");
                        for (int i = 0; i < tasks.size(); i++) {
                            System.out.println(" " + (i + 1) + "." + tasks.get(i));
                        }
                    } else if (command.equals("on") || command.startsWith("on ")) {
                        printTasksOnDate(command, tasks);
                    } else if (command.equals("mark") || command.startsWith("mark ")) {
                        int taskIndex = getTaskIndex(command, "mark", tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        storage.save(tasks);
                        System.out.println(" YAY! thank you, next!");
                        System.out.println("   " + tasks.get(taskIndex));
                    } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                        int taskIndex = getTaskIndex(command, "unmark", tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        storage.save(tasks);
                        System.out.println(" Awman... okay unmarked for now...");
                        System.out.println("   " + tasks.get(taskIndex));
                    } else if (command.equals("delete") || command.startsWith("delete ")) {
                        int taskIndex = getTaskIndex(command, "delete", tasks.size());
                        Task deletedTask = tasks.remove(taskIndex);
                        storage.save(tasks);
                        System.out.println(" Okay okay, I've removed this task!");
                        System.out.println("   " + deletedTask);
                        System.out.println(" You now have " + tasks.size() + " tasks in your list!");
                    } else if (command.equals("todo") || command.startsWith("todo ")) {
                        String description = command.substring("todo".length()).trim();

                        if (description.isEmpty()) {
                            throw new CarloException("hmm... there's nothing to do...");
                        }

                        tasks.add(new Todo(description));
                        storage.save(tasks);
                        System.out.println(" added: " + tasks.getLast());
                        System.out.println(" You now have " + tasks.size() + " tasks in your list!");
                    } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                        tasks.add(createDeadline(command));
                        storage.save(tasks);
                        System.out.println(" added: " + tasks.getLast());
                        System.out.println(" You now have " + tasks.size() + " tasks in your list!");
                    } else if (command.equals("event") || command.startsWith("event ")) {
                        tasks.add(createEvent(command));
                        storage.save(tasks);
                        System.out.println(" added: " + tasks.getLast());
                        System.out.println(" You now have " + tasks.size() + " tasks in your list!");
                    } else {
                        throw new CarloException("I'm not too sure what you mean actually...");
                    }
                } catch (CarloException e) {
                    System.out.println("Ohno!! " + e.getMessage());
                    System.out.println(HELP);
                }
                System.out.println("____________________________________________________________");
            }
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
     * @throws CarloException if no date was given or it could not be understood
     */
    private static void printTasksOnDate(String command, List<Task> tasks) throws CarloException {
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

        System.out.println(" Here's what's happening on " + date.format(ON_DATE_DISPLAY_FORMAT) + ":");

        boolean foundAny = false;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (occursOnDate(task, date)) {
                System.out.println(" " + (i + 1) + "." + task);
                foundAny = true;
            }
        }

        if (!foundAny) {
            System.out.println(" Nothing on that day! Free day yay!");
        }
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
}