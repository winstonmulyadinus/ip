package carlo.ui;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


/**
 * Provides the JavaFX graphical user interface for Carlo.
 */
public class MainWindow extends Application {
    private Carlo carlo;
    private ListView<String> taskListView;
    private TextField taskInput;
    private TextField deadlineDescriptionInput;
    private TextField deadlineTimeInput;

    private TextField eventDescriptionInput;
    private TextField eventFromInput;
    private TextField eventToInput;

    private TextField searchInput;
    private TextField dateInput;

    private Label messageLabel;

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

        Dates can be given as yyyy-mm-dd or yyyy-mm-dd HHmm.
        """;

    /**
     * Starts the Carlo graphical user interface.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        carlo = new Carlo();

        taskListView = new ListView<>();

        taskInput = new TextField();
        taskInput.setPromptText("Enter a todo");
        taskInput.setOnAction(event -> addTodo());

        deadlineDescriptionInput = new TextField();
        deadlineDescriptionInput.setPromptText("Deadline description");
        deadlineDescriptionInput.setOnAction(event -> addDeadline());

        deadlineTimeInput = new TextField();
        deadlineTimeInput.setPromptText("Due date/time");
        deadlineTimeInput.setOnAction(event -> addDeadline());

        Button addDeadlineButton = new Button("Add deadline");
        addDeadlineButton.setOnAction(event -> addDeadline());

        eventDescriptionInput = new TextField();
        eventDescriptionInput.setPromptText("Event description");
        eventDescriptionInput.setOnAction(event -> addEvent());

        eventFromInput = new TextField();
        eventFromInput.setPromptText("From");
        eventFromInput.setOnAction(event -> addEvent());

        eventToInput = new TextField();
        eventToInput.setPromptText("To");
        eventToInput.setOnAction(event -> addEvent());

        Button addEventButton = new Button("Add event");
        addEventButton.setOnAction(event -> addEvent());

        searchInput = new TextField();
        searchInput.setPromptText("Find task");

        Button searchButton = new Button("Find");
        searchButton.setOnAction(event -> findTasks());

        searchInput.setOnAction(event -> findTasks());

        dateInput = new TextField();
        dateInput.setPromptText("Date: yyyy-mm-dd");

        Button dateButton = new Button("Show date");
        dateButton.setOnAction(event -> showTasksOnDate());

        dateInput.setOnAction(event -> showTasksOnDate());

        Button showAllButton = new Button("Show all");
        showAllButton.setOnAction(event -> refreshTaskList());

        Image image = new Image(
                getClass().getResourceAsStream("/images/carlo.png")
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);
        Label welcomeLabel = new Label(GREETING);

        Button addButton = new Button("Add");
        Button markButton = new Button("Mark done");
        Button unmarkButton = new Button("Unmark");
        Button deleteButton = new Button("Delete");
        Button helpButton = new Button("Help");
        helpButton.setOnAction(event -> showHelp());

        addButton.setOnAction(event -> addTodo());
        markButton.setOnAction(event -> markSelectedTask());
        unmarkButton.setOnAction(event -> unmarkSelectedTask());
        deleteButton.setOnAction(event -> deleteSelectedTask());

        HBox inputArea = new HBox(
                10,
                taskInput,
                addButton,
                helpButton
        );


        HBox deadlineArea = new HBox(
                10,
                deadlineDescriptionInput,
                deadlineTimeInput,
                addDeadlineButton
        );

        HBox eventArea = new HBox(
                10,
                eventDescriptionInput,
                eventFromInput,
                eventToInput,
                addEventButton
        );

        HBox searchArea = new HBox(
                10,
                searchInput,
                searchButton,
                dateInput,
                dateButton,
                showAllButton
        );

        VBox leftTopArea = new VBox(
                10,
                welcomeLabel,
                inputArea,
                deadlineArea,
                eventArea,
                searchArea
        );

        VBox rightTopArea = new VBox(
                10,
                messageLabel,
                imageView
        );

        rightTopArea.setAlignment(Pos.TOP_RIGHT);

        BorderPane topArea = new BorderPane();
        topArea.setLeft(leftTopArea);
        topArea.setRight(rightTopArea);

        BorderPane.setMargin(rightTopArea, new Insets(10));
        HBox actionArea = new HBox(
                10,
                markButton,
                unmarkButton,
                deleteButton
        );

        VBox bottomArea = new VBox(
                10,
                actionArea
        );

        BorderPane root = new BorderPane();
        root.setTop(topArea);
        root.setCenter(taskListView);
        root.setBottom(bottomArea);

        refreshTaskList();

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Carlo");
        stage.setScene(scene);
        stage.show();
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
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

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
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

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
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

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

            taskListView.setItems(FXCollections.observableArrayList(
                    matches.stream()
                            .map(Task::toString)
                            .toList()
            ));

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

            taskListView.setItems(FXCollections.observableArrayList(
                    matches.stream()
                            .map(Task::toString)
                            .toList()
            ));

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
        taskListView.setItems(FXCollections.observableArrayList(
                carlo.getTasks()
                        .stream()
                        .map(Task::toString)
                        .toList()
        ));
    }
}