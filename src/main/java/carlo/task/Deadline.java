package carlo.task;

/**
 * Represents a task that must be completed by a specified time.
 */
public class Deadline extends Task {
    private final String dueTime;

    /**
     * Creates an incomplete deadline.
     *
     * @param description text describing the task
     * @param dueTime the deadline, stored as entered by the user
     */
    public Deadline(String description, String dueTime) {
        super(description);
        this.dueTime = dueTime;
    }

    /**
     * Returns the due time of this deadline.
     *
     * @return the deadline, as entered by the user
     */
    public String getDueTime() {
        return dueTime;
    }

    /**
     * Returns this deadline in the command-line display format.
     *
     * @return the task type, completion status, description, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + dueTime + ")";
    }

    /**
     * Returns this deadline in the on-disk save format.
     *
     * @return the type letter, completion flag, description, and due time, separated by " | "
     */
    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + dueTime;
    }
}