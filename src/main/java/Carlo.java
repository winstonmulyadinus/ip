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
                Input your items in order!
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
                } else {
                    tasks[taskCount] = new Task(command);
                    taskCount++;
                    System.out.println(" added: " + command);
                }

                System.out.println("____________________________________________________________");
            }
        }
    }
}
