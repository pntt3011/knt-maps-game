package com.example.maplogin.struct;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String name;
    public String photo_url;
    public Map<String, Long> captured;

    public User() {
    }

    public User(String name, String photo_url, Map<String, Long> captured) {
        this.name = name;
        this.photo_url = photo_url;
        this.captured = captured;
    }
}
