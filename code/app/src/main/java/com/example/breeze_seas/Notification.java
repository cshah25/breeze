package com.example.breeze_seas;

import android.graphics.drawable.Drawable;

import com.google.firebase.Timestamp;

public class Notification {

    private NotificationType type;
    private String content;
    private String eventId;
    private Timestamp timestamp;

    public Notification(NotificationType type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }


}
