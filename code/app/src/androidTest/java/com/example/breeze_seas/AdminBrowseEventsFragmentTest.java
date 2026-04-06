package com.example.breeze_seas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseEventsFragmentTest {

    /**
     * Builds a basic Event with the given name and a unique event ID.
     */
    private Event makeEvent(String name, String eventId) {
        Event event = new Event("organizer-id", 10);
        event.setEventId(eventId);
        event.setName(name);
        event.setDescription("Description for " + name);
        return event;
    }

    /**
     * Launches AdminBrowseEventsFragment in an isolated container, pre-populates
     * AdminViewModel's events LiveData with the given list before the fragment resumes,
     * then sets it again immediately after resume to override any async Firestore response.
     */
    private FragmentScenario<AdminBrowseEventsFragment> launchWithEvents(List<Event> events) {
        FragmentScenario<AdminBrowseEventsFragment> scenario = FragmentScenario.launchInContainer(
                AdminBrowseEventsFragment.class,
                null,
                R.style.Theme_Breezeseas,
                Lifecycle.State.INITIALIZED
        );

        scenario.onFragment(fragment -> {
            AdminViewModel viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(AdminViewModel.class);
            viewModel.getEvents().setValue(new ArrayList<>(events));
        });

        scenario.moveToState(Lifecycle.State.RESUMED);

        scenario.onFragment(fragment -> {
            AdminViewModel viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(AdminViewModel.class);
            viewModel.getEvents().setValue(new ArrayList<>(events));
        });

        return scenario;
    }

    /**
     * Verifies that three mock events injected via AdminViewModel all appear in the
     * RecyclerView, confirming that events flow correctly from LiveData through the
     * adapter to the displayed list.
     */
    @Test
    public void testEventsListShowsMockEvents() {
        List<Event> events = Arrays.asList(
                makeEvent("Summer Festival", "event-1"),
                makeEvent("Winter Gala", "event-2"),
                makeEvent("Spring Fair", "event-3")
        );

        FragmentScenario<AdminBrowseEventsFragment> scenario = launchWithEvents(events);

        scenario.onFragment(fragment -> {
            RecyclerView rv = fragment.requireView().findViewById(R.id.abe_rv_events_list);
            assertNotNull(rv);
            assertEquals(3, rv.getAdapter().getItemCount());
        });
    }
}
