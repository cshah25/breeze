package com.example.breeze_seas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * ActiveTicketsFragment displays the entrant's active ticket states.
 *
 * <p>Current state:
 * - Loads waiting, backup, and invited tickets through {@link TicketDB}.
 *
 * <p>Outstanding:
 * - Replace temporary success/failure messaging with the final UX once the wider notification flow is done.
 */
public class ActiveTicketsFragment extends Fragment {

    private final TicketDB ticketDb = TicketDB.getInstance();
    private final TicketDB.Listener ticketsListener = new TicketDB.Listener() {
        /**
         * Refreshes the active-ticket list when {@link TicketDB} publishes new data.
         */
        @Override
        public void onTicketsChanged() {
            renderTickets();
        }
    };

    private ActiveTicketsAdapter adapter;

    /**
     * Inflates the Active tab, binds its list, and starts listening for ticket updates.
     *
     * @param inflater Layout inflater used to build the fragment view.
     * @param container Optional parent that will host the inflated hierarchy.
     * @param savedInstanceState Saved state bundle, if the fragment is being recreated.
     * @return The inflated Active Tickets view.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_active_tickets, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.active_tickets_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ActiveTicketsAdapter(new ActiveTicketsAdapter.OnTicketClickListener() {
            /**
             * Routes each active ticket click to the correct dialog or status message.
             *
             * @param ticket The active ticket card the entrant tapped.
             */
            @Override
            public void onTicketClick(@NonNull TicketUIModel ticket) {
                handleTicketClick(view, ticket);
            }
        });

        recyclerView.setAdapter(adapter);
        ticketDb.addListener(ticketsListener);
        refreshTickets();
        renderTickets();

        return view;
    }

    /**
     * Removes ticket listeners tied to the destroyed view hierarchy.
     */
    @Override
    public void onDestroyView() {
        ticketDb.removeListener(ticketsListener);
        adapter = null;
        super.onDestroyView();
    }

    /**
     * Rebinds the current active-ticket list into the adapter.
     */
    private void renderTickets() {
        if (adapter == null) {
            return;
        }

        adapter.submitList(ticketDb.getActiveTickets());
    }

    /**
     * Reloads the ticket lists using the current session user's device id when available.
     */
    private void refreshTickets() {
        SessionViewModel viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        User currentUser = viewModel.getUser().getValue();
        String preferredDeviceId = currentUser == null ? null : currentUser.getDeviceId();
        ticketDb.refreshTickets(requireContext(), preferredDeviceId);
    }

    /**
     * Handles taps on active ticket cards based on their current status.
     *
     * @param rootView Root fragment view used for transient snackbar messages.
     * @param ticket The ticket card the entrant tapped.
     */
    private void handleTicketClick(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (ticket.getStatus() == TicketUIModel.Status.PENDING) {
            showLeaveWaitlistDialog(rootView, ticket);
            return;
        }

        if (ticket.getStatus() == TicketUIModel.Status.BACKUP) {
            showBackupPoolDialog(rootView, ticket);
            return;
        }

        if (ticket.getStatus() == TicketUIModel.Status.ACTION_REQUIRED) {
            showInvitationActionDialog(rootView, ticket);
            return;
        }

        Snackbar.make(
                rootView,
                "Your entry is still active. We will notify you when the draw status changes.",
                Snackbar.LENGTH_SHORT
        ).show();
    }

    /**
     * Shows a centered informational dialog explaining the backup pool state.
     *
     * <p>This replaces the previous bottom sheet approach to avoid excessive empty space.
     * The dialog also lets the entrant leave the waitlist from the backup pool.
     *
     * Source:
     * Google Material Design, "Dialogs", accessed 2026-03-04:
     * https://m3.material.io/components/dialogs/overview
     *
     * @param rootView Root fragment view used for the follow-up snackbar message.
     * @param ticket The backup-pool ticket the entrant tapped.
     */
    private void showBackupPoolDialog(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (!isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("You're on the Waitlist")
                .setMessage("The first draw is complete. You are currently in the backup pool. If any tickets are declined, we will run another draw and notify you.")
                .setNegativeButton("Leave Waitlist", new DialogInterface.OnClickListener() {
                    /**
                     * Opens a confirmation dialog before removing the entrant from the backup pool.
                     *
                     * @param dialog The informational dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showLeaveWaitlistConfirmDialog(rootView, ticket);
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    /**
                     * Dismisses the informational dialog once the entrant acknowledges it.
                     *
                     * @param dialog The displayed dialog instance.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows the primary waitlist dialog for entrants who are still waiting on the initial draw.
     *
     * @param rootView Root fragment view used for the follow-up snackbar message.
     * @param ticket The waiting-list ticket the entrant tapped.
     */
    private void showLeaveWaitlistDialog(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (!isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Leave waitlist?")
                .setMessage(
                        "You are currently waiting for:\n\n"
                                + ticket.getTitle()
                                + "\n"
                                + ticket.getDateLabel()
                                + "\n\nIf you leave now, your entry will be removed from the draw."
                )
                .setPositiveButton("Leave Waitlist", new DialogInterface.OnClickListener() {
                    /**
                     * Opens the final confirmation step before deleting the waitlist entry.
                     *
                     * @param dialog The waitlist dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showLeaveWaitlistConfirmDialog(rootView, ticket);
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    /**
                     * Keeps the entrant on the waitlist and closes the dialog.
                     *
                     * @param dialog The waitlist dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows a confirmation dialog before removing the entrant from the waitlist.
     *
     * @param rootView Root fragment view used for the follow-up snackbar message.
     * @param ticket The active ticket whose waitlist entry may be removed.
     */
    private void showLeaveWaitlistConfirmDialog(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (!isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm leave?")
                .setMessage("This will remove your entry from the waitlist for this event.")
                .setPositiveButton("Yes, leave", new DialogInterface.OnClickListener() {
                    /**
                     * Removes the entrant from the waitlist and refreshes the Active tab.
                     *
                     * @param dialog The confirmation dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ticketDb.leaveWaitlist(ticket);
                        Snackbar.make(rootView, "You left the waitlist.", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    /**
                     * Cancels the leave-waitlist flow and dismisses the dialog.
                     *
                     * @param dialog The confirmation dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows a centered dialog for the "Action Required" state where the entrant can accept or decline.
     *
     * <p>Current behavior:
     * - Accept: updates the participant status to {@code accepted}.
     * - Decline: updates the participant status to {@code declined}.
     *
     * <p>Outstanding:
     * - Surface write failures with the final UX once the wider app error-handling pattern is settled.
     *
     * Source:
     * Google Material Design, "Dialogs", accessed 2026-03-04:
     * https://m3.material.io/components/dialogs/overview
     */
    private void showInvitationActionDialog(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (!isAdded()) return;

        String title = "You won!";
        String message =
                "You've been selected for:\n\n" +
                        ticket.getTitle() + "\n" +
                        ticket.getDateLabel() + "\n\n" +
                        "Accept to confirm your spot, or decline to release it to the backup pool.";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Accept Invitation", new DialogInterface.OnClickListener() {
                    /**
                     * Accepts the invitation and moves the ticket into the attending state.
                     *
                     * @param dialog The action dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ticketDb.acceptInvitation(ticket);
                        Snackbar.make(rootView, "Invitation accepted. Ticket moved to Attending.", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    /**
                     * Opens the confirmation dialog before declining the invitation.
                     *
                     * @param dialog The action dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDeclineConfirmDialog(rootView, ticket);
                    }
                })
                .show();
    }

    /**
     * Shows a confirmation dialog to avoid accidental invitation declines.
     *
     * @param rootView Root fragment view used for the follow-up snackbar message.
     * @param ticket The invited ticket that may be declined.
     */
    private void showDeclineConfirmDialog(@NonNull View rootView, @NonNull TicketUIModel ticket) {
        if (!isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Decline invitation?")
                .setMessage("If you decline, your spot will be offered to someone in the backup pool.")
                .setPositiveButton("Yes, decline", new DialogInterface.OnClickListener() {
                    /**
                     * Confirms the decline and updates the participant status.
                     *
                     * @param dialog The confirmation dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ticketDb.declineInvitation(ticket);
                        Snackbar.make(rootView, "Invitation declined", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    /**
                     * Cancels the decline flow and dismisses the confirmation dialog.
                     *
                     * @param dialog The confirmation dialog shown to the entrant.
                     * @param which The pressed button identifier.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
