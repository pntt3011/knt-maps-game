package com.example.maplogin.ui.shop;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.maplogin.models.ShopItem;
import com.example.maplogin.models.ShopRepository;
import com.example.maplogin.models.User;
import com.example.maplogin.models.UserLocation;
import com.example.maplogin.models.UserRepository;
import com.example.maplogin.ui.shop.ShopRecyclerAdapter.ShopItemExt;
import com.example.maplogin.utils.CombinedLiveData;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ShopViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MediatorLiveData<HashMap<String, ShopItem>> itemsLiveData;
    private final MediatorLiveData<User> userLiveData;
    private final MediatorLiveData<BuyState> buyState;
    private final String uid;

    public enum BuyState {
        OK,
        ERROR
    }

    public ShopViewModel() {
        userRepository = new UserRepository();
        ShopRepository shopRepository = new ShopRepository();
        buyState = new MediatorLiveData<>();
        uid = FirebaseAuth.getInstance().getUid();

        itemsLiveData = shopRepository.getItemsLiveData();
        userLiveData = userRepository.getUserLiveData(uid);
    }

    public MediatorLiveData<Long> getPointsLiveData() {
        MediatorLiveData<Long> pointsLiveData = new MediatorLiveData<>();
        pointsLiveData.addSource(userLiveData, user ->
                pointsLiveData.setValue(getRemainedPoints(user)));
        return pointsLiveData;
    }

    private Long getRemainedPoints(User user) {
        if (user == null) {
            return 0L;
        }
        Long get = getSumCheckedInPoints(user);
        Long spent = getSpendPoints(user);
        return get - spent;
    }

    private Long getSpendPoints(User user) {
        if (user.items == null)
            return 0L;
        Set<String> ownedItems = user.items.keySet();

        HashMap<String, ShopItem> shopItems = itemsLiveData.getValue();
        if (shopItems == null) {
            return 0L;
        }

        Long s = 0L;
        for (String item: ownedItems) {
            if (shopItems.get(item) == null)
                continue;

            s += Objects.requireNonNull(shopItems.get(item)).point;
        }
        return s;
    }

    private Long getSumCheckedInPoints(User user) {
        Long s = 0L;
        Map<String, UserLocation> userLocationMap =
                user.captured == null ? new HashMap<>(): user.captured;
        for (Map.Entry<String, UserLocation> entry: userLocationMap.entrySet()) {
            s += entry.getValue().score;
        }
        return s;
    }

    public void buyItem(String itemId) {
        assert itemsLiveData.getValue() != null;
        if (itemsLiveData.getValue().containsKey(itemId)) {
            Long itemPt = Objects.requireNonNull(itemsLiveData.getValue().get(itemId)).point;
            Long userPt = getRemainedPoints(userLiveData.getValue());
            if (itemPt <= userPt) {
                userRepository.buyItem(uid, itemId);
                buyState.setValue(BuyState.OK);
            } else {
                buyState.setValue(BuyState.ERROR);
            }
        }
    }

    public MediatorLiveData<HashMap<String, ShopItemExt>> getItemsLiveData() {
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

                    HashMap<String, ShopItemExt> itemsExt = new HashMap<>();
                    for (Map.Entry<String, ShopItem> entry: items.entrySet()) {
                        boolean hasOwned = userItems.containsKey(entry.getKey());
                        itemsExt.put(entry.getKey(), new ShopItemExt(entry.getValue(), hasOwned));
                    }
                    return itemsExt;
                });
    }

    public MediatorLiveData<BuyState> getBuyResult() {
        return buyState;
    }
}
