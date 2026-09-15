package carlo.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import carlo.Carlo;
import carlo.exception.CarloException;
import carlo.task.CarloDateTime;
import carlo.task.Task;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Provides the JavaFX graphical user interface for Carlo.
 */
public class MainWindow extends Application {
    private static final String GREETING = """
        Cheers! My name is Carlo!
        I can help you to list down anything!
        """;

    private static final String HELP_TEXT = """
        Input your items in order! ({task} {name} {time})

        Possible list items:
        • todo: tasks without any date/time attached
        • deadline: tasks that need to be completed by a specific time
        • event: tasks that start and end at specific times
        Click Undo to reverse the last task operation.

        Dates can be given as yyyy-mm-dd or yyyy-mm-dd HHmm.
        """;

    private static final int SPACING = 10;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int IMAGE_SIZE = 120;
    private static final int MESSAGE_WIDTH = 500;

    private Carlo carlo;
    private ListView<Task> taskListView;
    private TextField taskInput;
    private TextField deadlineDescriptionInput;
    private TextField deadlineTimeInput;

    private TextField eventDescriptionInput;
    private TextField eventFromInput;
    private TextField eventToInput;

    private TextField searchInput;
    private TextField dateInput;

    private Label messageLabel;

    /**
     * Starts the Carlo graphical user interface.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        carlo = new Carlo();
        taskListView = new ListView<>();

        BorderPane root = createLayout();
        refreshTaskList();

        stage.setTitle("Carlo");
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    /**
     * Creates the main window layout.
     */
    private BorderPane createLayout() {
        BorderPane root = new BorderPane();
        root.setTop(createTopArea());
        root.setCenter(taskListView);
        root.setBottom(new VBox(SPACING, createActionArea()));
        return root;
    }

    /**
     * Creates the input and message sections at the top of the window.
     */
    private BorderPane createTopArea() {
        VBox inputArea = new VBox(
                SPACING,
                new Label(GREETING),
                createTodoArea(),
                createDeadlineArea(),
                createEventArea(),
                createSearchArea()
        );

        VBox messageArea = createMessageArea();

        BorderPane topArea = new BorderPane();
        topArea.setLeft(inputArea);
        topArea.setRight(messageArea);
        BorderPane.setMargin(messageArea, new Insets(SPACING));
        return topArea;
    }

    /**
     * Creates the todo input row.
     */
    private HBox createTodoArea() {
        taskInput = createTextField("Enter a todo", this::addTodo);

        return new HBox(
                SPACING,
                taskInput,
                createButton("Add", this::addTodo),
                createButton("Help", this::showHelp)
        );
    }

    /**
     * Creates the deadline input row.
     */
    private HBox createDeadlineArea() {
        deadlineDescriptionInput = createTextField(
                "Deadline description", this::addDeadline);
        deadlineTimeInput = createTextField(
                "Due date/time", this::addDeadline);

        return new HBox(
                SPACING,
                deadlineDescriptionInput,
                deadlineTimeInput,
                createButton("Add deadline", this::addDeadline)
        );
    }

    /**
     * Creates the event input row.
     */
    private HBox createEventArea() {
        eventDescriptionInput = createTextField(
                "Event description", this::addEvent);
        eventFromInput = createTextField("From", this::addEvent);
        eventToInput = createTextField("To", this::addEvent);

        return new HBox(
                SPACING,
                eventDescriptionInput,
                eventFromInput,
                eventToInput,
                createButton("Add event", this::addEvent)
        );
    }

    /**
     * Creates the keyword and date search row.
     */
    private HBox createSearchArea() {
        searchInput = createTextField("Find task", this::findTasks);
        dateInput = createTextField(
                "Date: yyyy-mm-dd", this::showTasksOnDate);

        return new HBox(
                SPACING,
                searchInput,
                createButton("Find", this::findTasks),
                dateInput,
                createButton("Show date", this::showTasksOnDate),
                createButton("Show all", this::refreshTaskList)
        );
    }

    /**
     * Creates the message and image section.
     */
    private VBox createMessageArea() {
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(MESSAGE_WIDTH);

        Image image = new Image(
                getClass().getResourceAsStream("/images/carlo.png"));

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setPreserveRatio(true);

        VBox messageArea = new VBox(SPACING, messageLabel, imageView);
        messageArea.setAlignment(Pos.TOP_RIGHT);
        return messageArea;
    }

    /**
     * Creates the task action buttons, including undo.
     *
     * @return the row of task action buttons
     */
    private HBox createActionArea() {
        return new HBox(
                SPACING,
                createButton("Mark done", this::markSelectedTask),
                createButton("Unmark", this::unmarkSelectedTask),
                createButton("Delete", this::deleteSelectedTask),
                createButton("Undo", this::undoLastAction)
        );
    }

    /**
     * Creates a text field that performs an action when Enter is pressed.
     *
     * @param prompt the placeholder text
     * @param action the action to perform
     * @return the configured text field
     */
    private TextField createTextField(String prompt, Runnable action) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setOnAction(event -> action.run());
        return field;
    }

    /**
     * Creates a button that performs an action when clicked.
     *
     * @param text the button label
     * @param action the action to perform
     * @return the configured button
     */
    private Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    /**
     * Adds a todo task using the text entered by the user.
     */
    private void addTodo() {
        try {
            carlo.addTodo(taskInput.getText());

            taskInput.clear();
            messageLabel.setText(
                    "Added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );
            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    /**
     * Marks the selected task as completed.
     */
    private void markSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            messageLabel.setText(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.markTask(selectedIndex);
            messageLabel.setText("YAY! Thank you, next!");
            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    /**
     * Marks the selected task as incomplete.
     */
    private void unmarkSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            messageLabel.setText(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.unmarkTask(selectedIndex);
            messageLabel.setText("Awman... okay, unmarked for now...");
            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    /**
     * Deletes the selected task.
     */
    private void deleteSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            messageLabel.setText(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.deleteTask(selectedIndex);
            messageLabel.setText(
                    "Okay okay, I've removed this task!\n"
                            + "You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );
            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    /**
     * Displays Carlo's usage instructions.
     */
    private void showHelp() {
        messageLabel.setText(HELP_TEXT);
    }

    /**
     * Adds a deadline using the values entered by the user.
     */
    private void addDeadline() {
        try {
            carlo.addDeadline(
                    deadlineDescriptionInput.getText(),
                    deadlineTimeInput.getText()
            );

            deadlineDescriptionInput.clear();
            deadlineTimeInput.clear();

            messageLabel.setText(
                    "Deadline added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );

            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText("Ohno!! " + e.getMessage());
        }
    }

    /**
     * Adds an event using the values entered by the user.
     */
    private void addEvent() {
        try {
            carlo.addEvent(
                    eventDescriptionInput.getText(),
                    eventFromInput.getText(),
                    eventToInput.getText()
            );

            eventDescriptionInput.clear();
            eventFromInput.clear();
            eventToInput.clear();

            messageLabel.setText(
                    "Event added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );

            refreshTaskList();
        } catch (CarloException e) {
            messageLabel.setText("Ohno!! " + e.getMessage());
        }
    }

    /**
     * Displays tasks matching the search keyword.
     */
    private void findTasks() {
        try {
            List<Task> matches = carlo.findTasks(searchInput.getText());

            displayTasks(matches);

            if (matches.isEmpty()) {
                messageLabel.setText(
                        "I couldn't find any matching tasks!"
                );
            } else {
                messageLabel.setText(
                        "Here are the matching tasks in your list!"
                );
            }
        } catch (CarloException e) {
            messageLabel.setText("Ohno!! " + e.getMessage());
        }
    }

    /**
     * Displays tasks occurring on the entered date.
     */
    private void showTasksOnDate() {
        try {
            LocalDate date = CarloDateTime.parseDate(dateInput.getText());
            List<Task> matches = carlo.getTasksOnDate(date);

            displayTasks(matches);

            if (matches.isEmpty()) {
                messageLabel.setText(
                        "Nothing on that day! Free day yay!"
                );
            } else {
                messageLabel.setText(
                        "Here's what's happening on " + date + "!"
                );
            }
        } catch (DateTimeParseException e) {
            messageLabel.setText(
                    "I couldn't understand that date!\nTry today, tomorrow, "
                            + "yesterday,\nor yyyy-mm-dd."
            );
        }
    }

    /**
     * Refreshes the displayed task list using Carlo's current tasks.
     */
    private void refreshTaskList() {
        displayTasks(carlo.getTasks());
    }

    /**
     * Displays the given task objects.
     *
     * @param tasks the tasks to display
     */
    private void displayTasks(List<Task> tasks) {
        taskListView.setItems(FXCollections.observableArrayList(tasks));
    }

    /**
     * Returns the selected task's index in Carlo's full task list.
     *
     * @return the task index, or -1 if no stored task is selected
     */
    private int getSelectedTaskIndex() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            return -1;
        }

        return carlo.getTasks().indexOf(selectedTask);
    }

    /**
     * Undoes the latest task operation and refreshes the displayed list.
     */
    private void undoLastAction() {
        try {
            carlo.undo();
            refreshTaskList();
            messageLabel.setText("Undid the last task operation!");
        } catch (CarloException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
