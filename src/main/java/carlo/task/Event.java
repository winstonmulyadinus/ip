package carlo.task;

/**
 * Represents a task that starts and ends at specified times.
 */
public class Event extends Task {
    private final String startTime;
    private final String endTime;

    /**
     * Creates an incomplete event.
     *
     * @param description text describing the event
     * @param startTime the event start time, stored as entered by the user
     * @param endTime the event end time, stored as entered by the user
     */
    public Event(String description, String startTime, String endTime) {
        super(description);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the start time of this event.
     *
     * @return the start time, as entered by the user
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this event.
     *
     * @return the end time, as entered by the user
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Returns this event in the command-line display format.
     *
     * @return the task type, completion status, description, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + startTime + " to: " + endTime + ")";
    }

    /**
     * Returns this event in the on-disk save format.
     *
     * @return the type letter, completion flag, description, start time, and
     *         end time, separated by " | "
     */
    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat() + " | " + startTime + " | " + endTime;
    }
}