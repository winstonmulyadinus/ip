package carlo.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import carlo.Carlo;
import carlo.exception.CarloException;
import carlo.task.CarloDateTime;
import carlo.task.Deadline;
import carlo.task.Event;
import carlo.task.Task;
import carlo.task.Todo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextAlignment;
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

        Dates can be given as yyyy-mm-dd or yyyy-mm-dd HHmm.
        """;

    private static final int SPACING = 10;
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 650;
    private static final int IMAGE_SIZE = 120;
    private static final int MESSAGE_WIDTH = 500;

    private static final String MARK_DONE_STYLE =
            "-fx-background-color: #4CAF50; -fx-text-fill: white;";
    private static final String UNMARK_STYLE =
            "-fx-background-color: #FFC107; -fx-text-fill: black;";
    private static final String DELETE_STYLE =
            "-fx-background-color: #F44336; -fx-text-fill: white;";
    private static final String COLUMN_HEADER_STYLE =
            "-fx-font-weight: bold;";
    private static final String SPEECH_BUBBLE_STYLE =
            "-fx-background-color: white; "
                    + "-fx-background-radius: 12; "
                    + "-fx-border-color: #CCCCCC; "
                    + "-fx-border-radius: 12; "
                    + "-fx-border-width: 1; "
                    + "-fx-padding: 10;";
    private static final String SPEECH_BUBBLE_TAIL_STYLE =
            "-fx-fill: white; -fx-stroke: #CCCCCC; -fx-stroke-width: 1;";

    private Carlo carlo;

    private ListView<Task> todoListView;
    private ListView<Task> deadlineListView;
    private ListView<Task> eventListView;

    private ComboBox<TaskFormType> taskTypeSelector;
    private HBox todoRow;
    private HBox deadlineRow;
    private HBox eventRow;

    private TextField taskInput;
    private TextField deadlineDescriptionInput;
    private TextField deadlineTimeInput;

    private TextField eventDescriptionInput;
    private TextField eventFromInput;
    private TextField eventToInput;

    private TextField searchInput;
    private TextField dateInput;

    private Label messageLabel;
    private String lastMessage = GREETING;
    private boolean showingHelp;

    /**
     * Identifies which task-entry form is currently shown, and how it is
     * labelled in the {@link #taskTypeSelector} dropdown.
     */
    private enum TaskFormType {
        TODO("Todo"),
        DEADLINE("Deadline"),
        EVENT("Event");

        private final String label;

        TaskFormType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Starts the Carlo graphical user interface.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        carlo = new Carlo();

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
        root.setCenter(createTaskColumns());
        root.setBottom(new VBox(SPACING, createActionArea()));
        return root;
    }

    /**
     * Creates the input and message sections at the top of the window.
     */
    private BorderPane createTopArea() {
        VBox inputArea = createInputArea();
        VBox messageArea = createMessageArea();

        BorderPane topArea = new BorderPane();
        topArea.setLeft(inputArea);
        topArea.setRight(messageArea);
        BorderPane.setMargin(inputArea, new Insets(SPACING));
        BorderPane.setMargin(messageArea, new Insets(SPACING));
        return topArea;
    }

    /**
     * Creates the left-hand input area: the task type dropdown, the form
     * matching the selected type, and the search/date filter rows.
     */
    private VBox createInputArea() {
        todoRow = createTodoArea();
        deadlineRow = createDeadlineArea();
        eventRow = createEventArea();

        taskTypeSelector = createTaskTypeSelector();
        updateFormVisibility();

        VBox formArea = new VBox(SPACING, todoRow, deadlineRow, eventRow);

        return new VBox(
                SPACING,
                taskTypeSelector,
                formArea,
                new Separator(),
                createSearchArea()
        );
    }

    /**
     * Creates the dropdown used to choose which task form is shown.
     */
    private ComboBox<TaskFormType> createTaskTypeSelector() {
        ComboBox<TaskFormType> selector = new ComboBox<>(
                FXCollections.observableArrayList(TaskFormType.values()));
        selector.setValue(TaskFormType.TODO);
        selector.setOnAction(event -> updateFormVisibility());
        return selector;
    }

    /**
     * Shows only the task form matching the selected task type, hiding the
     * other two.
     */
    private void updateFormVisibility() {
        TaskFormType selected = taskTypeSelector.getValue();
        setRowVisible(todoRow, selected == TaskFormType.TODO);
        setRowVisible(deadlineRow, selected == TaskFormType.DEADLINE);
        setRowVisible(eventRow, selected == TaskFormType.EVENT);
    }

    /**
     * Shows or hides a form row, excluding it from layout when hidden.
     */
    private void setRowVisible(HBox row, boolean visible) {
        row.setVisible(visible);
        row.setManaged(visible);
    }

    /**
     * Creates the todo input row.
     */
    private HBox createTodoArea() {
        taskInput = createTextField("Enter a todo", this::addTodo);

        return new HBox(
                SPACING,
                taskInput,
                createButton("Add", this::addTodo)
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
     * Creates the keyword search row and, below it, the date filter row.
     */
    private VBox createSearchArea() {
        Label heading = new Label("Find tasks");
        heading.setStyle(COLUMN_HEADER_STYLE);

        searchInput = createTextField("Find task", this::findTasks);
        HBox searchRow = new HBox(
                SPACING,
                searchInput,
                createButton("Find", this::findTasks)
        );

        dateInput = createTextField("Date: yyyy-mm-dd", this::showTasksOnDate);
        HBox dateRow = new HBox(
                SPACING,
                dateInput,
                createButton("Show date", this::showTasksOnDate),
                createButton("Show all", this::refreshTaskList)
        );

        return new VBox(SPACING, heading, searchRow, dateRow);
    }

    /**
     * Creates the message, image, and help section, with the help button
     * and its collapsible text sitting underneath Carlo's image.
     */
    private VBox createMessageArea() {
        messageLabel = new Label(GREETING);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(MESSAGE_WIDTH);
        messageLabel.setAlignment(Pos.CENTER_RIGHT);
        messageLabel.setTextAlignment(TextAlignment.RIGHT);
        messageLabel.setStyle(SPEECH_BUBBLE_STYLE);

        Polygon speechBubbleTail = new Polygon(0.0, 0.0, 16.0, 0.0, 16.0, 14.0);
        speechBubbleTail.setStyle(SPEECH_BUBBLE_TAIL_STYLE);
        VBox.setMargin(speechBubbleTail, new Insets(-1, 24, 0, 0));

        VBox speechBubble = new VBox(messageLabel, speechBubbleTail);
        speechBubble.setAlignment(Pos.CENTER_RIGHT);

        Image image = new Image(
                getClass().getResourceAsStream("/images/carlo.png"));

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setPreserveRatio(true);

        Button helpButton = createButton("Help", this::showHelp);

        VBox messageArea = new VBox(
                SPACING, speechBubble, imageView, helpButton);
        messageArea.setAlignment(Pos.TOP_RIGHT);
        return messageArea;
    }

    /**
     * Creates the three task columns (Todos, Deadlines, Events) and wires
     * up their list views so that selecting a task in one clears the
     * selection in the other two.
     */
    private HBox createTaskColumns() {
        todoListView = new ListView<>();
        deadlineListView = new ListView<>();
        eventListView = new ListView<>();

        VBox todoColumn = createTaskColumn("Todos", todoListView);
        VBox deadlineColumn = createTaskColumn("Deadlines", deadlineListView);
        VBox eventColumn = createTaskColumn("Events", eventListView);

        HBox.setHgrow(todoColumn, Priority.ALWAYS);
        HBox.setHgrow(deadlineColumn, Priority.ALWAYS);
        HBox.setHgrow(eventColumn, Priority.ALWAYS);

        setupSelectionClearing();

        HBox columns = new HBox(SPACING, todoColumn, deadlineColumn, eventColumn);
        columns.setPadding(new Insets(SPACING));
        return columns;
    }

    /**
     * Creates a single labelled task column.
     *
     * @param title the column heading
     * @param listView the list view to display beneath the heading
     * @return the assembled column
     */
    private VBox createTaskColumn(String title, ListView<Task> listView) {
        Label header = new Label(title);
        header.setStyle(COLUMN_HEADER_STYLE);

        applyTaskCellFactory(listView);

        VBox column = new VBox(5, header, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return column;
    }

    /**
     * Configures a task column's list view to display each task without
     * its {@code [T]}/{@code [D]}/{@code [E]} type marker, since the
     * column it appears in already conveys that.
     *
     * @param listView the list view to configure
     */
    private void applyTaskCellFactory(ListView<Task> listView) {
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                setText(empty || task == null ? null : formatTaskForDisplay(task));
            }
        });
    }

    /**
     * Formats a task for display within its column, omitting the type
     * marker that {@link Task#toString()} would normally include.
     *
     * @param task the task to format
     * @return the status icon, description, and any date/time fields
     */
    private String formatTaskForDisplay(Task task) {
        String base = "[" + task.getStatusIcon() + "] " + task.getDescription();

        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return base + " (by: " + deadline.getDueTime() + ")";
        } else if (task instanceof Event) {
            Event event = (Event) task;
            return base + " (from: " + event.getStartTime()
                    + " to: " + event.getEndTime() + ")";
        }
        return base;
    }

    /**
     * Ensures that selecting a task in any one column clears the selection
     * in the other two, so at most one task is selected overall.
     */
    private void setupSelectionClearing() {
        todoListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                deadlineListView.getSelectionModel().clearSelection();
                eventListView.getSelectionModel().clearSelection();
            }
        });
        deadlineListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                todoListView.getSelectionModel().clearSelection();
                eventListView.getSelectionModel().clearSelection();
            }
        });
        eventListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                todoListView.getSelectionModel().clearSelection();
                deadlineListView.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * Creates the buttons for modifying the selected task, coloured by
     * what they do: green for marking done, yellow for unmarking, and red
     * for deleting.
     */
    private HBox createActionArea() {
        Button markDoneButton = createButton("Mark done", this::markSelectedTask);
        markDoneButton.setStyle(MARK_DONE_STYLE);

        Button unmarkButton = createButton("Unmark", this::unmarkSelectedTask);
        unmarkButton.setStyle(UNMARK_STYLE);

        Button deleteButton = createButton("Delete", this::deleteSelectedTask);
        deleteButton.setStyle(DELETE_STYLE);

        return new HBox(SPACING, markDoneButton, unmarkButton, deleteButton);
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
     * Sets the text shown in the speech bubble, exiting help mode if it
     * was active so the next help click shows instructions rather than
     * restoring stale text.
     *
     * @param text the message to display
     */
    private void setMessage(String text) {
        showingHelp = false;
        lastMessage = text;
        messageLabel.setText(text);
    }

    /**
     * Adds a todo task using the text entered by the user.
     */
    private void addTodo() {
        try {
            carlo.addTodo(taskInput.getText());

            taskInput.clear();
            setMessage(
                    "Added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );
            refreshTaskList();
        } catch (CarloException e) {
            setMessage(e.getMessage());
        }
    }

    /**
     * Marks the selected task as completed.
     */
    private void markSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            setMessage(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.markTask(selectedIndex);
            setMessage("YAY! Thank you, next!");
            refreshTaskList();
        } catch (CarloException e) {
            setMessage(e.getMessage());
        }
    }

    /**
     * Marks the selected task as incomplete.
     */
    private void unmarkSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            setMessage(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.unmarkTask(selectedIndex);
            setMessage("Awman... okay, unmarked for now...");
            refreshTaskList();
        } catch (CarloException e) {
            setMessage(e.getMessage());
        }
    }

    /**
     * Deletes the selected task.
     */
    private void deleteSelectedTask() {
        int selectedIndex = getSelectedTaskIndex();

        if (selectedIndex == -1) {
            setMessage(
                    "Hmm... please select a task first!"
            );
            return;
        }

        try {
            carlo.deleteTask(selectedIndex);
            setMessage(
                    "Okay okay, I've removed this task!\n"
                            + "You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );
            refreshTaskList();
        } catch (CarloException e) {
            setMessage(e.getMessage());
        }
    }

    /**
     * Toggles between showing Carlo's usage instructions in the speech
     * bubble and restoring whatever message was shown before.
     */
    private void showHelp() {
        if (showingHelp) {
            messageLabel.setText(lastMessage);
        } else {
            lastMessage = messageLabel.getText();
            messageLabel.setText(HELP_TEXT);
        }
        showingHelp = !showingHelp;
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

            setMessage(
                    "Deadline added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );

            refreshTaskList();
        } catch (CarloException e) {
            setMessage("Ohno!! " + e.getMessage());
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

            setMessage(
                    "Event added! You now have "
                            + carlo.getTasks().size()
                            + " tasks in your list!"
            );

            refreshTaskList();
        } catch (CarloException e) {
            setMessage("Ohno!! " + e.getMessage());
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
                setMessage(
                        "I couldn't find any matching tasks!"
                );
            } else {
                setMessage(
                        "Here are the matching tasks in your list!"
                );
            }
        } catch (CarloException e) {
            setMessage("Ohno!! " + e.getMessage());
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
                setMessage(
                        "Nothing on that day! Free day yay!"
                );
            } else {
                setMessage(
                        "Here's what's happening on " + date + "!"
                );
            }
        } catch (DateTimeParseException e) {
            setMessage(
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
     * Displays the given task objects, sorting them into the Todos,
     * Deadlines, and Events columns by their type.
     *
     * @param tasks the tasks to display
     */
    private void displayTasks(List<Task> tasks) {
        List<Task> todos = new ArrayList<>();
        List<Task> deadlines = new ArrayList<>();
        List<Task> events = new ArrayList<>();

        for (Task task : tasks) {
            if (task instanceof Todo) {
                todos.add(task);
            } else if (task instanceof Deadline) {
                deadlines.add(task);
            } else if (task instanceof Event) {
                events.add(task);
            }
        }

        todoListView.setItems(FXCollections.observableArrayList(todos));
        deadlineListView.setItems(FXCollections.observableArrayList(deadlines));
        eventListView.setItems(FXCollections.observableArrayList(events));
    }

    /**
     * Returns the selected task's index in Carlo's full task list, checking
     * each of the three columns in turn since only one can have a
     * selection at a time.
     *
     * @return the task index, or -1 if no stored task is selected
     */
    private int getSelectedTaskIndex() {
        Task selectedTask = todoListView.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            selectedTask = deadlineListView.getSelectionModel().getSelectedItem();
        }
        if (selectedTask == null) {
            selectedTask = eventListView.getSelectionModel().getSelectedItem();
        }
        if (selectedTask == null) {
            return -1;
        }

        return carlo.getTasks().indexOf(selectedTask);
    }
}
