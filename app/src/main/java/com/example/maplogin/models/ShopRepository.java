package com.example.maplogin.models;

import androidx.lifecycle.MediatorLiveData;

import com.example.maplogin.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;

public class ShopRepository {
    private final MediatorLiveData<HashMap<String, ShopItem>> allItems;
    private final DatabaseReference shopRef;

    public ShopRepository() {
        shopRef = FirebaseDatabase
                .getInstance(Constants.DATABASE_URI)
                .getReference(Constants.SHOP_ITEMS_INFO_ROOT);
        allItems = new MediatorLiveData<>();
        bindItems();
    }

    private void bindItems() {
        FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(shopRef);
        GenericTypeIndicator<HashMap<String, ShopItem>> type =
                new GenericTypeIndicator<HashMap<String, ShopItem>>() {};
        allItems.addSource(liveData, users -> {
            if (users != null) {
                new Thread(() -> allItems.postValue(users.getValue(type))).start();
            } else {
                allItems.postValue(new HashMap<>());
            }
        });
    }

    public MediatorLiveData<HashMap<String, ShopItem>> getItemsLiveData() {
        return allItems;
    }
}
