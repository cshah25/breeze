package com.example.breeze_seas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

/**
 * PastTicketsFragment shows completed or inactive ticket history.
 *
 * <p>Intended content:
 * - Declined
 * - Lost / not selected
 * - Optional cancelled states
 *
 * <p>Current state:
 * - Loads declined, cancelled, and not-selected entries through {@link TicketDB}.
 *
 * <p>Outstanding:
 * - Expand past-history detail once the team finalizes whether completed accepted events should move here too.
 */
public class PastTicketsFragment extends Fragment {

    private final TicketDB ticketDb = TicketDB.getInstance();
    private final TicketDB.Listener ticketsListener = new TicketDB.Listener() {
        /**
         * Refreshes the past-ticket list when {@link TicketDB} publishes new data.
         */
        @Override
        public void onTicketsChanged() {
            renderTickets();
        }
    };

    private PastTicketsAdapter adapter;

    /**
     * Inflates the Past tab, binds its list, and starts listening for archived ticket updates.
     *
     * @param inflater Layout inflater used to build the fragment view.
     * @param container Optional parent that will host the inflated hierarchy.
     * @param savedInstanceState Saved state bundle, if the fragment is being recreated.
     * @return The inflated Past Tickets view.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_past_tickets, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.past_tickets_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PastTicketsAdapter(new PastTicketsAdapter.OnPastEventClickListener() {
            /**
             * Shows the current placeholder response for past-ticket taps.
             *
             * @param event The archived event card the entrant tapped.
             */
            @Override
            public void onPastEventClick(@NonNull PastEventUIModel event) {
                Snackbar.make(view, "Past event details are not available yet.", Snackbar.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        ticketDb.addListener(ticketsListener);
        ticketDb.refreshTickets(requireContext());
        renderTickets();

        return view;
    }

    /**
     * Removes ticket listeners tied to the destroyed past-history view hierarchy.
     */
    @Override
    public void onDestroyView() {
        ticketDb.removeListener(ticketsListener);
        adapter = null;
        super.onDestroyView();
    }

    /**
     * Rebinds the current past-ticket list into the adapter.
     */
    private void renderTickets() {
        if (adapter == null) {
            return;
        }

        adapter.submitList(ticketDb.getPastTickets());
    }
}
