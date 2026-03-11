package com.example.breeze_seas;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationService {

    private final FirebaseFirestore db;
    private final CollectionReference notificationsRef;
    Notification notification;

    public NotificationService(Notification notification) {
        this.db = DBConnector.getDb();
        this.notificationsRef = db.collection("notifications");
        this.notification = notification;
    }


}
