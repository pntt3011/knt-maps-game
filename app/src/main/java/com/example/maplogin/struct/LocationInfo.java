package com.example.maplogin.struct;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;

@IgnoreExtraProperties
public class LocationInfo implements Info {
    public String name;
    public String phone;
    public String email;
    public String address;
    public String description;
    public ArrayList<String> imageUrls;
    public ArrayList<String> questions;

    public LocationInfo() {}

    public LocationInfo(String name,
                        String phone,
                        String email,
                        String address,
                        String description,
                        ArrayList<String> imageUrls,
                        ArrayList<String> questions) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.description = description;
        this.imageUrls = imageUrls;
        this.questions = questions;
    }
}
