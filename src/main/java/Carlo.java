import java.util.Scanner;

public class Carlo {
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
                  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝ ╚═════╝\s
                Cheers! My name is Carlo!
                I can help you to list down anything!
                ____________________________________________________________
                Input your items in order!
                Say 'list' and I will show you your list!
                When you're done, just say 'bye'!
                ____________________________________________________________
                """;
        String farewell = "Byeeee! Love always! <3\n" +
                "____________________________________________________________\n";
        String[] tasks = new String[100];
        int taskCount = 0;

        System.out.println(greeting);

        try (Scanner scanner = new Scanner(System.in)) {bye
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                System.out.println("____________________________________________________________");

                if (command.equals("bye")) {
                    System.out.println(farewell);
                    break;
                } else if (command.equals("list")) {
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println(" " + (i + 1) + ". " + tasks[i]);
                    }
                } else {
                    tasks[taskCount] = command;
                    taskCount++;
                    System.out.println(" added: " + command);
                }


                // System.out.println(" " + command);
                System.out.println("____________________________________________________________");
            }
        }
    }
}
