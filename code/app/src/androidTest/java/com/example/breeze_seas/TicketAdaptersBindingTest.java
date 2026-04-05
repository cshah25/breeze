package com.example.breeze_seas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * Binding tests for the ticket-related adapters used by the Tickets feature.
 *
 * <p>These tests verify the presentation copy, status chips, visibility rules, and click
 * callbacks for the Active, Attending, and Past ticket cards.
 */
@RunWith(AndroidJUnit4.class)
public class TicketAdaptersBindingTest {

    /**
     * Verifies that a waiting active ticket binds the expected title, date, helper copy, and
     * click callback.
     */
    @Test
    public void activeTicketsAdapter_bindsWaitingTicketCopyAndDispatchesClicks() {
        ContextThemeWrapper context = themedContext();
        TicketUIModel ticket = new TicketUIModel(
                "active-event",
                "Night Swim",
                "Jun 1, 2026",
                TicketUIModel.Status.PENDING
        );
        TicketUIModel[] clickedTicket = new TicketUIModel[1];
        ActiveTicketsAdapter adapter = new ActiveTicketsAdapter(selectedTicket -> clickedTicket[0] = selectedTicket);
        adapter.submitList(Collections.singletonList(ticket));
        FrameLayout parent = new FrameLayout(context);
        ActiveTicketsAdapter.TicketViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performClick();

        assertEquals("Night Swim", holder.title.getText().toString());
        assertEquals("Jun 1, 2026", holder.date.getText().toString());
        assertEquals("Waiting", holder.chip.getText().toString());
        assertEquals(
                "Your entry is in the waiting list. We will notify you when the organizer updates the draw outcome.",
                holder.supporting.getText().toString()
        );
        assertEquals("Awaiting selection outcome", holder.footer.getText().toString());
        assertEquals(View.GONE, holder.actionDot.getVisibility());
        assertSame(ticket, clickedTicket[0]);
    }

    /**
     * Verifies that a private event invitation uses the private-invite copy and highlights the
     * action-required indicator.
     */
    @Test
    public void activeTicketsAdapter_bindsPrivateInvitationVariant() {
        ContextThemeWrapper context = themedContext();
        TicketUIModel ticket = new TicketUIModel(
                "private-invite-event",
                "Invite Lounge",
                "Jun 9, 2026",
                TicketUIModel.Status.ACTION_REQUIRED,
                true
        );
        ActiveTicketsAdapter adapter = new ActiveTicketsAdapter(selectedTicket -> { });
        adapter.submitList(Collections.singletonList(ticket));
        FrameLayout parent = new FrameLayout(context);
        ActiveTicketsAdapter.TicketViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals("Private Invite", holder.chip.getText().toString());
        assertEquals(
                "You were invited to join the waitlist for this private event. Accept to join the waitlist, or decline to dismiss the invite.",
                holder.supporting.getText().toString()
        );
        assertEquals("Tap to join or decline", holder.footer.getText().toString());
        assertEquals(View.VISIBLE, holder.actionDot.getVisibility());
    }

    /**
     * Verifies that a confirmed attending ticket binds its title, date, location, status, and
     * click callback.
     */
    @Test
    public void attendingTicketsAdapter_bindsConfirmedTicketAndDispatchesClicks() {
        ContextThemeWrapper context = themedContext();
        AttendingTicketUIModel ticket = new AttendingTicketUIModel(
                "attending-event",
                "VIP Concert",
                "Jun 12, 2026",
                "Winspear Centre",
                "General admission",
                "Bring your QR pass",
                "View ticket"
        );
        AttendingTicketUIModel[] clickedTicket = new AttendingTicketUIModel[1];
        AttendingTicketsAdapter adapter = new AttendingTicketsAdapter(selectedTicket -> clickedTicket[0] = selectedTicket);
        adapter.submitList(Collections.singletonList(ticket));
        FrameLayout parent = new FrameLayout(context);
        AttendingTicketsAdapter.AttendingTicketViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performClick();

        assertEquals("VIP Concert", holder.title.getText().toString());
        assertEquals("Jun 12, 2026", holder.date.getText().toString());
        assertEquals("Winspear Centre", holder.location.getText().toString());
        assertEquals(View.VISIBLE, holder.location.getVisibility());
        assertEquals("General admission", holder.ticketType.getText().toString());
        assertEquals("Confirmed", holder.statusChip.getText().toString());
        assertSame(ticket, clickedTicket[0]);
    }

    /**
     * Verifies that attending tickets hide the optional location label when the mapped value is
     * blank.
     */
    @Test
    public void attendingTicketsAdapter_hidesLocationWhenTheLabelIsBlank() {
        ContextThemeWrapper context = themedContext();
        AttendingTicketUIModel ticket = new AttendingTicketUIModel(
                "attending-event-no-location",
                "Indoor Climb",
                "Jun 15, 2026",
                "   ",
                "Lottery spot",
                "Check in at the desk",
                "View ticket"
        );
        AttendingTicketsAdapter adapter = new AttendingTicketsAdapter(selectedTicket -> { });
        adapter.submitList(Collections.singletonList(ticket));
        FrameLayout parent = new FrameLayout(context);
        AttendingTicketsAdapter.AttendingTicketViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);

        assertEquals(View.GONE, holder.location.getVisibility());
    }

    /**
     * Verifies that an archived past-event card binds its summary fields and dispatches the bound
     * event when tapped.
     */
    @Test
    public void pastTicketsAdapter_bindsArchivedTicketContentAndDispatchesClicks() {
        ContextThemeWrapper context = themedContext();
        PastEventUIModel event = new PastEventUIModel(
                "Past Gala",
                "May 2, 2026",
                "Downtown Ballroom",
                "Completed",
                "Attendance recorded successfully",
                R.drawable.ic_ticket
        );
        PastEventUIModel[] clickedEvent = new PastEventUIModel[1];
        PastTicketsAdapter adapter = new PastTicketsAdapter(selectedEvent -> clickedEvent[0] = selectedEvent);
        adapter.submitList(Collections.singletonList(event));
        FrameLayout parent = new FrameLayout(context);
        PastTicketsAdapter.PastTicketViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performClick();

        assertEquals("Past Gala", holder.title.getText().toString());
        assertEquals("May 2, 2026", holder.date.getText().toString());
        assertEquals("Downtown Ballroom", holder.location.getText().toString());
        assertEquals("Attendance recorded successfully", holder.detail.getText().toString());
        assertEquals("Completed", holder.statusChip.getText().toString());
        assertSame(event, clickedEvent[0]);
    }

    /**
     * Creates a themed context so adapter row inflation matches the app under test.
     *
     * @return Application context wrapped in the Breezeseas theme.
     */
    private ContextThemeWrapper themedContext() {
        return new ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(),
                R.style.Theme_Breezeseas
        );
    }
}
