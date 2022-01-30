package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String name;
    public String photo_url;
    public Map<String, Boolean> items;
    public Map<String, Boolean> follows;
    public Map<String, UserLocation> captured;
    public Map<String, UserLocation> failed;

    public User() {
    }

    public User(String name,
                String photo_url,
                Map<String, Boolean> follows,
                Map<String, Boolean> items,
                Map<String, UserLocation> captured,
                Map<String, UserLocation> failed) {
        this.name = name;
        this.photo_url = photo_url;
        this.items = items;
        this.follows = follows;
        this.captured = captured;
        this.failed = failed;
    }
}
