package com.example.breeze_seas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputEditText;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * Instrumentation tests for organizer event-creation validation logic.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventValidationTest {

    /**
     * Verifies that create-event rejects schedules where registration closes after the event starts
     * and shows the organizer-facing conflict message instead of proceeding with creation.
     *
     * @throws Exception When reflection-based test setup fails.
     */
    @Test
    public void onCreateClicked_registrationEndsAfterEventStartShowsConflictToast() throws Exception {
        FragmentScenario<CreateEventFragment> scenario =
                FragmentScenario.launchInContainer(CreateEventFragment.class, null, R.style.Theme_Breezeseas, null);
        View[] decorViewHolder = new View[1];

        scenario.onFragment(fragment -> {
            decorViewHolder[0] = fragment.requireActivity().getWindow().getDecorView();

            TextInputEditText eventNameInput = fragment.requireView().findViewById(R.id.etEventName);
            TextInputEditText capacityInput = fragment.requireView().findViewById(R.id.etCapacity);
            TextInputEditText regFromInput = fragment.requireView().findViewById(R.id.etRegFrom);
            TextInputEditText regToInput = fragment.requireView().findViewById(R.id.etRegTo);
            TextInputEditText eventStartInput = fragment.requireView().findViewById(R.id.etEventStart);
            TextInputEditText eventEndInput = fragment.requireView().findViewById(R.id.etEventEnd);

            Calendar regFromCalendar = Calendar.getInstance();
            regFromCalendar.clear();
            regFromCalendar.set(2030, Calendar.MARCH, 10, 9, 0, 0);

            Calendar regToCalendar = Calendar.getInstance();
            regToCalendar.clear();
            regToCalendar.set(2030, Calendar.MARCH, 12, 11, 0, 0);

            Calendar eventStartCalendar = Calendar.getInstance();
            eventStartCalendar.clear();
            eventStartCalendar.set(2030, Calendar.MARCH, 12, 10, 0, 0);

            Calendar eventEndCalendar = Calendar.getInstance();
            eventEndCalendar.clear();
            eventEndCalendar.set(2030, Calendar.MARCH, 12, 12, 0, 0);

            long regFromMillis = regFromCalendar.getTimeInMillis();
            long regToMillis = regToCalendar.getTimeInMillis();
            long eventStartMillis = eventStartCalendar.getTimeInMillis();
            long eventEndMillis = eventEndCalendar.getTimeInMillis();

            eventNameInput.setText("Conflicting Schedule Event");
            capacityInput.setText("25");
            regFromInput.setText(EventMetadataUtils.formatDateTime(regFromMillis));
            regToInput.setText(EventMetadataUtils.formatDateTime(regToMillis));
            eventStartInput.setText(EventMetadataUtils.formatDateTime(eventStartMillis));
            eventEndInput.setText(EventMetadataUtils.formatDateTime(eventEndMillis));

            setPrivateLongField(fragment, "regFromMillis", regFromMillis);
            setPrivateLongField(fragment, "regToMillis", regToMillis);
            setPrivateLongField(fragment, "eventStartMillis", eventStartMillis);
            setPrivateLongField(fragment, "eventEndMillis", eventEndMillis);
        });

        onView(withId(R.id.btnCreate)).perform(click());

        onView(withText(R.string.create_event_schedule_conflict_error))
                .inRoot(withDecorView(not(is(decorViewHolder[0]))))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCreate)).check(matches(isDisplayed()));
    }

    /**
     * Sets one private millisecond field on the create-event fragment so the validation flow can
     * run without opening date and time pickers during the test.
     *
     * @param fragment Fragment under test.
     * @param fieldName Private field name to update.
     * @param value Millisecond value to store.
     * @throws Exception When reflection access fails.
     */
    private void setPrivateLongField(
            CreateEventFragment fragment,
            String fieldName,
            long value
    ) throws Exception {
        Field field = CreateEventFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
    }
}
