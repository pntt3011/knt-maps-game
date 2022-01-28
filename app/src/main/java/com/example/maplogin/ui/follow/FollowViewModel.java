package com.example.maplogin.ui.follow;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.maplogin.models.User;
import com.example.maplogin.models.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class FollowViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MediatorLiveData<String> followState;
    private final String uid;

    public FollowViewModel() {
        userRepository = new UserRepository();
        followState = new MediatorLiveData<>();
        uid = FirebaseAuth.getInstance().getUid();
    }

    public void followUser(String uid) {
        userRepository.subscribe(this.uid, uid, followState);
    }

    public MediatorLiveData<HashMap<String, User>> getFollowsLiveData() {
        return userRepository.getFollowsLiveData(uid);
    }

    public MediatorLiveData<String> getAddFollowResult() {
        return followState;
    }
}
