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
    private final TicketDB.Listener ticketsListener = this::renderTickets;

    private AttendingTicketsAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;

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

        adapter = new AttendingTicketsAdapter(this::openTicket);
        recyclerView.setAdapter(adapter);
        ticketDb.addListener(ticketsListener);
        ticketDb.refreshTickets(requireContext());
        renderTickets();

        return view;
    }

    @Override
    public void onDestroyView() {
        ticketDb.removeListener(ticketsListener);
        recyclerView = null;
        emptyState = null;
        adapter = null;
        super.onDestroyView();
    }

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

    private void openTicket(@NonNull AttendingTicketUIModel ticket) {
        if (!isAdded()) {
            return;
        }

        Intent intent = new Intent(requireContext(), ViewQrCodeActivity.class);
        intent.putExtra("eventId", ticket.getEventId());
        intent.putExtra("eventName", ticket.getTitle());
        startActivity(intent);
    }
}
