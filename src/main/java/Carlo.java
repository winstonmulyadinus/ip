import java.util.Scanner;

/**
 * Provides a command-line task list that stores, displays, and marks tasks.
 */
public class Carlo {
    /**
     * Starts the Carlo command-line application.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        String greeting = """
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
                ____________________________________________________________
                Input your items in order! ({task} {name} {time})
                    possible list items {task}:
                        "todo": tasks without any date/time attached
                        "deadline": for tasks that need to be done by a specific time (do specify using /by ___ )
                        "event": for tasks that start and end at specific times (do specify using /from ___ /to ___)
                Say 'list' and I will show you your list!
                When you're done, just say 'bye'!
                ____________________________________________________________
                """;
        Task[] tasks = new Task[100];
        int taskCount = 0;

        System.out.println(greeting);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {

                String command = scanner.nextLine();

                System.out.println("____________________________________________________________");
                try {
                    if (taskCount == tasks.length) {
                        throw new CarloException("Uh Oh! Uhm, my list is kinda full already...");
                    }

                    if (command.equals("bye")) {
                        System.out.println(" Byeeee! Love always!");
                        System.out.println("____________________________________________________________");
                        break;
                    } else if (command.equals("list")) {
                        System.out.println(" Here's your list!");
                        for (int i = 0; i < taskCount; i++) {
                            System.out.println(" " + (i + 1) + "." + tasks[i]);
                        }
                    } else if (command.startsWith("mark ")) {
                        int taskIndex = getTaskIndex(command, "mark", taskCount);
                        tasks[taskIndex].markAsDone();
                        System.out.println(" YAY! thank you, next!");
                        System.out.println("   " + tasks[taskIndex]);
                    } else if (command.startsWith("unmark ")) {
                        int taskIndex = getTaskIndex(command, "unmark", taskCount);
                        tasks[taskIndex].markAsNotDone();
                        System.out.println(" Awman... okay unmarked for now...");
                        System.out.println("   " + tasks[taskIndex]);
                    } else if (command.equals("todo") || command.startsWith("todo ")) {
                        String description = command.substring("todo".length()).trim();

                        if (description.isEmpty()) {
                            throw new CarloException("hmm... there's nothing to do...");
                        }

                        tasks[taskCount] = new Todo(command.substring(5));
                        taskCount++;
                        System.out.println(" added: " + tasks[taskCount - 1]);
                        System.out.println(" You now have " + taskCount + " tasks in your list!");
                    } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                        tasks[taskCount] = createDeadline(command);
                        taskCount++;
                        System.out.println(" added: " + tasks[taskCount - 1]);
                        System.out.println(" You now have " + taskCount + " tasks in your list!");
                    } else if (command.equals("event") || command.startsWith("event ")) {
                        tasks[taskCount] = createEvent(command);
                        taskCount++;
                        System.out.println(" added: " + tasks[taskCount - 1]);
                        System.out.println(" You now have " + taskCount + " tasks in your list!");
                    } else {
                        throw new CarloException("I'm not too sure what you mean actually...");
                    }
                } catch (CarloException e) {
                    System.out.println("Ohno!! " + e.getMessage());
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
                throw new CarloException("I think it that task number does not exist!!! haha");
            }

            return taskIndex;
        } catch (NumberFormatException e) {
            throw new CarloException("whole numbers only please!");
        }
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
