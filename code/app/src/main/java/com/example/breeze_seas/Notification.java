package com.example.breeze_seas;

import android.graphics.drawable.Drawable;

import com.google.firebase.Timestamp;

public class Notification {

    private String notificationId;
    private NotificationType type;
    private String content; // should be an empty string if  not an announcement
    private String eventId;
    private Timestamp timestamp;

    public Notification() {
        this.notificationId = null;
        this.type = null;
        this.content = null;
        this.eventId = null;
        this.timestamp = null;
    }

    public Notification(String notificationId, NotificationType type, String content, String eventId, Timestamp timestamp) {
        this.notificationId = notificationId;
        this.type = type;
        this.content = content;
        this.eventId = eventId;
        this.timestamp = timestamp;
    }

    public Notification(NotificationType type, String content) {
        this.notificationId = null;
        this.type = type;
        this.content = content;
        this.eventId = null;
        this.timestamp = null;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDisplayMessage() {
        switch (type) {
            case WIN:
                return "Congratulations! You won the lottery for " + content + "!";
            case LOSS:
                return "We're sorry, but you were not selected for " + content + ".";
            case ANNOUNCEMENT_SELECTED:
            case ANNOUNCEMENT_WAITLIST:
            case ANNOUNCEMENT_CANCELLED:
                return content;
            default:
                return content;
        }
    }




}
