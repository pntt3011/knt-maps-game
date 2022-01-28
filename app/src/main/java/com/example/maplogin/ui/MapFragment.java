package com.example.maplogin.ui;

import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.utils.BottomSheetController;
import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentMapBinding;
import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.utils.DatabaseAdapter;
import com.example.maplogin.utils.MarkerController;
import com.example.maplogin.utils.NearestRecyclerAdapter;
import com.example.maplogin.utils.RoutingController;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    public static final int MIN_ZOOM = 13;
    public static final int MAX_ZOOM = 17;
    public static final int DEFAULT_ZOOM = 15;
    public static final double VALID_RANGE = 250;
    public static final int NUM_NEAR_PLACE = 5;

    // Fragment information
    private FragmentMapBinding binding;
    private Activity mActivity;

    // Database information
    private DatabaseAdapter mDatabase;
    private BottomSheetController mBottomSheet;

    // Routing controller
    private RoutingController mRoutingController;

    // Map information
    public static final int DEFAULT_UPDATE_INTERVAL = 1000;
    public static final int FASTEST_UPDATE_INTERVAL = 500;

    private GoogleMap mMap;

    MarkerController mMarkerController;

    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private LocationRequest locationRequest = null;
    private LocationCallback locationCallback = null;

    private boolean locationPermissionGranted;

    private Location lastKnownLocation;
    private View mapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mDatabase = DatabaseAdapter.getInstance();
        getLocationPermission();
        setupLocationAttribute();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mActivity != null) {
            startSyncMap();
        }
    }

    private void setupLocationAttribute() {
        // create new fused location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);

        // setup how frequent and how accuracy to update location
        locationRequest = LocationRequest.create()
                .setInterval(DEFAULT_UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // location call back function everytime location change
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save the location
                lastKnownLocation = locationResult.getLastLocation();

                // get list of marker
                HashMap<String, Marker> markerHashMap = mMarkerController.getMarkerMap();
                HashMap<String, Boolean> isNearMap = new HashMap<>();
                for (HashMap.Entry<String, Marker> entry : markerHashMap.entrySet()) {
                    String key = entry.getKey();
                    Marker marker = entry.getValue();
                    isNearMap.put(key, isNearUser(marker.getPosition()));
                }

                mMarkerController.updateAllIcons(isNearMap);
            }
        };
    }

    private void startSyncMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapView = mapFragment.getView();
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // mMap.setMinZoomPreference(MIN_ZOOM);
        // mMap.setMaxZoomPreference(MAX_ZOOM);

        updateLocationUI();
        addStarButtonToMapView();

        setupBottomSheet();
        setupMarkerController();
        setupMarkerListener();

        startLocationUpdates();

        mRoutingController = new RoutingController(mActivity, mMap);
        mDatabase.startSync();
    }

    private void addStarButtonToMapView() {
        View location_button = mapView.findViewWithTag("GoogleMapMyLocationButton");
        RelativeLayout mapRelativeLayout = (RelativeLayout) location_button.getParent();

        Button button = createStartButton(location_button);
        mapRelativeLayout.addView(button);
    }

    private Button createStartButton(View location_button) {
        Button button = new Button(mActivity);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(int2dp(40), int2dp(40));
        params.addRule(RelativeLayout.BELOW, location_button.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
        params.setMargins(0, int2dp(10), int2dp(10), 0);
        button.setLayoutParams(params);
        Drawable star_icon = ContextCompat.getDrawable(mActivity, R.drawable.ic_star_icon);
        button.setBackground(star_icon);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating an ArrayList of location info
                HashMap<String, LocationInfo> locationInfoHashMap =
                        (HashMap<String, LocationInfo>) mDatabase.getAllLocations();

                Set<Map.Entry<String, LocationInfo>> entrySet = locationInfoHashMap.entrySet();

                // get top NUM_NEAR_PLACE nearest location from list of entries
                ArrayList<Map.Entry<String, LocationInfo>> locationEntries = new ArrayList<>(entrySet);

                locationEntries.sort((a, b) -> {
                    LocationInfo location1 = a.getValue();
                    LatLng latLng1 = new LatLng(location1.latitude, location1.longitude);
                    double dist1 = getDistanceToUser(latLng1);
                    LocationInfo location2 = b.getValue();
                    LatLng latLng2 = new LatLng(location2.latitude, location2.longitude);
                    double dist2 = getDistanceToUser(latLng2);
                    return (dist1 > dist2) ? 1 : (dist1 < dist2) ? -1 : 0;
                });

                int nTop = min(locationEntries.size(),NUM_NEAR_PLACE);
                locationEntries = new ArrayList<>(locationEntries.subList(0, nTop));

                // create and show the popup window
                PopupWindow popupWindow = createLocationListPopup(locationEntries);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });
        return button;
    }

    private int int2dp(int val) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (val * scale + 0.5f);
    }

    private PopupWindow createLocationListPopup(ArrayList<Map.Entry<String, LocationInfo>> locationEntries) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout popupView = (LinearLayout) inflater.inflate(R.layout.popup_location_list_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view_near_place);

        NearestRecyclerAdapter adapter = new NearestRecyclerAdapter(mActivity, locationEntries,
                locationKey -> {
                    HashMap<String, Marker> markerHashMap = mMarkerController.getMarkerMap();
                    popupWindow.dismiss();
                    Marker marker = markerHashMap.get(locationKey);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), MIN_ZOOM));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));
                    mBottomSheet.update(marker);
                });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        return popupWindow;
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void updateUserLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (locationPermissionGranted) {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(mActivity, location -> {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = location;
                LatLng latlng = getUserLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));

                if (lastKnownLocation == null) {
                    Toast.makeText(mActivity, "Cannot retrieve current location. Please try again.", Toast.LENGTH_SHORT).show();
                    mActivity.finish();
                }
            });
        }
    }

    private LatLng getUserLatLng() {
        return new LatLng(lastKnownLocation.getLatitude(),
                lastKnownLocation.getLongitude());
        // Prompts the user for permission to use the device location.
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(mActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        else {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        locationPermissionGranted = isGranted;
                        updateLocationUI();
                    });
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // Updates the map's UI settings based on whether the user has granted location permission.
    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (locationPermissionGranted && mMap != null) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.map_style));
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            updateUserLocation();
        }
    }

    public boolean isNearUser(LatLng destLatLng) {
        return getDistanceToUser(destLatLng) <= VALID_RANGE;
    }

    private double getDistanceToUser(LatLng destLatLng) {
        Location dest = new Location(LocationManager.GPS_PROVIDER);
        dest.setLatitude(destLatLng.latitude);
        dest.setLongitude(destLatLng.longitude);
        return lastKnownLocation.distanceTo(dest);
    }

    private void setupBottomSheet() {
        mBottomSheet = new BottomSheetController(mActivity,
                (dst, mode) -> mRoutingController.findRoutes(getUserLatLng(), dst, mode),
                this::isNearUser);
    }

    private void setupMarkerController() {
        mMarkerController = new MarkerController(mActivity, mMap);
        mDatabase.setModifyLocationListener(
                mMarkerController.getLocationListener());
        mDatabase.setModifyCaptureListener(
                mMarkerController.getCaptureListener());
    }

    private void setupMarkerListener() {
        mMap.setOnMarkerClickListener(mBottomSheet::update);
    }

}