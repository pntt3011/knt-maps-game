package com.example.maplogin.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;

import com.example.maplogin.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserRepository {
    private final static String FOLLOW_NODE = "follows";
    private final MediatorLiveData<HashMap<String, User>> allUsers;
    private final DatabaseReference allUsersRef;

    public UserRepository() {
        allUsersRef = FirebaseDatabase
                .getInstance(Constants.DATABASE_URI)
                .getReference(Constants.USER_INFO_ROOT);
        allUsers = new MediatorLiveData<>();
        bindUsers();
    }

    private void bindUsers() {
        FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(allUsersRef);
        GenericTypeIndicator<HashMap<String, User>> type = new GenericTypeIndicator<HashMap<String, User>>() {};
        allUsers.addSource(liveData, users -> {
            if (users != null) {
                new Thread(() -> allUsers.postValue(users.getValue(type))).start();
            } else {
                allUsers.postValue(new HashMap<>());
            }
        });
    }

    public void subscribe(String follower, String followee, MediatorLiveData<String> followState) {
        allUsersRef.child(followee).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    allUsersRef.child(follower).child(FOLLOW_NODE).push().setValue(followee);
                    followState.setValue("OK");
                }
                else {
                    followState.setValue("ERROR");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public MediatorLiveData<HashMap<String, User>> getFollowsLiveData(String uid) {
        FirebaseQueryLiveData userFollowLiveData =
                new FirebaseQueryLiveData(allUsersRef.child(uid).child(FOLLOW_NODE));
        MediatorLiveData<HashMap<String, User>> followLiveData = new MediatorLiveData<>();

        followLiveData.addSource(userFollowLiveData, followList -> {
            if (followList != null) {
                GenericTypeIndicator<List<String>> type = new GenericTypeIndicator<List<String>>(){};
                List<String> data = followList.getValue(type);
                if (data == null)
                    data = new ArrayList<>();
                HashMap<String, User> followInfos = getUserInfos(data);
                followLiveData.postValue(followInfos);
            }
            else {
                followLiveData.setValue(new HashMap<>());
            }
        });
        return followLiveData;
    }

    private HashMap<String, User> getUserInfos(List<String> data) {
        HashMap<String, User> userInfos = new HashMap<>();
        for (String id: data) {
            User userInfo = getUserInfo(id);
            if (userInfo != null)
                userInfos.put(id, userInfo);
        }
        return userInfos;
    }

    private User getUserInfo(String id) {
        HashMap<String, User> allUsersMap = allUsers.getValue();
        if (allUsersMap != null)
            return allUsersMap.getOrDefault(id, null);
        return null;
    }
}