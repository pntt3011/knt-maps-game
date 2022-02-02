package com.example.maplogin.ui.ar;

import static com.example.maplogin.utils.Constants.DEFAULT_ITEM;

import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.maplogin.models.ShopItem;
import com.example.maplogin.models.ShopRepository;
import com.example.maplogin.models.User;
import com.example.maplogin.models.UserRepository;
import com.example.maplogin.utils.CombinedLiveData;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class ArViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MediatorLiveData<HashMap<String, ShopItem>> itemsLiveData;
    private final MediatorLiveData<User> userLiveData;
    private final MediatorLiveData<HashMap<String, ShopItemExt>> ownedItemsLiveData;
    private final String uid;

    public ArViewModel() {
        userRepository = new UserRepository();
        ShopRepository shopRepository = new ShopRepository();
        uid = FirebaseAuth.getInstance().getUid();

        itemsLiveData = shopRepository.getItemsLiveData();
        userLiveData = userRepository.getUserLiveData(uid);
        ownedItemsLiveData = combineOwnedItemsLiveData();
    }

    public static class ShopItemExt extends ShopItem {
        public boolean inUsed;

        public ShopItemExt(ShopItem item, Boolean inUsed) {
            super(item);
            this.inUsed = inUsed;
        }
    }

    private MediatorLiveData<HashMap<String, ShopItemExt>> combineOwnedItemsLiveData() {
        return new CombinedLiveData<>(
                itemsLiveData,
                userLiveData,
                (items, user) -> {
                    if (items == null) {
                        items = new HashMap<>();
                    }

                    Map<String, Boolean> userItems;
                    if (user == null || user.items == null) {
                        userItems = new HashMap<>();
                    } else {
                        userItems = user.items;
                    }

                    HashMap<String, ShopItemExt> ownedItems = new HashMap<>();
                    for (Map.Entry<String, ShopItem> entry : items.entrySet()) {
                        if (userItems.containsKey(entry.getKey()))
                            ownedItems.put(
                                    entry.getKey(),
                                    new ShopItemExt(
                                            entry.getValue(),
                                            userItems.get(entry.getKey())));
                    }
                    return ownedItems;
                });
    }

    public MediatorLiveData<ShopItem> getCurrentItemLiveData() {
        MediatorLiveData<ShopItem> currentItemLiveData = new MediatorLiveData<>();
        currentItemLiveData.addSource(ownedItemsLiveData, ownedItems -> {
            if (ownedItems != null) {
                for (Map.Entry<String, ShopItemExt> entry: ownedItems.entrySet()) {
                    if (entry.getValue().inUsed) {
                        currentItemLiveData.setValue(entry.getValue());
                        break;
                    }
                }
            } else {
                selectItem(DEFAULT_ITEM);
            }
        });
        return currentItemLiveData;
    }

    public MediatorLiveData<HashMap<String, ShopItemExt>> getOwnedItemsLiveData() {
        return ownedItemsLiveData;
    }

    public void selectItem(String itemId) {
        if (ownedItemsLiveData.getValue() == null)
            return;

        if (!ownedItemsLiveData.getValue().containsKey(itemId)) {
            return;
        }

        userRepository.selectItem(uid, itemId);
        Log.d("hehe", itemId);
    }
}
