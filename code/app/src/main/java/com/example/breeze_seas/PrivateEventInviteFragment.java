package com.example.breeze_seas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lets organizers invite existing users into a private event by searching local user fields.
 * Invites reuse the shared notification flow so entrants respond from Alerts first. A participant
 * row is only created after the entrant accepts, which then moves the event into the waiting-list
 * flow inside Active Tickets.
 */
public class PrivateEventInviteFragment extends Fragment {

    private final FirebaseFirestore db = DBConnector.getDb();
    private final NotificationService notificationService = new NotificationService();
    private final ArrayList<User> allUsers = new ArrayList<>();
    private final ArrayList<User> eligibleUsers = new ArrayList<>();
    private final Set<String> participantIds = new HashSet<>();
    private final Set<String> pendingInviteIds = new HashSet<>();
    private final Set<String> organizerIds = new HashSet<>();

    private SessionViewModel sessionViewModel;
    private Event currentEvent;

    private OrganizerListAdapter adapter;
    private ListView userListView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView eventNameView;
    private EditText searchInput;

    @Nullable
    private ListenerRegistration eventListener;
    @Nullable
    private ListenerRegistration participantsListener;
    @Nullable
    private ListenerRegistration inviteNotificationsListener;

    private boolean usersLoaded = false;
    private String searchQuery = "";

    /**
     * Creates the private-event invite screen.
     */
    public PrivateEventInviteFragment() {
        super(R.layout.fragment_private_event_invite);
    }

    /**
     * Connects this screen to the shared session state used for organizer flows.
     *
     * @param savedInstanceState Previously saved instance state, or {@code null}.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
    }

    /**
     * Inflates the private-event invite screen.
     *
     * @param inflater Layout inflater used to build the view.
     * @param container Optional parent view group.
     * @param savedInstanceState Previously saved instance state, or {@code null}.
     * @return Inflated root view for this screen.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_private_event_invite, container, false);
    }

    /**
     * Binds the invite UI, validates that the current event is private, and starts the initial
     * user-loading flow.
     *
     * @param view Inflated root view for the screen.
     * @param savedInstanceState Previously saved instance state, or {@code null}.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentEvent = sessionViewModel.getEventShown().getValue();
        if (currentEvent == null) {
            Toast.makeText(requireContext(), R.string.private_event_invite_missing_event, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }
        if (!currentEvent.isPrivate()) {
            Toast.makeText(requireContext(), R.string.private_event_invite_private_only, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        bindViews(view);
        adapter = new OrganizerListAdapter(
                requireContext(),
                R.layout.item_organizer_list,
                eligibleUsers,
                getString(R.string.private_event_invite_chip),
                false
        );
        userListView.setAdapter(adapter);
        userListView.setEmptyView(emptyTextView);

        view.findViewById(R.id.private_event_invite_back_button)
                .setOnClickListener(new View.OnClickListener() {
                    /**
                     * Returns to the organizer preview screen.
                     *
                     * @param v Back button that was tapped.
                     */
                    @Override
                    public void onClick(View v) {
                        getParentFragmentManager().popBackStack();
                    }
                });

        userListView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < eligibleUsers.size()) {
                confirmInvite(eligibleUsers.get(position));
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s == null ? "" : s.toString();
                refreshEligibleUsers();
            }
        });

        bindEventInfo(currentEvent);
        loadUsers();
    }

    /**
     * Starts realtime listeners that keep organizer exclusions and participant exclusions fresh
     * while this screen is visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (currentEvent != null
                && eventListener == null
                && participantsListener == null
                && inviteNotificationsListener == null) {
            startRealtimeExclusionListeners();
        }
    }

    /**
     * Stops the realtime listeners owned by this view so they do not outlive the screen.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
            eventListener = null;
        }
        if (participantsListener != null) {
            participantsListener.remove();
            participantsListener = null;
        }
        if (inviteNotificationsListener != null) {
            inviteNotificationsListener.remove();
            inviteNotificationsListener = null;
        }
    }

    /**
     * Binds the view references used by the private invite UI.
     *
     * @param view Inflated root view for the screen.
     */
    private void bindViews(@NonNull View view) {
        userListView = view.findViewById(R.id.private_event_invite_user_list);
        progressBar = view.findViewById(R.id.private_event_invite_loading_progress);
        emptyTextView = view.findViewById(R.id.private_event_invite_empty_text);
        eventNameView = view.findViewById(R.id.private_event_invite_event_name);
        searchInput = view.findViewById(R.id.private_event_invite_search_input);
    }

    /**
     * Updates the header and organizer exclusion set from the current event snapshot.
     *
     * @param event Current private event being managed.
     */
    private void bindEventInfo(@NonNull Event event) {
        eventNameView.setText(event.getName());

        organizerIds.clear();
        if (event.getOrganizerId() != null && !event.getOrganizerId().trim().isEmpty()) {
            organizerIds.add(event.getOrganizerId().trim());
        }
        if (event.getCoOrganizerId() != null) {
            for (String coOrganizerId : event.getCoOrganizerId()) {
                if (coOrganizerId != null && !coOrganizerId.trim().isEmpty()) {
                    organizerIds.add(coOrganizerId.trim());
                }
            }
        }

        refreshEligibleUsers();
    }

    /**
     * Starts realtime listeners for the event document and its participants subcollection so the
     * invite list automatically removes newly added organizers, entrants, and outstanding invites.
     */
    private void startRealtimeExclusionListeners() {
        String eventId = currentEvent.getEventId();

        eventListener = db.collection("events")
                .document(eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), R.string.private_event_invite_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(requireContext(), R.string.private_event_invite_missing_event, Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }

                    currentEvent.setOrganizerId(snapshot.getString("organizerId"));
                    currentEvent.setPrivate(Boolean.TRUE.equals(snapshot.getBoolean("isPrivate")));
                    List<String> coOrganizers = (List<String>) snapshot.get("coOrganizerId");
                    currentEvent.setCoOrganizerId(coOrganizers == null
                            ? new ArrayList<>()
                            : new ArrayList<>(coOrganizers));
                    currentEvent.setName(snapshot.getString("name"));

                    if (!currentEvent.isPrivate()) {
                        Toast.makeText(requireContext(), R.string.private_event_invite_private_only, Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                        return;
                    }

                    bindEventInfo(currentEvent);
                });

        participantsListener = db.collection("events")
                .document(eventId)
                .collection("participants")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), R.string.private_event_invite_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    participantIds.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String participantStatus = doc.getString("status");
                            if (!"private_invited".equals(participantStatus)) {
                                participantIds.add(doc.getId());
                            }
                        }
                    }
                    refreshEligibleUsers();
                });

        inviteNotificationsListener = db.collection("notifications")
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), R.string.private_event_invite_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pendingInviteIds.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String type = doc.getString("type");
                            boolean isSeen = Boolean.TRUE.equals(doc.getBoolean("isSeen"));
                            String invitedUserId = doc.getString("userId");
                            if (NotificationType.PRIVATE_EVENT_INVITE.name().equals(type)
                                    && !isSeen
                                    && invitedUserId != null
                                    && !invitedUserId.trim().isEmpty()) {
                                pendingInviteIds.add(invitedUserId.trim());
                            }
                        }
                    }
                    refreshEligibleUsers();
                });
    }

    /**
     * Loads the current user directory once so search can run locally across multiple fields.
     */
    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get().addOnCompleteListener(task -> {
            usersLoaded = true;
            progressBar.setVisibility(View.GONE);

            if (!task.isSuccessful() || task.getResult() == null) {
                Toast.makeText(requireContext(), R.string.private_event_invite_failed, Toast.LENGTH_SHORT).show();
                refreshEligibleUsers();
                return;
            }

            allUsers.clear();
            for (QueryDocumentSnapshot doc : task.getResult()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setDeviceId(doc.getId());
                    allUsers.add(user);
                }
            }

            Collections.sort(allUsers, Comparator.comparing(this::buildDisplayName, String.CASE_INSENSITIVE_ORDER));
            refreshEligibleUsers();
        });
    }

    /**
     * Recomputes the inviteable user list after any search, organizer change, participant change,
     * or user-directory refresh.
     */
    private void refreshEligibleUsers() {
        eligibleUsers.clear();

        for (User user : allUsers) {
            if (user == null || user.getDeviceId() == null || user.getDeviceId().trim().isEmpty()) {
                continue;
            }
            if (organizerIds.contains(user.getDeviceId())
                    || participantIds.contains(user.getDeviceId())
                    || pendingInviteIds.contains(user.getDeviceId())) {
                continue;
            }
            if (matchesSearch(user, searchQuery)) {
                eligibleUsers.add(user);
            }
        }

        Collections.sort(eligibleUsers, Comparator.comparing(this::buildDisplayName, String.CASE_INSENSITIVE_ORDER));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    /**
     * Updates the empty-state copy so the screen distinguishes between "no eligible users" and
     * "no search results."
     */
    private void updateEmptyState() {
        if (!usersLoaded) {
            emptyTextView.setVisibility(View.GONE);
            return;
        }

        boolean hasSearch = searchQuery != null && !searchQuery.trim().isEmpty();
        emptyTextView.setText(hasSearch
                ? R.string.private_event_invite_empty_search
                : R.string.private_event_invite_empty);
    }

    /**
     * Matches one user against the current search query.
     *
     * @param user Candidate user to evaluate.
     * @param rawQuery Current free-text search value.
     * @return {@code true} when the user should remain visible in the invite list.
     */
    private boolean matchesSearch(@NonNull User user, @Nullable String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return true;
        }

        String normalizedQuery = rawQuery.trim().toLowerCase(Locale.US);
        String phoneQuery = digitsOnly(rawQuery);

        return containsIgnoreCase(buildDisplayName(user), normalizedQuery)
                || containsIgnoreCase(user.getUserName(), normalizedQuery)
                || containsIgnoreCase(user.getEmail(), normalizedQuery)
                || (!phoneQuery.isEmpty() && digitsOnly(user.getPhoneNumber()).contains(phoneQuery));
    }

    /**
     * Shows a confirmation dialog before writing a private invite for the selected user.
     *
     * @param user User chosen from the invite list.
     */
    private void confirmInvite(@NonNull User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.private_event_invite_confirm_title)
                .setMessage(getString(
                        R.string.private_event_invite_confirm_message,
                        buildDisplayName(user),
                        currentEvent.getName()
                ))
                .setPositiveButton(R.string.send, (dialog, which) -> inviteUser(user))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Sends the private invite through the shared notification system so the entrant responds from
     * Alerts before any ticket row is created.
     *
     * @param user User receiving the private invite.
     */
    private void inviteUser(@NonNull User user) {
        if (currentEvent == null || currentEvent.getEventId() == null) {
            Toast.makeText(requireContext(), R.string.private_event_invite_missing_event, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Notification notification = new Notification(
                NotificationType.PRIVATE_EVENT_INVITE,
                "",
                currentEvent.getEventId(),
                currentEvent.getName(),
                user.getDeviceId()
        );
        notificationService.sendNotification(notification)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    pendingInviteIds.add(user.getDeviceId());
                    refreshEligibleUsers();
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.private_event_invite_success, buildDisplayName(user)),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.private_event_invite_failed, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Builds the best display name available for one user in organizer-facing lists.
     *
     * @param user User whose visible label is needed.
     * @return Best available human-readable display label.
     */
    @NonNull
    private String buildDisplayName(@NonNull User user) {
        String firstName = safeTrim(user.getFirstName());
        String lastName = safeTrim(user.getLastName());
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        String userName = safeTrim(user.getUserName());
        if (!userName.isEmpty()) {
            return userName;
        }

        String email = safeTrim(user.getEmail());
        if (!email.isEmpty()) {
            return email;
        }

        return user.getDeviceId() == null ? "" : user.getDeviceId();
    }

    /**
     * Performs a case-insensitive substring match for local search.
     *
     * @param source Source text that may contain the query.
     * @param normalizedQuery Lowercased search query.
     * @return {@code true} when the source contains the query.
     */
    private boolean containsIgnoreCase(@Nullable String source, @NonNull String normalizedQuery) {
        return source != null && source.toLowerCase(Locale.US).contains(normalizedQuery);
    }

    /**
     * Normalizes optional phone-like strings to digits only for partial phone search.
     *
     * @param value Raw optional phone text.
     * @return Digits-only value, or an empty string when unavailable.
     */
    @NonNull
    private String digitsOnly(@Nullable String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    /**
     * Normalizes optional string values for display and filtering.
     *
     * @param value Raw optional text.
     * @return Trimmed text, or an empty string when unavailable.
     */
    @NonNull
    private String safeTrim(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
