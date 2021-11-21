package com.example.maplogin.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.maplogin.FirebaseLogin;
import com.example.maplogin.struct.Info;
import com.example.maplogin.struct.InfoType;
import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.struct.LocationMarker;
import com.example.maplogin.struct.QuestionInfo;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAdapter {
    private static DatabaseAdapter instance = null;

    private static final String DATABASE_URI = "https://api-3011-ad499.asia-southeast1.firebasedatabase.app/";
    private static final String USER_ROOT = "users";
    private static final String LOCATION_MARKER_ROOT = "markers";
    private static final String QUESTION_INFO_ROOT = "questions";
    private static final String LOCATION_INFO_ROOT = "infos";

    // Database info
    private FirebaseDatabase mDatabase;
    private String mUid;

    // Data syncs
    private HashMap<String, LocationMarker> mAllLocations;
    private HashMap<String, Long> mFailedLocations;
    private HashMap<String, Long> mCapturedLocations;

    // Listeners
    private ArrayList<OnModifyCaptureListener> mCaptureListeners;
    private ArrayList<OnModifyLocationListener> mLocationListeners;

    // --------------------------Interface-------------------------------------
    public interface OnModifyLocationListener {
        void add(String id, LocationMarker marker);
        void change(String id, LocationMarker marker);
        void remove(String id);
    }

    public interface OnModifyCaptureListener {
        void add(String id);
        void remove(String id);
    }

    public interface OnGetInfoListener {
        void process(Info info);
    }

    // -----------------------Public methods-----------------------------------
    // Get the singleton instance
    public static DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
            instance.updateInfo();
        }
        return instance;
    }

    // Use when change account
    public static void updateUserInfo() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        instance.updateInfo();
    }

    // Should be used before startSync
    public void setModifyCaptureListener(OnModifyCaptureListener listener) {
        mCaptureListeners.add(listener);
    }

    // Should be used before startSync
    public void setModifyLocationListener(OnModifyLocationListener listener) {
        mLocationListeners.add(listener);
    }

    // Run all set  listener
    public void startSync() {
        setupLocationMarkersListener();
        setupCaptureListener();
        setupFailListener();
    }

    public Map<String, LocationMarker> getAllLocations() {
        return new HashMap<>(mAllLocations);
    }

    public Map<String, Long> getFailedLocations() {return new HashMap<>(mFailedLocations);}

    public Map<String, Long> getCapturedLocations() {
        return new HashMap<>(mCapturedLocations);
    }

    public void addCapturedLocation(String id) {
        DatabaseReference capturedMarkersRef = getCapturedMarkerReference();
        capturedMarkersRef.child(id).setValue(true);
    }

    // Get location or question info
    public void queryInfo(String ID, InfoType type, OnGetInfoListener callback) {
        String root = getRoot(type);
        DatabaseReference ref = getPathReference(root, new String[]{ID});
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Info info = parseInfo(dataSnapshot, type);
                callback.process(info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.e("Query Info", "Fail to load query info.");
            }
        };
        ref.addListenerForSingleValueEvent(listener);
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public boolean isAnonymousUser() {
        FirebaseUser user = getCurrentUser();
        if (user == null)
            return false;
        return user.isAnonymous();
    }

    public void logoutCurrentUser(Context context) {
        // Remove server data if anonymous
        if (isAnonymousUser()) {
            deleteAnonymousUser(context);
        } else {
            signOutNormalUser(context);
        }
    }

    public void startLoginActivity(boolean isUpgrade, Context context) {
        Intent i = new Intent(context, FirebaseLogin.class);
        i.putExtra("UPGRADE", isUpgrade);
        context.startActivity(i);
    }

    // -----------------------Private methods---------------------------------
    private DatabaseAdapter() { }

    private void updateInfo() {
        mUid = getCurrentUserId();
        mDatabase = FirebaseDatabase.getInstance(DATABASE_URI);

        mAllLocations = new HashMap<>();
        mFailedLocations = new HashMap<>();
        mCapturedLocations = new HashMap<>();

        mCaptureListeners = new ArrayList<>();
        mLocationListeners = new ArrayList<>();
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            return user.getUid();
        return "UNKNOWN_USER";
    }

    private void setupLocationMarkersListener() {
        DatabaseReference locationMarkersRef = getPathReference(LOCATION_MARKER_ROOT, new String[]{});

        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                LocationMarker m = snapshot.getValue(LocationMarker.class);
                String key = snapshot.getKey();
                mAllLocations.put(key, m);
                for (OnModifyLocationListener listener: mLocationListeners) {
                    listener.add(key, m);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                LocationMarker m = snapshot.getValue(LocationMarker.class);
                String key = snapshot.getKey();
                mAllLocations.put(key, m);
                for (OnModifyLocationListener listener: mLocationListeners) {
                    listener.change(key, m);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                mAllLocations.remove(key);
                for (OnModifyLocationListener listener: mLocationListeners) {
                    listener.remove(key);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Location Markers", "Failed to load location markers.");
            }
        };

        locationMarkersRef.addChildEventListener(childListener);
    }

    private void setupCaptureListener() {
        DatabaseReference capturedMarkersRef = getCapturedMarkerReference();

        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Long num = snapshot.getValue(Long.class);
                String key = snapshot.getKey();
                mCapturedLocations.put(key, num);
                for (OnModifyCaptureListener listener: mCaptureListeners) {
                    listener.add(key);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Long num = snapshot.getValue(Long.class);
                String key = snapshot.getKey();
                mCapturedLocations.put(key, num);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                mCapturedLocations.remove(key);
                for (OnModifyCaptureListener listener: mCaptureListeners) {
                    listener.remove(key);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Captured Locations", "Failed to load captured locations.");
            }
        };

        capturedMarkersRef.addChildEventListener(childListener);
    }

    private void setupFailListener() {
        DatabaseReference failedMarkersRef = getFailedMarkerReference();

        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Long num = snapshot.getValue(Long.class);
                String key = snapshot.getKey();
                mFailedLocations.put(key, num);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Long num = snapshot.getValue(Long.class);
                String key = snapshot.getKey();
                mFailedLocations.put(key, num);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                mFailedLocations.remove(key);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Failed Locations", "Failed to load failed locations.");
            }
        };

        failedMarkersRef.addChildEventListener(childListener);
    }

    private DatabaseReference getPathReference(String root, String[] path) {
        StringBuilder pathString = new StringBuilder(root);
        for (String s: path) {
            pathString.append("/").append(s);
        }
        return mDatabase.getReference(pathString.toString());
    }

    private DatabaseReference getCapturedMarkerReference() {
        return getPathReference(USER_ROOT, new String[]{mUid, "captured"});
    }

    private DatabaseReference getFailedMarkerReference() {
        return getPathReference(USER_ROOT, new String[]{mUid, "failed"});
    }

    private void signOutNormalUser(Context context) {
        AuthUI.getInstance()
                .signOut(context)

                // after sign out is executed we are redirecting
                // our user to MainActivity where our login flow is being displayed.
                .addOnCompleteListener(v-> logoutCallback(context));
    }

    private String getRoot(InfoType type) {
        if (type == InfoType.LOCATION)
            return LOCATION_INFO_ROOT;
        else
            return QUESTION_INFO_ROOT;
    }

    private Info parseInfo(DataSnapshot dataSnapshot, InfoType type) {
        if (type == InfoType.QUESTION)
            return dataSnapshot.getValue(QuestionInfo.class);
        else
            return dataSnapshot.getValue(LocationInfo.class);
    }

    private void deleteAnonymousUser(Context context) {
        mDatabase.getReference(USER_ROOT)
                .child(mUid)
                .removeValue();

        AuthUI.getInstance()
                .delete(context)
                .addOnCompleteListener(v-> logoutCallback(context));
    }

    private void logoutCallback(Context context) {
        // below method is used after logout from device.
        Toast.makeText(context, "User Signed Out", Toast.LENGTH_SHORT).show();

        // below line is to go to MainActivity via an intent.
        startLoginActivity(false, context);
    }
}
