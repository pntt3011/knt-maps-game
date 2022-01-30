package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserLocation {
    public Long score;
    public Long time;

    public UserLocation() {
    }

    public UserLocation(Long score, Long time) {
        this.score = score;
        this.time = time;
    }
}
