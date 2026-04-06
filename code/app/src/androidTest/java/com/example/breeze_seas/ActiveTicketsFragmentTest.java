package com.example.breeze_seas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
 * Instrumentation tests for {@link ActiveTicketsFragment}.
 *
 * <p>These tests verify that seeded {@link TicketDB} data is rendered into the active-ticket
 * recycler and that later cache updates are reflected in the fragment UI.
 */
@RunWith(AndroidJUnit4.class)
public class ActiveTicketsFragmentTest {

    private final TicketDB ticketDb = TicketDB.getInstance();
    private FragmentScenario<ActiveTicketsFragment> scenario;

    /**
     * Enables deterministic ticket seeding and launches the Active Tickets fragment.
     */
    @Before
    public void setUp() {
        ticketDb.setTestModeEnabled(true);
        ticketDb.resetForTesting();
        scenario = FragmentScenario.launchInContainer(
                ActiveTicketsFragment.class,
                null,
                R.style.Theme_Breezeseas,
                (FragmentFactory) null
        );
    }

    /**
     * Clears the seeded ticket cache after each test to avoid cross-test pollution.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        ticketDb.resetForTesting();
        ticketDb.setTestModeEnabled(false);
    }

    /**
     * Verifies that one seeded active ticket is rendered with its title, date, status, and footer
     * hint in the recycler view.
     */
    @Test
    public void activeTicketsFragment_rendersTicketDbContentInRecyclerView() {
        publishActiveTickets(Collections.singletonList(
                new TicketUIModel(
                        "event-active-1",
                        "Night Swim",
                        "Jun 1, 2026",
                        TicketUIModel.Status.PENDING
                )
        ));

        onView(withText("Night Swim")).check(matches(isDisplayed()));
        onView(withText("Jun 1, 2026")).check(matches(isDisplayed()));
        onView(withText("Waiting")).check(matches(isDisplayed()));
        onView(withText("Awaiting selection outcome")).check(matches(isDisplayed()));

        scenario.onFragment(fragment -> {
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.active_tickets_recycler);
            assertEquals(1, recyclerView.getAdapter().getItemCount());
        });
    }

    /**
     * Verifies that the fragment updates its recycler contents after the seeded ticket cache
     * changes from empty to one action-required ticket.
     */
    @Test
    public void activeTicketsFragment_updatesRenderedRowsWhenTicketDbPublishesChanges() {
        publishActiveTickets(Collections.emptyList());

        scenario.onFragment(fragment -> {
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.active_tickets_recycler);
            assertEquals(0, recyclerView.getAdapter().getItemCount());
        });

        publishActiveTickets(Collections.singletonList(
                new TicketUIModel(
                        "event-active-2",
                        "Pool Finals",
                        "Jul 9, 2026",
                        TicketUIModel.Status.ACTION_REQUIRED
                )
        ));

        onView(withText("Pool Finals")).check(matches(isDisplayed()));
        onView(withText("Action Required")).check(matches(isDisplayed()));
        onView(withText("Tap to accept or decline")).check(matches(isDisplayed()));

        scenario.onFragment(fragment -> {
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.active_tickets_recycler);
            assertEquals(1, recyclerView.getAdapter().getItemCount());
        });
    }

    /**
     * Publishes seeded Active-tab data while leaving the Attending and Past tabs empty.
     *
     * @param activeTickets Active-tab tickets that should be rendered by the fragment.
     */
    private void publishActiveTickets(List<TicketUIModel> activeTickets) {
        scenario.onFragment(fragment -> ticketDb.publishTicketsForTesting(
                activeTickets,
                Collections.emptyList(),
                Collections.emptyList()
        ));
    }
}
