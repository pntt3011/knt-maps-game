package com.example.maplogin.ui.history;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.maplogin.models.User;
import com.example.maplogin.models.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class HistoryViewModel extends ViewModel {
    private static final long millisecPerDay = 86400000;

    private UserRepository repository;

    private MediatorLiveData<Integer> currentMonth;
    private MediatorLiveData<Integer> currentYear;
    private MediatorLiveData<String> monthYear;
    private MediatorLiveData<Long> currentTime;

    private MediatorLiveData<Integer> totalCheckedIn;
    private MediatorLiveData<Integer> totalBadges;
    private MediatorLiveData<Integer> totalPoints;
    private MediatorLiveData<ArrayList<Integer>> checkedInHistory;

    public HistoryViewModel(){
        repository = new UserRepository();
        currentTime = new MediatorLiveData<>();
        currentTime.setValue(System.currentTimeMillis());

        checkedInHistory = new MediatorLiveData<ArrayList<Integer>>();

        totalCheckedIn   = new MediatorLiveData<Integer>();
        totalBadges  = new MediatorLiveData<Integer>();
        currentMonth = new MediatorLiveData<Integer>();
        totalCheckedIn.setValue((0));
        totalBadges .setValue((0));

        currentYear  = new MediatorLiveData<Integer>();
        monthYear    = new MediatorLiveData<String>();

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        long current = getCurrentTime().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(current);
        String formattedDate = formatter.format(calendar.getTime());
        currentMonth.setValue(Integer.parseInt(formattedDate.substring(3,5)));
        currentYear.setValue(Integer.parseInt(formattedDate.substring(6,10)));
        monthYear.setValue(Integer.toString(currentYear.getValue()));
    }

    public MediatorLiveData<ArrayList<Integer>> getCheckedInHistory() {
        return checkedInHistory;
    }
    public MediatorLiveData<Integer> getCurrentMonth() {
        return currentMonth;
    }
    public MediatorLiveData<Integer> getCurrentYear() {
        return currentYear;
    }
    public MediatorLiveData<Long> getCurrentTime() {
        return currentTime;
    }
    public MediatorLiveData<String> getMonthYear() { return monthYear; }
    public MediatorLiveData<Integer> getTotalCheckedIn() {
        return totalCheckedIn;
    }
    public MediatorLiveData<Integer> getTotalBadges() {
        return totalBadges;
    }
    public MediatorLiveData<Integer> getTotalPoints() {
        return totalPoints;
    }

    void countTotalCheckedIn(){
//        totalCheckedIn.addSource(repository.getCheckedInLiveData(), new Observer<HashMap<String, Long>>() {
//            @Override
//            public void onChanged(HashMap<String, Long> stringLongHashMap) {
//                totalCheckedIn.postValue(stringLongHashMap.size());
//            }
//        });
    }

    void initHistory(){
//        checkedInHistory.removeSource(repository.getCheckedInLiveData());
//        checkedInHistory.addSource(repository.getCheckedInLiveData(), new Observer<HashMap<String, Long>>() {
//            @Override
//            public void onChanged(HashMap<String, Long> stringLongHashMap) {
//                ArrayList<Integer> historyData = new ArrayList<Integer>();
//
//                for (int i = 0; i < 12; i++){
//                    int count = 0;
//                    for (Long t : stringLongHashMap.values()){
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTimeInMillis(t);
//
//                        int mYear = calendar.get(Calendar.YEAR);
//                        int mMonth = calendar.get(Calendar.MONTH);
//                        if(mMonth == i + 1 && mYear == currentYear.getValue()){
//                            count++;
//                        }
//                    }
//                    historyData.add(count);
//                }
//                checkedInHistory.setValue(historyData);
//            }
//        });
    }

    void changeForward(){

        if (currentMonth.getValue() == 12){
            currentMonth.postValue(1);
            currentYear.postValue(currentYear.getValue()+1);
        }
        else
            currentMonth.postValue(currentMonth.getValue()+1);
        monthYear.postValue(String.valueOf(getCurrentYear().getValue()));
    }

    void changeBack(){

        if (currentMonth.getValue() ==1){
            currentMonth.postValue(12);
            currentYear.postValue(currentYear.getValue()-1);
        }
        else
            currentMonth.postValue(currentMonth.getValue()-1);
        monthYear.postValue(String.valueOf(getCurrentYear().getValue()));
        //initStatistic();

    }
}
