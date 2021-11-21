package com.example.maplogin.struct;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class LocationInfo implements Info {
    public String name;
    public String address;
    public String description;
    public HashMap<Long, String> imageUrls;

    public HashMap<String, Boolean> questions;

    public LocationInfo() {}

    public LocationInfo(String name,
                        String address,
                        String description,
                        HashMap<Long, String> imageUrls,
                        HashMap<String, Boolean> questions) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.imageUrls = imageUrls;
        this.questions = questions;
    }
}
