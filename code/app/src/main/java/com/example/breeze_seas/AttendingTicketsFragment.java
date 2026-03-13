package com.example.breeze_seas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * AttendingTicketsFragment displays the events that the entrant has accepted and will attend.
 *
 * <p>Current state:
 * - Loads accepted tickets through {@link TicketDB}.
 *
 * <p>Outstanding:
 * - Replace fallback display values once event location/ticket-type fields are finalized.
 */
public class AttendingTicketsFragment extends Fragment {

    private final TicketDB ticketDb = TicketDB.getInstance();
    private final TicketDB.Listener ticketsListener = new TicketDB.Listener() {
        /**
         * Refreshes the attending-ticket UI when {@link TicketDB} publishes new data.
         */
        @Override
        public void onTicketsChanged() {
            renderTickets();
        }
    };

    private AttendingTicketsAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;

    /**
     * Inflates the Attending tab, binds its list, and starts listening for accepted tickets.
     *
     * @param inflater Layout inflater used to build the fragment view.
     * @param container Optional parent that will host the inflated hierarchy.
     * @param savedInstanceState Saved state bundle, if the fragment is being recreated.
     * @return The inflated Attending Tickets view.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_attending_tickets, container, false);

        recyclerView = view.findViewById(R.id.attending_tickets_recycler);
        emptyState = view.findViewById(R.id.attending_tickets_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AttendingTicketsAdapter(new AttendingTicketsAdapter.OnTicketClickListener() {
            /**
             * Opens the QR ticket pass for the selected attending event.
             *
             * @param ticket The confirmed attending ticket the entrant tapped.
             */
            @Override
            public void onTicketClick(@NonNull AttendingTicketUIModel ticket) {
                openTicket(ticket);
            }
        });
        recyclerView.setAdapter(adapter);
        ticketDb.addListener(ticketsListener);
        ticketDb.refreshTickets(requireContext());
        renderTickets();

        return view;
    }

    /**
     * Clears references tied to the destroyed attending-tickets view hierarchy.
     */
    @Override
    public void onDestroyView() {
        ticketDb.removeListener(ticketsListener);
        recyclerView = null;
        emptyState = null;
        adapter = null;
        super.onDestroyView();
    }

    /**
     * Updates the attending-ticket list and toggles the empty state when necessary.
     */
    private void renderTickets() {
        if (adapter == null || recyclerView == null || emptyState == null) {
            return;
        }

        java.util.List<AttendingTicketUIModel> tickets = ticketDb.getAttendingTickets();
        boolean hasTickets = !tickets.isEmpty();
        adapter.submitList(tickets);
        recyclerView.setVisibility(hasTickets ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasTickets ? View.GONE : View.VISIBLE);
    }

    /**
     * Opens the QR-code ticket screen for a confirmed event.
     *
     * @param ticket The confirmed attending ticket to open.
     */
    private void openTicket(@NonNull AttendingTicketUIModel ticket) {
        if (!isAdded()) {
            return;
        }

        // TODO: Open QRCode Fragment
    }
}
