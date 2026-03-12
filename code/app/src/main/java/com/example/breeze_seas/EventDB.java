package com.example.breeze_seas;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventDB {
    private static FirebaseFirestore db;
    private static CollectionReference eventRef;
    private static boolean setup = false;

    private EventDB() {
    }

    private void setup() {
        if (!setup) {
            db = DBConnector.getDb();
            eventRef = db.collection("events");
            setup = true;
        }
    }


    // Takes event object and returns a hashmap
    private Map<String, Object> getMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", event.getEventId());
        map.put("organizerId", event.getOrganizerId());
        map.put("name", event.getName());
        map.put("description", event.getDescription());
        map.put("image", event.getImage());
        map.put("qrValue", event.getQrValue());
        map.put("dateCreated", event.getDateCreated());
        map.put("dateModified", event.getDateModified());
        map.put("registrationStartDate", event.getRegistrationStartDate());
        map.put("registrationEndDate", event.getRegistrationEndDate());
        map.put("eventStartDate", event.getEventStartDate());
        map.put("eventEndDate", event.getEventEndDate());
        map.put("geolocationEnforced", event.isGeolocationEnforced());
        map.put("eventCapacity", event.getEventCapacity());
        map.put("waitingListCapacity", event.getWaitingListCapacity());
        map.put("drawARound", event.getDrawARound());
        return map;
    }


    // Add Event to DB
    public static void addEvent(Event event) {
//        eventRef
//                .add()

    }

    // Get all events
    public static ArrayList<Event> getAllEvents() {

    }

    // Get event by id
    public static Event getEventById(String eventId) {

    }



    public interface LoadSingleEventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }

//    public void createEvent(Event event, @NonNull AddEventCallback callback) {
//        //eventRef.add().addOnCompleteListener(callback.on)
//    }


    // Get all Events
    // Get event by id
    // Add Event

}
