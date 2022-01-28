package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String name;
    public String photo_url;
    public Map<String, Boolean> follows;
    public Map<String, Long> captured;

    public User() {
    }

    public User(String name, String photo_url, Map<String, Boolean> follows, Map<String, Long> captured) {
        this.name = name;
        this.photo_url = photo_url;
        this.follows = follows;
        this.captured = captured;
    }
}
