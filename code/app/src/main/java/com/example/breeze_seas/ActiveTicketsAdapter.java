package com.example.breeze_seas;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * ActiveTicketsAdapter renders the list of "Active" ticket cards.
 *
 * <p>Role:
 * - Binds {@link TicketUIModel} data to the ticket card layout.
 *
 * <p>Outstanding:
 * - Keep binding logic stable while TicketDB expands to more ticket states.
 * - Add stable IDs when using real Firestore document IDs.
 */
public class ActiveTicketsAdapter extends RecyclerView.Adapter<ActiveTicketsAdapter.TicketViewHolder> {
    // The following adapter implementation was generated with assistance from ChatGPT,
    // "How to implement a RecyclerView Adapter in Android using java", 2026-03-03.

    /**
     * Listener for taps on active-ticket cards.
     */
    public interface OnTicketClickListener {
        /**
         * Handles a tap on an active ticket card.
         *
         * @param ticket The active ticket that was tapped.
         */
        void onTicketClick(TicketUIModel ticket);
    }

    private final List<TicketUIModel> items = new ArrayList<>();
    private final OnTicketClickListener listener;

    /**
     * Creates the adapter used by the Active Tickets list.
     *
     * @param listener Listener invoked when the user taps a ticket card.
     */
    public ActiveTicketsAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the current active-ticket list contents.
     *
     * @param newItems New active tickets to render.
     */
    public void submitList(List<TicketUIModel> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Inflates a single active-ticket card view holder.
     *
     * @param parent Parent view group that will host the card.
     * @param viewType Adapter view type value.
     * @return A new active-ticket view holder.
     */
    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_history, parent, false);
        return new TicketViewHolder(v);
    }

    /**
     * Binds the active-ticket card at the requested position.
     *
     * @param holder View holder receiving the ticket data.
     * @param position Zero-based adapter position being bound.
     */
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketUIModel ticket = items.get(position);

        holder.title.setText(ticket.getTitle());
        holder.date.setText(ticket.getDateLabel());
        holder.actionDot.setVisibility(View.GONE);

        final int white = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white);
        final int black = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black);
        final int secondary = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
        int fallbackIconRes = R.drawable.ic_clock;

        switch (ticket.getStatus()) {

            case PENDING:
                holder.chip.setText(R.string.ticket_waiting_chip);
                fallbackIconRes = R.drawable.ic_clock;
                holder.supporting.setText(R.string.ticket_waiting_supporting);
                holder.footer.setText(R.string.ticket_waiting_footer);

                holder.chip.setBackgroundResource(R.drawable.bg_ticket_status_outline);
                holder.chip.setTextColor(black);
                break;

            case BACKUP:
                holder.chip.setText(R.string.ticket_backup_chip);
                fallbackIconRes = R.drawable.ic_info;
                holder.supporting.setText(R.string.ticket_backup_supporting);
                holder.footer.setText(R.string.ticket_backup_footer);

                holder.chip.setBackgroundResource(R.drawable.bg_ticket_status_outline);
                holder.chip.setTextColor(black);
                break;

            case ACTION_REQUIRED:
                holder.chip.setText(ticket.isPrivateInvitePending()
                        ? R.string.ticket_private_invite_chip
                        : R.string.ticket_selected_chip);
                fallbackIconRes = ticket.isPrivateInvitePending()
                        ? R.drawable.ic_ticket
                        : R.drawable.ic_star;
                if (ticket.isPrivateInvitePending()) {
                    holder.supporting.setText(R.string.ticket_private_invite_supporting);
                    holder.footer.setText(R.string.ticket_private_invite_footer);
                } else {
                    holder.supporting.setText(R.string.ticket_selected_supporting);
                    holder.footer.setText(R.string.ticket_selected_footer);
                }

                holder.chip.setBackgroundResource(R.drawable.bg_ticket_status_solid);
                holder.chip.setTextColor(white);
                holder.actionDot.setVisibility(View.VISIBLE);
                break;
        }

        final int resolvedFallbackIconRes = fallbackIconRes;
        bindFallbackThumbnail(holder.icon, resolvedFallbackIconRes);
        UiImageBinder.bindImageDoc(holder.icon, ticket.getImageDocId(),
                () -> bindFallbackThumbnail(holder.icon, resolvedFallbackIconRes));
        holder.footer.setTextColor(secondary);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            /**
             * Dispatches the tapped active ticket back to the fragment layer.
             *
             * @param v The ticket card view that was pressed.
             */
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTicketClick(ticket);
                }
            }
        });
    }

    private void bindFallbackThumbnail(@NonNull ImageView imageView, int drawableResId) {
        int padding = (int) (imageView.getResources().getDisplayMetrics().density * 15);
        imageView.setImageResource(drawableResId);
        imageView.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(imageView.getContext(), R.color.text_primary)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(padding, padding, padding, padding);
    }

    /**
     * Returns the number of active-ticket cards currently being rendered.
     *
     * @return The active-ticket item count.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * View holder for a single active-ticket card.
     */
    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView date;
        TextView supporting;
        TextView footer;
        TextView chip;
        View actionDot;

        /**
         * Binds the child views used to render one active-ticket card.
         *
         * @param itemView Inflated card view backing this holder.
         */
        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ticket_thumbnail);
            title = itemView.findViewById(R.id.ticket_event_title);
            date = itemView.findViewById(R.id.ticket_event_date);
            supporting = itemView.findViewById(R.id.ticket_supporting_copy);
            footer = itemView.findViewById(R.id.ticket_footer_hint);
            chip = itemView.findViewById(R.id.ticket_status_chip);
            actionDot = itemView.findViewById(R.id.action_dot);
        }
    }
}
