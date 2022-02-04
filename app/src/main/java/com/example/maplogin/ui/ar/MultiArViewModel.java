package com.example.maplogin.ui.ar;

import androidx.lifecycle.MediatorLiveData;

import com.example.maplogin.models.Anchor;
import com.example.maplogin.models.AnchorRepository;
import com.example.maplogin.models.User;
import com.example.maplogin.utils.CombinedLiveData;

import java.util.HashMap;
import java.util.Map;

public class MultiArViewModel extends ArViewModel{
    private final AnchorRepository anchorRepository;
    private final MediatorLiveData<HashMap<String, User>> usersLiveData;
    private final MediatorLiveData<HashMap<String, Anchor>> anchorsLiveData;
    private final MediatorLiveData<HashMap<String, AnchorExt>> anchorsInfoLiveData;

    public MultiArViewModel() {
        super();
        anchorRepository = new AnchorRepository();
        usersLiveData = userRepository.getAllUsersLiveData();
        anchorsLiveData = anchorRepository.getAnchorsLiveData();
        anchorsInfoLiveData = combineAnchorsInfoLiveData();
    }

    public static class AnchorExt extends Anchor {
        public User userInfo;

        public AnchorExt(Anchor anchor, User userInfo) {
            super(anchor);
            this.userInfo = userInfo;
        }
    }

    private MediatorLiveData<HashMap<String, AnchorExt>> combineAnchorsInfoLiveData() {
        return new CombinedLiveData<>(
                anchorsLiveData,
                usersLiveData,
                (anchors, users) -> {
                    if (anchors == null)
                        anchors = new HashMap<>();

                    if (users == null)
                        users = new HashMap<>();

                    HashMap<String, AnchorExt> anchorsInfo = new HashMap<>();
                    for (Map.Entry<String, Anchor> entry: anchors.entrySet()) {
                        String userID = entry.getValue().userID;
                        if (userID == null)
                            continue;

                        if (users.containsKey(userID)) {
                            AnchorExt anchorExt = new AnchorExt(entry.getValue(), users.get(userID));
                            anchorsInfo.put(entry.getKey(), anchorExt);
                        }
                    }
                    return anchorsInfo;
                }
        );
    }

    public MediatorLiveData<HashMap<String, AnchorExt>> getAnchorsInfoLiveData() {
        return anchorsInfoLiveData;
    }

    public void placeAnchor(String anchorId, String comment) {
        if (currentItemLiveData.getValue() == null)
            return;

        String modelId = currentItemLiveData.getValue().model;
        Anchor anchor = new Anchor(uid, modelId, comment);

        anchorRepository.placeModel(anchorId, anchor);
    }
}
