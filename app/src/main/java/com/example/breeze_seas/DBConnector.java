package com.example.breeze_seas;

import com.google.firebase.firestore.FirebaseFirestore;

//No class ever talks directly to Firebase anymore, they use this class
public class DBConnector {
    private static FirebaseFirestore db;

    //No one can instantiate DBConnect
    private DBConnector() {}

    // the only way other classes call to talk to Firestore.
    public static FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
}
