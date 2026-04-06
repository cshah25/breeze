package com.example.breeze_seas;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager2.widget.ViewPager2;

import static androidx.test.espresso.Espresso.onView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * Instrumentation tests for the Tickets tab host fragment.
 *
 * <p>These tests verify that the custom tab controls drive the pager to the expected page and
 * that the selected page survives fragment recreation.
 */
@RunWith(AndroidJUnit4.class)
public class TicketsFragmentTest {

    private final TicketDB ticketDb = TicketDB.getInstance();
    private FragmentScenario<TicketsFragment> scenario;

    /**
     * Seeds deterministic ticket-tab data and launches the Tickets host fragment under test.
     */
    @Before
    public void setUp() {
        ticketDb.setTestModeEnabled(true);
        ticketDb.resetForTesting();
        scenario = FragmentScenario.launchInContainer(
                TicketsFragment.class,
                null,
                R.style.Theme_Breezeseas,
                (FragmentFactory) null
        );
        publishSeededTickets();
    }

    /**
     * Clears the seeded ticket state after each test so later instrumentation tests start clean.
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
     * Verifies that tapping each custom Tickets tab moves the pager to the matching page index.
     */
    @Test
    public void ticketsScreen_switchesPagerIndexWhenTabsArePressed() {
        assertCurrentPage(0);

        onView(withId(R.id.tickets_tab_attending)).perform(click());
        assertCurrentPage(1);

        onView(withId(R.id.tickets_tab_past)).perform(click());
        assertCurrentPage(2);
    }

    /**
     * Verifies that the Tickets host restores the previously selected page after recreation.
     */
    @Test
    public void ticketsScreen_restoresSelectedPageAfterRecreation() {
        onView(withId(R.id.tickets_tab_past)).perform(click());

        scenario.recreate();

        assertCurrentPage(2);
    }

    /**
     * Publishes one seeded item into each ticket tab so child fragments can render stable content
     * while the pager tests run.
     */
    private void publishSeededTickets() {
        scenario.onFragment(fragment -> ticketDb.publishTicketsForTesting(
                Collections.singletonList(
                        new TicketUIModel(
                                "event-active-1",
                                "Night Swim",
                                "Jun 1, 2026",
                                TicketUIModel.Status.PENDING
                        )
                ),
                Collections.singletonList(
                        new AttendingTicketUIModel(
                                "event-attending-1",
                                "VIP Concert",
                                "Jun 12, 2026",
                                "Winspear Centre",
                                "General admission",
                                "Bring your code to the door.",
                                "View ticket"
                        )
                ),
                Collections.singletonList(
                        new PastEventUIModel(
                                "Past Gala",
                                "May 2, 2026",
                                "Downtown Ballroom",
                                "Completed",
                                "Attendance recorded successfully",
                                R.drawable.ic_ticket
                        )
                )
        ));
    }

    /**
     * Waits for the UI to settle, then verifies the pager index owned by {@link TicketsFragment}.
     *
     * @param expectedPage Zero-based page index expected after the tab interaction.
     */
    private void assertCurrentPage(int expectedPage) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        scenario.onFragment(fragment -> {
            ViewPager2 viewPager = fragment.requireView().findViewById(R.id.tickets_view_pager);
            assertEquals(expectedPage, viewPager.getCurrentItem());
        });
    }
}
