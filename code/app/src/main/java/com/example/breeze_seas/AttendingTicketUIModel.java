package com.example.breeze_seas;

import androidx.annotation.Nullable;

/**
 * UI-facing model for confirmed attending tickets.
 *
 * <p>This remains separate from any eventual Firestore document shape so the repository can map
 * backend data into ticket-specific presentation fields later.
 */
public class AttendingTicketUIModel {

    private final String eventId;
    private final String title;
    private final String dateLabel;
    private final String locationLabel;
    private final String ticketTypeLabel;
    private final String entryNote;
    private final String actionLabel;
    @Nullable
    private final String imageDocId;

    /**
     * Creates a presentation model for one attending ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param locationLabel Human-readable location string shown on the card.
     * @param ticketTypeLabel Summary label describing the ticket type/state.
     * @param entryNote Supporting copy shown under the main ticket details.
     * @param actionLabel Label describing the tap action available on the card.
     */
    public AttendingTicketUIModel(
            String eventId,
            String title,
            String dateLabel,
            String locationLabel,
            String ticketTypeLabel,
            String entryNote,
            String actionLabel
    ) {
        this(eventId, title, dateLabel, locationLabel, ticketTypeLabel, entryNote, actionLabel, null);
    }

    /**
     * Creates a presentation model for one attending ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param locationLabel Human-readable location string shown on the card.
     * @param ticketTypeLabel Summary label describing the ticket type/state.
     * @param entryNote Supporting copy shown under the main ticket details.
     * @param actionLabel Label describing the tap action available on the card.
     * @param imageDocId Optional image document id for the event poster.
     */
    public AttendingTicketUIModel(
            String eventId,
            String title,
            String dateLabel,
            String locationLabel,
            String ticketTypeLabel,
            String entryNote,
            String actionLabel,
            @Nullable String imageDocId
    ) {
        this.eventId = eventId;
        this.title = title;
        this.dateLabel = dateLabel;
        this.locationLabel = locationLabel;
        this.ticketTypeLabel = ticketTypeLabel;
        this.entryNote = entryNote;
        this.actionLabel = actionLabel;
        this.imageDocId = imageDocId;
    }

    /**
     * Returns the Firestore event identifier for the related event.
     *
     * @return Firestore event identifier.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Returns the title shown on the attending ticket card.
     *
     * @return Event title shown on the card.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the date string shown on the attending ticket card.
     *
     * @return Human-readable event date label.
     */
    public String getDateLabel() {
        return dateLabel;
    }

    /**
     * Returns the location string shown on the attending ticket card.
     *
     * @return Human-readable location label.
     */
    public String getLocationLabel() {
        return locationLabel;
    }

    /**
     * Returns the ticket-type label shown on the attending ticket card.
     *
     * @return Ticket-type summary label.
     */
    public String getTicketTypeLabel() {
        return ticketTypeLabel;
    }

    /**
     * Returns the supporting note shown below the main card details.
     *
     * @return Supporting entry note.
     */
    public String getEntryNote() {
        return entryNote;
    }

    /**
     * Returns the label describing the available card action.
     *
     * @return Action label shown on the card.
     */
    public String getActionLabel() {
        return actionLabel;
    }

    /**
     * Returns the optional event poster image document id.
     *
     * @return Firestore image document id, or {@code null} when unavailable.
     */
    @Nullable
    public String getImageDocId() {
        return imageDocId;
    }
}
