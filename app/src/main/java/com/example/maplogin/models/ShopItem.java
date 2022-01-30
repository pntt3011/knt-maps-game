package com.example.maplogin.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ShopItem {
    public String name;
    public String model;
    public String photo;
    public Long point;

    public ShopItem() {
    }

    public ShopItem(String name, String model, String url, Long point) {
        this.name = name;
        this.model = model;
        this.photo = url;
        this.point = point;
    }

    public ShopItem(ShopItem other) {
        this.name = other.name;
        this.model = other.model;
        this.photo = other.photo;
        this.point = other.point;
    }
}
