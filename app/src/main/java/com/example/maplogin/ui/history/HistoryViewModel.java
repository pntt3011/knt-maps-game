package com.example.maplogin.ui.history;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.example.maplogin.models.User;
import com.example.maplogin.models.UserLocation;
import com.example.maplogin.models.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HistoryViewModel extends ViewModel {
    private final MediatorLiveData<Long> currentYear;
    private final MediatorLiveData<Long> currentTime;

    private final MediatorLiveData<Long> totalCheckedIn;
    private final MediatorLiveData<Long> totalBadges;
    private final MediatorLiveData<Long> totalPoints;
    private final MediatorLiveData<ArrayList<Long>> checkedInHistory;

    private final MediatorLiveData<User> userLiveData;

    public HistoryViewModel(){
        UserRepository repository = new UserRepository();
        String uid = FirebaseAuth.getInstance().getUid();

        currentTime = new MediatorLiveData<>();
        currentTime.setValue(System.currentTimeMillis());

        checkedInHistory = new MediatorLiveData<>();

        totalCheckedIn   = new MediatorLiveData<>();
        totalBadges  = new MediatorLiveData<>();
        totalPoints = new MediatorLiveData<>();
        totalCheckedIn.setValue((0L));
        totalBadges .setValue((0L));

        currentYear  = new MediatorLiveData<>();
        userLiveData = repository.getUserLiveData(uid);

        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        long current = getCurrentTime().getValue() == null ? 0 : getCurrentTime().getValue();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(current);
        String formattedDate = formatter.format(calendar.getTime());
        currentYear.setValue(Long.parseLong(formattedDate.substring(6,10)));
    }

    public MediatorLiveData<ArrayList<Long>> getCheckedInHistory() {
        return checkedInHistory;
    }
    public MediatorLiveData<Long> getCurrentYear() {
        return currentYear;
    }
    public MediatorLiveData<Long> getCurrentTime() {
        return currentTime;
    }
    public MediatorLiveData<Long> getTotalCheckedIn() {
        return totalCheckedIn;
    }
    public MediatorLiveData<Long> getTotalBadges() {
        return totalBadges;
    }
    public MediatorLiveData<Long> getTotalPoints() {
        return totalPoints;
    }

    void countTotalCheckedIn(){
        totalCheckedIn.addSource(userLiveData, user -> {
            if(user == null){
                totalCheckedIn.postValue(0L);
            }
            else {
                Map<String, UserLocation> locationHashMap = user.captured;
                if(locationHashMap == null){
                    locationHashMap = new HashMap<>();
                }
                totalCheckedIn.postValue((long)locationHashMap.size());
            }
        });
    }

    void countTotalPoint(){
        totalPoints.addSource(userLiveData, user -> {
            Long s = 0L;
            Map<String, UserLocation> userLocationMap =
                    user.captured == null ? new HashMap<>(): user.captured;
            for (Map.Entry<String, UserLocation> entry: userLocationMap.entrySet()) {
                s += entry.getValue().score;
            }

            totalPoints.postValue(s);
        });
    }

    void countTotalBadges(){
        totalBadges.addSource(userLiveData, user -> {
            totalBadges.postValue(0L);
        });
    }

    void initHistory(){
        checkedInHistory.removeSource(userLiveData);
        checkedInHistory.addSource(userLiveData, user -> {
            ArrayList<Long> historyData = new ArrayList<>();
            if (user != null) {
                Map<String, UserLocation> locationHashMap = user.captured;
                if(locationHashMap == null){
                    locationHashMap = new HashMap<>();
                }
                for (int i = 0; i < 12; i++) {
                    Long count = 0L;
                    for (Map.Entry<String, UserLocation> entry : locationHashMap.entrySet()) {
                        Long t = entry.getValue().time;
                        
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(t);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mMonth = calendar.get(Calendar.MONTH);
                        if(currentYear != null && currentYear.getValue() != null){
                            if (mMonth == i && mYear == currentYear.getValue()) {
                                count++;
                            }
                        }
                    }
                    historyData.add(count);
                }
            }
            checkedInHistory.setValue(historyData);
        });
    }

    void changeForward(){
        if (currentYear != null && currentYear.getValue() != null) {
            currentYear.setValue(currentYear.getValue() + 1);
        }
    }

    void changeBack(){
        if (currentYear != null && currentYear.getValue() != null) {
            currentYear.setValue(currentYear.getValue() - 1);
        }
    }
}
