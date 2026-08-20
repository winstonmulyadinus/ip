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
                Input your items in order! (task {name} {time})
                    possible list items:
                        "todo": tasks without any date/time attached (default)
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
                    int taskIndex = Integer.parseInt(command.substring(5)) - 1;
                    tasks[taskIndex].markAsDone();
                    System.out.println(" YAY! thank you, next!");
                    System.out.println("   " + tasks[taskIndex]);
                } else if (command.startsWith("unmark ")) {
                    int taskIndex = Integer.parseInt(command.substring(7)) - 1;
                    tasks[taskIndex].markAsNotDone();
                    System.out.println(" Awman... okay unmarked for now...");
                    System.out.println("   " + tasks[taskIndex]);
                } else if (command.startsWith("todo ")) {
                    tasks[taskCount] = new Todo(command.substring(5));
                    taskCount++;
                    System.out.println(" added: " + tasks[taskCount - 1]);
                    System.out.println(" You now have " + taskCount + " tasks in your list!");
                } else if (command.startsWith("deadline ")) {
                    tasks[taskCount] = createDeadline(command);
                    taskCount++;
                    System.out.println(" added: " + tasks[taskCount - 1]);
                    System.out.println(" You now have " + taskCount + " tasks in your list!");
                } else if (command.startsWith("event ")) {
                    tasks[taskCount] = createEvent(command);
                    taskCount++;
                    System.out.println(" added: " + tasks[taskCount - 1]);
                    System.out.println(" You now have " + taskCount + " tasks in your list!");
                } else {
                    tasks[taskCount] = new Todo(command);
                    taskCount++;
                    System.out.println(" added: " + tasks[taskCount - 1]);
                }

                System.out.println("____________________________________________________________");
            }
        }
    }

    /**
     * Creates a deadline from a command using the {@code /by} separator.
     *
     * @param command the complete deadline command
     * @return the deadline described by the command
     */
    private static Deadline createDeadline(String command) {
        String details = command.substring("deadline ".length());
        int byIndex = details.indexOf(" /by");

        String description;
        String by;

        if (byIndex == -1) {
            description = details.trim();
            by = "today";
        } else {
            description = details.substring(0, byIndex).trim();
            by = details.substring(byIndex + " /by".length()).trim();

            if (by.isEmpty()) {
                by = "today";
            }
        }

        return new Deadline(description, by);
    }

    /**
     * Creates an event from a command using the {@code /from} and {@code /to} separators.
     *
     * @param command the complete event command
     * @return the event described by the command
     */
    private static Event createEvent(String command) {
        String details = command.substring("event ".length());
        int fromIndex = details.indexOf(" /from");

        if (fromIndex == -1) {
            return new Event(details.trim(), "today", "today");
        }

        String description = details.substring(0, fromIndex).trim();
        String timeDetails = details.substring(fromIndex + " /from".length()).trim();
        int toIndex = timeDetails.indexOf(" /to");

        String from;
        String to;

        if (toIndex == -1) {
            from = timeDetails.isEmpty() ? "today" : timeDetails;
            to = "today";
        } else {
            from = timeDetails.substring(0, toIndex).trim();
            to = timeDetails.substring(toIndex + " /to".length()).trim();

            if (from.isEmpty()) {
                from = "today";
            }
            if (to.isEmpty()) {
                to = "today";
            }
        }

        return new Event(description, from, to);
    }
}
