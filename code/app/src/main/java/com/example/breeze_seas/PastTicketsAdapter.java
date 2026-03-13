package com.example.breeze_seas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * PastTicketsAdapter renders archived ticket history in a RecyclerView.
 */
public class PastTicketsAdapter extends RecyclerView.Adapter<PastTicketsAdapter.PastTicketViewHolder> {

    /**
     * Listener for taps on past-event cards.
     */
    public interface OnPastEventClickListener {
        /**
         * Handles a tap on a past-event card.
         *
         * @param event The archived event that was tapped.
         */
        void onPastEventClick(PastEventUIModel event);
    }

    private final List<PastEventUIModel> items = new ArrayList<>();
    private final OnPastEventClickListener listener;

    /**
     * Creates the adapter used by the Past Tickets list.
     *
     * @param listener Listener invoked when the user taps a past event card.
     */
    public PastTicketsAdapter(OnPastEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the current past-ticket list contents.
     *
     * @param events New past events to render.
     */
    public void submitList(List<PastEventUIModel> events) {
        items.clear();
        items.addAll(events);
        notifyDataSetChanged();
    }

    /**
     * Inflates a single past-event card view holder.
     *
     * @param parent Parent view group that will host the card.
     * @param viewType Adapter view type value.
     * @return A new past-event view holder.
     */
    @NonNull
    @Override
    public PastTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_event_card, parent, false);
        return new PastTicketViewHolder(view);
    }

    /**
     * Binds the past-event card at the requested position.
     *
     * @param holder View holder receiving the event data.
     * @param position Zero-based adapter position being bound.
     */
    @Override
    public void onBindViewHolder(@NonNull PastTicketViewHolder holder, int position) {
        PastEventUIModel event = items.get(position);

        holder.icon.setImageResource(event.getIconResId());
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDateLabel());
        holder.location.setText(event.getLocationLabel());
        holder.detail.setText(event.getDetailLabel());
        holder.statusChip.setText(event.getStatusLabel());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            /**
             * Dispatches the tapped past event back to the fragment layer.
             *
             * @param v The event card view that was pressed.
             */
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPastEventClick(event);
                }
            }
        });
    }

    /**
     * Returns the number of past-event cards currently being rendered.
     *
     * @return The past-event item count.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * View holder for a single past-event card.
     */
    static class PastTicketViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView date;
        final TextView location;
        final TextView detail;
        final TextView statusChip;

        /**
         * Binds the child views used to render one past-event card.
         *
         * @param itemView Inflated card view backing this holder.
         */
        PastTicketViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.past_event_icon);
            title = itemView.findViewById(R.id.past_event_title);
            date = itemView.findViewById(R.id.past_event_date);
            location = itemView.findViewById(R.id.past_event_location);
            detail = itemView.findViewById(R.id.past_event_detail);
            statusChip = itemView.findViewById(R.id.past_event_status_chip);
        }
    }
}
