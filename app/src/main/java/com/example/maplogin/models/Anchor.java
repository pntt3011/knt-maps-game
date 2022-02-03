package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Anchor {
    public String userID;
    public String modelID;

    public Anchor() {
    }

    public Anchor(String userID, String modelID) {
        this.userID = userID;
        this.modelID = modelID;
    }

    public Anchor(Anchor other) {
        this.userID = other.userID;
        this.modelID = other.modelID;
    }
}
