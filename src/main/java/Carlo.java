import java.util.Scanner;

public class Carlo {
    public static void main(String[] args) {
        String greeting = "____________________________________________________________\n" +
                "  ██████╗                      \n" +
                " ██╔════╝                      \n" +
                " ██║       █████╗ ██████╗ ██╗      ██████╗ \n" +
                " ██║      ██╔══██╗██╔══██╗██║     ██╔═══██╗\n" +
                " ██║      ███████║██████╔╝██║     ██║   ██║\n" +
                " ██║      ██╔══██║██╔══██╗██║     ██║   ██║\n" +
                " ╚███████╗██║  ██║██║  ██║███████╗╚██████╔╝\n" +
                "  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝ ╚═════╝ \n" +
                "Cheers! My name is Carlo!\n" +
                "How may I help you?\n" +
                "____________________________________________________________\n";
        String farewell = "Byeeee! Love always!\n" +
                "____________________________________________________________\n";
        System.out.println(greeting);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                System.out.println("____________________________________________________________");

                if (command.equals("bye")) {
                    System.out.println("Byeee! Love always!");
                    System.out.println("____________________________________________________________");
                    break;
                }

                System.out.println(" " + command);
                System.out.println("____________________________________________________________");
            }
        }
    }
}
