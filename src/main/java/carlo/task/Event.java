package carlo.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a task that starts and ends at specified times.
 */
public class Event extends Task {
    private final CarloDateTime startTime;
    private final CarloDateTime endTime;

    /**
     * Creates an incomplete event.
     *
     * <p>{@code startTime} and {@code endTime} are each parsed as a date
     * (e.g. {@code 2019-12-02}) or a date and time (e.g.
     * {@code 2019-12-02 1800}) where possible; text that does not match
     * either format, such as {@code "today"}, is kept as entered.
     *
     * @param description text describing the event
     * @param startTime the event start time, as entered by the user
     * @param endTime the event end time, as entered by the user
     */
    public Event(String description, String startTime, String endTime) {
        super(description);
        this.startTime = CarloDateTime.parse(startTime);
        this.endTime = CarloDateTime.parse(endTime);
    }

    /**
     * Returns the start time of this event as display text.
     *
     * @return the start time, formatted for display
     */
    public String getStartTime() {
        return startTime.toDisplayString();
    }

    /**
     * Returns the end time of this event as display text.
     *
     * @return the end time, formatted for display
     */
    public String getEndTime() {
        return endTime.toDisplayString();
    }

    /**
     * Returns the parsed start date/time of this event.
     *
     * @return the start date/time, which may hold free-form text if it could not be parsed
     */
    public CarloDateTime getStartDateTime() {
        return startTime;
    }

    /**
     * Returns the parsed end date/time of this event.
     *
     * @return the end date/time, which may hold free-form text if it could not be parsed
     */
    public CarloDateTime getEndDateTime() {
        return endTime;
    }

    /**
     * Returns whether this event occurs on the given calendar date, i.e.
     * the date falls within the event's start and end dates (inclusive).
     *
     * <p>If only one of the start/end times could be parsed, this checks
     * against that one date instead. If neither could be parsed, this
     * returns {@code false}.
     *
     * @param date the date to check
     * @return {@code true} if the event occurs on {@code date}
     */
    public boolean occursOn(LocalDate date) {
        LocalDateTime start = startTime.getDateTime();
        LocalDateTime end = endTime.getDateTime();

        if (start != null && end != null) {
            return !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate());
        } else if (start != null) {
            return start.toLocalDate().equals(date);
        } else if (end != null) {
            return end.toLocalDate().equals(date);
        }
        return false;
    }

    /**
     * Returns this event in the command-line display format.
     *
     * @return the task type, completion status, description, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + startTime.toDisplayString()
                + " to: " + endTime.toDisplayString() + ")";
    }

    /**
     * Returns this event in the on-disk save format.
     *
     * @return the type letter, completion flag, description, start time, and
     *         end time, separated by " | "
     */
    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat() + " | " + startTime.toFileFormat() + " | " + endTime.toFileFormat();
    }
}