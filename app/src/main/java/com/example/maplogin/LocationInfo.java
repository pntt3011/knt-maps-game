package com.example.maplogin;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class LocationInfo implements Info {
    public String name;
    public String description;
    public String imageUrl;

    public HashMap<String, Boolean> questions;

    public LocationInfo() {}

    public LocationInfo(String name,
                        String description,
                        String imageUrl,
                        HashMap<String, Boolean> questions) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.questions = questions;
    }

}
