package com.example.maplogin.models;

import androidx.lifecycle.MediatorLiveData;

import com.example.maplogin.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;

public class AnchorRepository {
    private final MediatorLiveData<HashMap<String, Anchor>> allAnchors;
    private final DatabaseReference anchorsRef;

    public AnchorRepository() {
        anchorsRef = FirebaseDatabase
                .getInstance(Constants.DATABASE_URI)
                .getReference(Constants.ANCHORS_INFO_ROOT);
        allAnchors = new MediatorLiveData<>();
        bindItems();
    }

    private void bindItems() {
        FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(anchorsRef);
        GenericTypeIndicator<HashMap<String, Anchor>> type =
                new GenericTypeIndicator<HashMap<String, Anchor>>() {};
        allAnchors.addSource(liveData, anchors -> {
            if (anchors != null) {
                new Thread(() -> allAnchors.postValue(anchors.getValue(type))).start();
            } else {
                allAnchors.postValue(new HashMap<>());
            }
        });
    }

    public MediatorLiveData<HashMap<String, Anchor>> getAnchorsLiveData() {
        return allAnchors;
    }

    public void placeModel(String aid, Anchor anchor) {
        anchorsRef.child(aid).setValue(anchor);
    }
}
