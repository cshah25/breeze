package com.example.breeze_seas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.Timestamp;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Unit tests for organizer-facing event data flow used by the Organize landing page and event
 * preview screens.
 */
public class EventDataFlowTest {

    /**
     * Forces Architecture Components work onto the test thread so {@link Event} can create and
     * hydrate {@link androidx.lifecycle.MutableLiveData} safely in the JVM test environment.
     */
    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Verifies that organizer-facing event metadata survives the create-to-map-to-hydrated-event
     * round trip used by the Organize landing page and event preview.
     */
    @Test
    public void toMapAndLoadMap_preservesOrganizerFacingFields() {
        Timestamp createdTimestamp = new Timestamp(new Date(1893286800000L));
        Timestamp modifiedTimestamp = new Timestamp(new Date(1893290400000L));
        Timestamp registrationStart = new Timestamp(new Date(1893373200000L));
        Timestamp registrationEnd = new Timestamp(new Date(1893459600000L));
        Timestamp eventStart = new Timestamp(new Date(1893546000000L));
        Timestamp eventEnd = new Timestamp(new Date(1893632400000L));

        Event originalEvent = new Event(
                "event-data-flow",
                true,
                "organizer-device-id",
                new ArrayList<>(),
                "Private Showcase",
                "Invite-only launch event",
                null,
                createdTimestamp,
                modifiedTimestamp,
                registrationStart,
                registrationEnd,
                eventStart,
                eventEnd,
                true,
                120,
                30,
                2,
                null,
                null,
                null,
                null
        );

        Map<String, Object> serializedEvent = originalEvent.toMap();
        Event hydratedEvent = new Event(serializedEvent);

        assertTrue(hydratedEvent.isPrivate());
        assertEquals("event-data-flow", hydratedEvent.getEventId());
        assertEquals("organizer-device-id", hydratedEvent.getOrganizerId());
        assertEquals("Private Showcase", hydratedEvent.getName());
        assertEquals("Invite-only launch event", hydratedEvent.getDescription());
        assertEquals(createdTimestamp, hydratedEvent.getCreatedTimestamp());
        assertEquals(modifiedTimestamp, hydratedEvent.getModifiedTimestamp());
        assertEquals(registrationStart, hydratedEvent.getRegistrationStartTimestamp());
        assertEquals(registrationEnd, hydratedEvent.getRegistrationEndTimestamp());
        assertEquals(eventStart, hydratedEvent.getEventStartTimestamp());
        assertEquals(eventEnd, hydratedEvent.getEventEndTimestamp());
        assertTrue(hydratedEvent.isGeolocationEnforced());
        assertEquals(120, hydratedEvent.getEventCapacity());
        assertEquals(30, hydratedEvent.getWaitingListCapacity());
        assertEquals(2, hydratedEvent.getDrawARound());
    }
}
