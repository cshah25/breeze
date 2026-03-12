package com.example.breeze_seas;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal helper for classifying ticket states into Tickets tabs.
 *
 * <p>This keeps tab-mapping logic out of UI tests so it can be validated with plain JUnit.
 */
public final class TicketTabMapper {

    /**
     * Ticket states that can be classified into the three Tickets tabs.
     */
    public enum TicketState {
        PENDING,
        BACKUP,
        ACTION_REQUIRED,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        NOT_SELECTED
    }

    /**
     * Target tabs shown in the Tickets feature.
     */
    public enum TicketTab {
        ACTIVE,
        ATTENDING,
        PAST
    }

    /**
     * Prevents instantiation of this pure utility class.
     */
    private TicketTabMapper() {
    }

    /**
     * Maps a ticket state to the Tickets tab where it should appear.
     *
     * @param state Ticket state being classified.
     * @return The tab that should contain the provided state.
     */
    @NonNull
    public static TicketTab mapToTab(@NonNull TicketState state) {
        switch (state) {
            case PENDING:
            case BACKUP:
            case ACTION_REQUIRED:
                return TicketTab.ACTIVE;
            case ACCEPTED:
                return TicketTab.ATTENDING;
            case DECLINED:
            case CANCELLED:
            case NOT_SELECTED:
            default:
                return TicketTab.PAST;
        }
    }

    /**
     * Filters a list of ticket states down to only those that belong to the requested tab.
     *
     * @param states Ticket states to filter.
     * @param tab Target tab to keep.
     * @return A new list containing only states that belong to the requested tab.
     */
    @NonNull
    public static List<TicketState> filterStatesForTab(
            @NonNull List<TicketState> states,
            @NonNull TicketTab tab
    ) {
        List<TicketState> filteredStates = new ArrayList<>();
        for (TicketState state : states) {
            if (mapToTab(state) == tab) {
                filteredStates.add(state);
            }
        }
        return filteredStates;
    }
}
