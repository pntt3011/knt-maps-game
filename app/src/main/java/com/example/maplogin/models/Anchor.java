package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Anchor {
    public String userID;
    public String modelID;
    public String comment;

    public Anchor() {
    }

    public Anchor(String userID, String modelID, String comment) {
        this.userID = userID;
        this.modelID = modelID;
        this.comment = comment;
    }

    public Anchor(Anchor other) {
        this.userID = other.userID;
        this.modelID = other.modelID;
        this.comment = other.comment;
    }
}
