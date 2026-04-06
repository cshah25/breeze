package com.example.breeze_seas;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment for users to see their notifications.
 */
public class NotificationFragment extends Fragment {

    private final UserDB userDBInstance = new UserDB();
    private final NotificationService notificationService = new NotificationService();
    private final List<Notification> notifications = new ArrayList<>();

    private User currentUser;
    private RecyclerView notificationsRecycler;
    private LinearLayout optOutStateLayout;
    private LinearLayout emptyStateLayout;
    private NotificationEntryAdapter adapter;
    private View rootView;
    @Nullable
    private String currentDeviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications_inbox, container, false);
        rootView = view;

        adapter = new NotificationEntryAdapter(notifications, this::onNotificationClicked);
        notificationsRecycler = view.findViewById(R.id.notifications_recycler);
        notificationsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecycler.setAdapter(adapter);
        optOutStateLayout = view.findViewById(R.id.notifications_opt_out_state);
        emptyStateLayout = view.findViewById(R.id.notifications_empty_state);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String notificationUserId = resolveNotificationUserId();
        if (currentUser != null
                && currentUser.notificationEnabled()
                && notificationUserId != null) {
            startNotificationsListen(notificationUserId);
        }
    }

    @Override
    public void onStop() {
        notificationService.stopNotificationsListen();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        rootView = null;
        notificationsRecycler = null;
        optOutStateLayout = null;
        emptyStateLayout = null;
        adapter = null;
        super.onDestroyView();
    }

    private void onNotificationClicked(Notification notification) {
        if (!isAdded()) {
            return;
        }

        if (notification.getType() == NotificationType.PRIVATE_EVENT_INVITE) {
            if (notification.isSeen()) {
                return;
            }
            showPrivateEventInviteDialog(notification);
            return;
        }

        if (notification.getType() == NotificationType.CO_ORG_INVITE) {
            showCoOrganizerInviteDialog(notification);
            return;
        }

        if (notification.getType() == NotificationType.WIN) {
            markNotificationSeenLocally(notification);
            return;
        }

        if (notification.getType() == NotificationType.LOSS) {
            markNotificationSeenLocally(notification);
            return;
        }

        if (!notification.isSeen()) {
            markNotificationSeenLocally(notification);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionViewModel viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        viewModel.getAndroidID().observe(getViewLifecycleOwner(), deviceId -> {
            if (deviceId != null) {
                currentDeviceId = deviceId;
                Log.d("Breeze", "Observed ID: " + deviceId);
                fetchUserData(deviceId);
            }
        });
    }

    /**
     * Fetches user data and displays the notifications.
     *
     * @param deviceId Device ID of the logged-in user.
     */
    private void fetchUserData(String deviceId) {
        userDBInstance.getUser(deviceId, new UserDB.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;

                if (currentUser == null) {
                    renderNotifications(new ArrayList<>());
                    return;
                }

                if (currentUser.notificationEnabled()) {
                    if (optOutStateLayout != null) {
                        optOutStateLayout.setVisibility(GONE);
                    }
                    String notificationUserId = resolveNotificationUserId();
                    if (notificationUserId != null) {
                        startNotificationsListen(notificationUserId);
                    } else {
                        renderNotifications(new ArrayList<>());
                    }
                } else {
                    notificationService.stopNotificationsListen();
                    if (notificationsRecycler != null) {
                        notificationsRecycler.setVisibility(GONE);
                    }
                    if (emptyStateLayout != null) {
                        emptyStateLayout.setVisibility(GONE);
                    }
                    if (optOutStateLayout != null) {
                        optOutStateLayout.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("DB_ERROR", "Error fetching user by deviceId", e);
            }
        });
    }

    private void startNotificationsListen(@NonNull String userId) {
        notificationService.startNotificationsListen(
                userId,
                new NotificationService.OnNotificationLoadedListener() {
                    @Override
                    public void onNotificationLoaded(List<Notification> fetchedNotifications) {
                        renderNotifications(fetchedNotifications);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("DB_ERROR", "Error fetching notifications", e);
                    }
                }
        );
    }

    @Nullable
    private String resolveNotificationUserId() {
        if (currentUser != null
                && currentUser.getDeviceId() != null
                && !currentUser.getDeviceId().trim().isEmpty()) {
            return currentUser.getDeviceId().trim();
        }
        if (currentDeviceId != null && !currentDeviceId.trim().isEmpty()) {
            return currentDeviceId.trim();
        }
        return null;
    }

    private void renderNotifications(@NonNull List<Notification> fetchedNotifications) {
        notifications.clear();
        notifications.addAll(fetchedNotifications);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        boolean hasNotifications = !notifications.isEmpty();
        if (notificationsRecycler != null) {
            notificationsRecycler.setVisibility(hasNotifications ? VISIBLE : GONE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(hasNotifications ? GONE : VISIBLE);
        }
        if (optOutStateLayout != null) {
            optOutStateLayout.setVisibility(GONE);
        }
    }

    private void showCoOrganizerInviteDialog(@NonNull Notification notification) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notification_co_org_title)
                .setMessage(getString(
                        R.string.notification_co_org_message,
                        notification.getEventName()
                ))
                .setPositiveButton(R.string.notification_private_invite_accept, (dialog, which) -> {
                    FirebaseFirestore db = DBConnector.getDb();
                    db.collection("events").document(notification.getEventId())
                            .update("coOrganizerId", FieldValue.arrayUnion(notification.getUserId()))
                            .addOnSuccessListener(aVoid -> {
                                markNotificationSeenLocally(notification);
                                showInboxSnackbar(getString(R.string.notification_co_org_success), null, null);
                            });
                })
                .setNegativeButton(R.string.notification_private_invite_decline,
                        (dialog, which) -> markNotificationSeenLocally(notification))
                .show();
    }

    /**
     * Lets entrants accept or decline a private-event invite directly from Alerts. Accepting
     * creates the waiting-list participant row so the event appears in Active without skipping
     * ahead to Attending.
     *
     * @param notification Private-event invite notification being resolved.
     */
    private void showPrivateEventInviteDialog(@NonNull Notification notification) {
        String deviceId = resolveNotificationUserId();
        if (deviceId == null || deviceId.trim().isEmpty()) {
            showInboxSnackbar(getString(R.string.notification_private_invite_failure), null, null);
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notification_private_invite_title)
                .setMessage(getString(
                        R.string.notification_private_invite_message,
                        notification.getEventName()
                ))
                .setPositiveButton(R.string.notification_private_invite_accept, (dialog, which) -> {
                    FirebaseFirestore db = DBConnector.getDb();
                    Map<String, Object> inviteAcceptance = new HashMap<>();
                    inviteAcceptance.put("deviceId", deviceId);
                    inviteAcceptance.put("status", "waiting");
                    inviteAcceptance.put("timeJoined", FieldValue.serverTimestamp());

                    db.collection("events")
                            .document(notification.getEventId())
                            .collection("participants")
                            .document(deviceId)
                            .set(inviteAcceptance, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                markNotificationSeenLocally(notification);
                                showInboxSnackbar(
                                        getString(R.string.notification_private_invite_success),
                                        null,
                                        null
                                );
                            })
                            .addOnFailureListener(e -> showInboxSnackbar(
                                    getString(R.string.notification_private_invite_failure),
                                    null,
                                    null
                            ));
                })
                .setNegativeButton(R.string.notification_private_invite_decline, (dialog, which) -> {
                    markNotificationSeenLocally(notification);
                    showInboxSnackbar(getString(R.string.notification_private_invite_declined), null, null);
                })
                .show();
    }

    private void markNotificationSeenLocally(@NonNull Notification notification) {
        notification.setSeen(true);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (notification.getNotificationId() != null && !notification.getNotificationId().trim().isEmpty()) {
            notificationService.markNotificationSeen(notification.getNotificationId());
        }
    }

    private void showInboxSnackbar(@NonNull String message,
                                   @Nullable String actionLabel,
                                   @Nullable Runnable action) {
        if (rootView == null) {
            return;
        }

        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackbar_surface))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        if (actionLabel != null && action != null) {
            snackbar.setAction(actionLabel, v -> action.run());
            snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.breeze_light));
        }

        snackbar.show();
    }
}
