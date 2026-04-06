package com.example.breeze_seas;

import androidx.annotation.Nullable;

/**
 * TicketUIModel represents a single event "ticket" entry displayed in the Tickets feature UI.
 *
 * <p>Role:
 * - Lightweight view model used by RecyclerView adapters to render ticket cards.
 * - Presentation model mapped from Firestore-backed ticket data.
 *
 * <p>Outstanding:
 * - Expand with additional display fields only if the Tickets UI needs them.
 */
public class TicketUIModel {

    /**
     * Presentation states used by the Active Tickets UI.
     */
    public enum Status {
        PENDING,        // WAITING
        BACKUP,         // Not selected (backup pool)
        ACTION_REQUIRED // INVITED
    }

    private final String eventId;
    private final String title;
    private final String dateLabel;
    private final Status status;
    private final boolean privateEvent;
    private final boolean inboxDecisionRequired;
    @Nullable
    private final String imageDocId;

    /**
     * Creates a presentation model for one active ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param status Active-ticket state that drives the chip and click behavior.
     */
    public TicketUIModel(String eventId, String title, String dateLabel, Status status) {
        this(eventId, title, dateLabel, status, false, false, null);
    }

    /**
     * Creates a presentation model for one active ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param status Active-ticket state that drives the chip and click behavior.
     * @param privateEvent Whether the related event is private.
     */
    public TicketUIModel(String eventId, String title, String dateLabel, Status status, boolean privateEvent) {
        this(eventId, title, dateLabel, status, privateEvent, false, null);
    }

    /**
     * Creates a presentation model for one active ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param status Active-ticket state that drives the chip and click behavior.
     * @param privateEvent Whether the related event is private.
     * @param inboxDecisionRequired Whether the entrant must go through Notifications to respond.
     */
    public TicketUIModel(
            String eventId,
            String title,
            String dateLabel,
            Status status,
            boolean privateEvent,
            boolean inboxDecisionRequired
    ) {
        this(eventId, title, dateLabel, status, privateEvent, inboxDecisionRequired, null);
    }

    /**
     * Creates a presentation model for one active ticket card.
     *
     * @param eventId Firestore event identifier for the related event.
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param status Active-ticket state that drives the chip and click behavior.
     * @param privateEvent Whether the related event is private.
     * @param inboxDecisionRequired Whether the entrant must go through Notifications to respond.
     * @param imageDocId Optional image document id for the event poster.
     */
    public TicketUIModel(
            String eventId,
            String title,
            String dateLabel,
            Status status,
            boolean privateEvent,
            boolean inboxDecisionRequired,
            @Nullable String imageDocId
    ) {
        this.eventId = eventId;
        this.title = title;
        this.dateLabel = dateLabel;
        this.status = status;
        this.privateEvent = privateEvent;
        this.inboxDecisionRequired = inboxDecisionRequired;
        this.imageDocId = imageDocId;
    }

    /** @return Firestore event id for the related event document. */
    public String getEventId() { return eventId; }

    /** @return Event title shown on the ticket card. */
    public String getTitle() { return title; }

    /** @return Human-readable date label (e.g., "Mon, Mar 4 • 6:00 PM"). */
    public String getDateLabel() { return dateLabel; }

    /** @return Ticket status used to drive chip text and click behavior. */
    public Status getStatus() { return status; }

    /** @return {@code true} when the related event is private. */
    public boolean isPrivateEvent() { return privateEvent; }

    /** @return {@code true} when this card is still a private invite awaiting accept/decline. */
    public boolean isPrivateInvitePending() { return inboxDecisionRequired; }

    /** @return {@code true} when this card is still a private invite awaiting accept/decline. */
    public boolean requiresInboxDecision() { return isPrivateInvitePending(); }

    /** @return Optional image document id for the related event poster. */
    @Nullable
    public String getImageDocId() { return imageDocId; }
}
