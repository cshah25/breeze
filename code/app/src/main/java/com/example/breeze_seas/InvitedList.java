package com.example.breeze_seas;

import android.widget.BaseAdapter;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class InvitedList {
    private String event;
    private ArrayList<User> invitedList;
    private FirebaseFirestore db;
    public InvitedList(String event){
        this.event=event;
        this.invitedList=new ArrayList<User>();
        this.db=DBConnector.getDb();
    }

    public ArrayList<User> getInitialList() {
        return invitedList;
    }

    public void getInvitedList(android.widget.BaseAdapter adapter,Runnable onFinish){
        CollectionReference list=db.collection("Events").document(event)
                .collection("WaitingList");
        list.get().addOnCompleteListener(op->{
            if (op.isSuccessful() && op.getResult()!=null){
                invitedList.clear();
                for(DocumentSnapshot doc: op.getResult()){
                    if("Pending".equals(doc.get("Status"))){
                        User invitedEntrant=doc.toObject(User.class);
                        invitedList.add(invitedEntrant);
                    }
                }
                if (adapter != null) adapter.notifyDataSetChanged();
            }
            if (onFinish!=null){
                onFinish.run();
            }

        });
    }

}
