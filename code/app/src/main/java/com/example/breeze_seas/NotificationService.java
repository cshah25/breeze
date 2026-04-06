package com.example.breeze_seas;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Handles sending and fetching of notifications to the database.
 *
 * TODO: Add push notifications
 */
public class NotificationService {

    private final FirebaseFirestore db;
    private final CollectionReference notificationsRef;
    private ListenerRegistration notificationsListener;

    public NotificationService() {
        this.db = DBConnector.getDb();
        this.notificationsRef = db.collection("notifications");
    }

    /**
     * Returns a fresh notification document reference for callers that need to include
     * a notification write inside a larger Firestore transaction.
     *
     * @return Empty notification document reference in the shared notifications collection.
     */
    @NonNull
    public DocumentReference newNotificationDocument() {
        return notificationsRef.document();
    }

    /**
     * Writes the notification to the database.
     *
     * @param notification the notification that is sent to the database.
     * */
    public Task<Void> sendNotification(Notification notification) {
        Task<Void> writeTask = newNotificationDocument()
                .set(buildNotificationData(notification), SetOptions.merge());
        writeTask.addOnSuccessListener(aVoid ->
                        Log.d("DB_UPDATE", "User create/update successful"))
                .addOnFailureListener(e ->
                        Log.e("DB_UPDATE", "User create/update failed", e));

        return writeTask;
    }

    /**
     * Builds the shared Firestore payload used for notification documents.
     *
     * @param notification Notification being serialized for Firestore.
     * @return Map matching the existing notification collection schema.
     */
    @NonNull
    public Map<String, Object> buildNotificationData(@NonNull Notification notification) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", notification.getType() == null ? null : notification.getType().name());
        notificationData.put("content", notification.getContent());
        notificationData.put("eventId", notification.getEventId());
        notificationData.put("eventName", notification.getEventName());
        notificationData.put("userId", notification.getUserId());
        notificationData.put("sentAt", FieldValue.serverTimestamp());
        notificationData.put("isSeen", notification.isSeen());
        return notificationData;
    }

    /**
     * Writes the notification to a caller-provided document reference.
     *
     * @param notification Notification being serialized.
     * @param documentReference Destination notification document.
     * @return Firestore task for the write.
     */
    public Task<Void> sendNotification(@NonNull Notification notification,
                                       @NonNull DocumentReference documentReference) {
        Task<Void> writeTask = documentReference
                .set(buildNotificationData(notification), SetOptions.merge());
        writeTask.addOnSuccessListener(aVoid ->
                        Log.d("DB_UPDATE", "User create/update successful"))
                .addOnFailureListener(e ->
                        Log.e("DB_UPDATE", "User create/update failed", e));

        return writeTask;
    }

    /**
     * Interface definition for a callback to be invoked when user data is loaded or fails.
     */
    public interface OnNotificationLoadedListener {
        void onNotificationLoaded(List<Notification> notifications);
        void onError(Exception e);
    }

    /**
     * Gets the notifications that were meant for a specific user from the database.
     *
     * @param userId This user id is for filtering in the query.
     * @param listener this makes sure that data is actually received by the program.
     * */
    public void getNotifications(String userId, OnNotificationLoadedListener listener ) {
        notificationsRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = mapNotification(doc);
                        if (notification != null) {
                            notifications.add(notification);
                        }
                    }
                    sortNotifications(notifications);
                    listener.onNotificationLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB_ERROR", "Error fetching notifications", e);
                    listener.onError(e);
                });
    }

    /**
     * Get all notifications
     *
     * @param listener Callback to handle the retrieved data or errors.
     */
    public void getAllNotifications(OnNotificationLoadedListener listener) {
        notificationsRef
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = mapNotification(doc);
                        if (notification != null) {
                            notifications.add(notification);
                        }
                    }
                    listener.onNotificationLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e("DB_ERROR", "Error fetching all notifications", e);
                    listener.onError(e);
                });
    }

    /**
     * Starts a realtime listener for one user's inbox.
     *
     * @param userId Device identifier for the current user.
     * @param listener Callback receiving the current inbox snapshot.
     */
    public void startNotificationsListen(String userId, OnNotificationLoadedListener listener) {
        stopNotificationsListen();
        notificationsListener = notificationsRef
                .whereEqualTo("userId", userId)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("DB_ERROR", "Realtime notification listen failed", error);
                        listener.onError(error);
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Notification notification = mapNotification(doc);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                    }
                    sortNotifications(notifications);
                    listener.onNotificationLoaded(notifications);
                });
    }

    /**
     * Stops the active realtime inbox listener.
     */
    public void stopNotificationsListen() {
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }

    /**
     * Marks one notification as seen.
     *
     * @param notificationId Notification document id.
     * @return Firestore task for the update.
     */
    public Task<Void> markNotificationSeen(String notificationId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isSeen", true);
        return notificationsRef.document(notificationId)
                .set(updates, SetOptions.merge());
    }

    @Nullable
    private Notification mapNotification(@NonNull QueryDocumentSnapshot doc) {
        Object rawTypeValue = doc.get("type");
        NotificationType type = parseNotificationType(rawTypeValue == null ? null : rawTypeValue.toString());
        if (type == null) {
            Log.w("DB_ERROR", "Skipping notification with unrecognised type: " + doc.getId());
            return null;
        }

        Notification notification = new Notification();
        notification.setNotificationId(doc.getId());
        notification.setType(type);
        notification.setContent(doc.getString("content"));
        notification.setEventId(doc.getString("eventId"));
        notification.setEventName(doc.getString("eventName"));
        notification.setUserId(doc.getString("userId"));
        notification.setSentAt(doc.getTimestamp("sentAt"));
        notification.setSeen(Boolean.TRUE.equals(doc.getBoolean("isSeen")));
        return notification;
    }

    @Nullable
    private NotificationType parseNotificationType(@Nullable String rawType) {
        if (rawType == null || rawType.trim().isEmpty()) {
            return null;
        }

        try {
            return NotificationType.valueOf(rawType.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void sortNotifications(@NonNull List<Notification> notifications) {
        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification first, Notification second) {
                if (first.getSentAt() == null && second.getSentAt() == null) {
                    return 0;
                }
                if (first.getSentAt() == null) {
                    return 1;
                }
                if (second.getSentAt() == null) {
                    return -1;
                }
                return second.getSentAt().compareTo(first.getSentAt());
            }
        });
    }
}
