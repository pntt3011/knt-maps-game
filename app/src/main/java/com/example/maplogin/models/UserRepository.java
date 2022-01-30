package com.example.maplogin.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;

import com.example.maplogin.ui.follow.FollowViewModel.FollowState;
import com.example.maplogin.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final static String FOLLOW_NODE = "follows";
    private final static String ITEMS_NODE = "items";
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

    public void subscribe(String follower, String followee, MediatorLiveData<FollowState> followState) {
        allUsersRef.child(followee).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    allUsersRef.child(follower).child(FOLLOW_NODE).child(followee).setValue(Boolean.TRUE);
                    followState.setValue(FollowState.OK);
                }
                else {
                    followState.setValue(FollowState.ERROR);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void unsubscribe(String follower, String followee) {
        allUsersRef.child(follower).child(FOLLOW_NODE).child(followee).removeValue();
    }

    public void buyItem(String uid, String itemId) {
        DatabaseReference userRef = allUsersRef.child(uid);
        userRef.child(ITEMS_NODE).child(itemId).setValue(Boolean.FALSE);
    }

    public void selectItem(String uid, String itemId) {
        DatabaseReference userItemRef = allUsersRef.child(uid).child(ITEMS_NODE);
        userItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<HashMap<String, Boolean>> type =
                            new GenericTypeIndicator<HashMap<String, Boolean>>() {};
                    HashMap<String, Boolean> ownedItems = snapshot.getValue(type);

                    if (ownedItems == null)
                        ownedItems = new HashMap<>();

                    ownedItems.replaceAll((k, v) -> Boolean.FALSE);
                    ownedItems.put(itemId, Boolean.TRUE);

                }
                else {
                    userItemRef.child(itemId).setValue(Boolean.TRUE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public MediatorLiveData<User> getUserLiveData(String uid) {
        FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(allUsersRef.child(uid));
        MediatorLiveData<User> userLiveData = new MediatorLiveData<>();
        userLiveData.addSource(liveData, user -> {
            if (user != null) {
                new Thread(() -> userLiveData.postValue(user.getValue(User.class))).start();
            } else {
                userLiveData.postValue(new User());
            }
        });
        return userLiveData;
    }

    public MediatorLiveData<HashMap<String, User>> getFollowsLiveData(String uid) {
        MediatorLiveData<HashMap<String, User>> followLiveData = new MediatorLiveData<>();
        followLiveData.addSource(allUsers, users -> {
            if (users != null) {
                Map<String, Boolean> data = getUserFollows(uid);
                HashMap<String, User> followInfos = getUserInfos(data);
                followLiveData.postValue(followInfos);
            }
            else {
                followLiveData.setValue(new HashMap<>());
            }
        });
        return followLiveData;
    }

    private Map<String, Boolean> getUserFollows(String id) {
        User user = getUserInfo(id);
        Map<String, Boolean> data;
        if (user == null || user.follows == null)
            data = new HashMap<>();
        else
            data = user.follows;
        return data;
    }

    private User getUserInfo(String id) {
        HashMap<String, User> allUsersMap = allUsers.getValue();
        if (allUsersMap != null)
            return allUsersMap.getOrDefault(id, null);
        return null;
    }

    private HashMap<String, User> getUserInfos(Map<String, Boolean> data) {
        HashMap<String, User> userInfos = new HashMap<>();
        for (String id: data.keySet()) {
            User userInfo = getUserInfo(id);
            if (userInfo != null)
                userInfos.put(id, userInfo);
        }
        return userInfos;
    }
}
