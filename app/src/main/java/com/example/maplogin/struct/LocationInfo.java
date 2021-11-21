package com.example.maplogin.struct;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class LocationInfo implements Info {
    public Double latitude;
    public Double longitude;
    public String iconUrl;
    public String name;
    public String phone;
    public String email;
    public String address;
    public String description;
    public ArrayList<String> imageUrls;
    public ArrayList<String> questions;

    public LocationInfo() {}

    public LocationInfo(Double latitude,
                        Double longitude,
                        String iconUrl,
                        String name,
                        String phone,
                        String email,
                        String address,
                        String description,
                        ArrayList<String> imageUrls,
                        ArrayList<String> questions) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.iconUrl = iconUrl;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.description = description;
        this.imageUrls = imageUrls;
        this.questions = questions;
    }
}
