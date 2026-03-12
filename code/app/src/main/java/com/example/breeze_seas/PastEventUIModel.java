package com.example.breeze_seas;

/**
 * UI-facing model for archived past event history.
 *
 * <p>This is intentionally presentation-focused and does not define a Firestore schema.
 */
public class PastEventUIModel {

    private final String title;
    private final String dateLabel;
    private final String locationLabel;
    private final String statusLabel;
    private final String detailLabel;
    private final int iconResId;

    /**
     * Creates a presentation model for one archived ticket-history card.
     *
     * @param title Event title shown on the card.
     * @param dateLabel Human-readable date string shown on the card.
     * @param locationLabel Human-readable location string shown on the card.
     * @param statusLabel Short status text shown in the chip.
     * @param detailLabel Supporting explanation for why the ticket is archived.
     * @param iconResId Drawable resource used for the history icon.
     */
    public PastEventUIModel(
            String title,
            String dateLabel,
            String locationLabel,
            String statusLabel,
            String detailLabel,
            int iconResId
    ) {
        this.title = title;
        this.dateLabel = dateLabel;
        this.locationLabel = locationLabel;
        this.statusLabel = statusLabel;
        this.detailLabel = detailLabel;
        this.iconResId = iconResId;
    }

    /**
     * Returns the event title shown on the card.
     *
     * @return Event title shown on the history card.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the date string shown on the card.
     *
     * @return Human-readable event date label.
     */
    public String getDateLabel() {
        return dateLabel;
    }

    /**
     * Returns the location string shown on the card.
     *
     * @return Human-readable location label.
     */
    public String getLocationLabel() {
        return locationLabel;
    }

    /**
     * Returns the short status chip label for the archived ticket.
     *
     * @return Archived-ticket status label.
     */
    public String getStatusLabel() {
        return statusLabel;
    }

    /**
     * Returns the supporting explanation shown under the card metadata.
     *
     * @return Detail label explaining the archived state.
     */
    public String getDetailLabel() {
        return detailLabel;
    }

    /**
     * Returns the drawable used for the archived ticket icon.
     *
     * @return Drawable resource identifier.
     */
    public int getIconResId() {
        return iconResId;
    }
}
