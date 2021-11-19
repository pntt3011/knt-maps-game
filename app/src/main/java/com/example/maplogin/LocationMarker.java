package com.example.maplogin;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LocationMarker {
    public Double latitude;
    public Double longitude;
    public String iconUrl;

    public LocationMarker() {}

    public LocationMarker(Double latitude, Double longitude, String iconUrl) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.iconUrl = iconUrl;
    }
}
