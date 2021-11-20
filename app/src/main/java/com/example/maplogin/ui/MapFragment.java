package com.example.maplogin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentMapBinding;
import com.example.maplogin.utils.DatabaseAdapter;
import com.example.maplogin.utils.MarkerController;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, RoutingListener {
    // Fragment information
    private FragmentMapBinding binding;
    private Activity mActivity;

    // Database information
    private DatabaseAdapter mDatabase;

    // Map information
    public static final int DEFAULT_UPDATE_INTERVAL = 1000;
    public static final int FASTEST_UPDATE_INTERVAL = 500;
    public static final int POLYLINE_WIDTH = 7;

    private GoogleMap mMap;

    private ArrayList<Polyline> polylines;
    private ArrayList<Marker> markers;

    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private LocationRequest locationRequest = null;
    private LocationCallback locationCallback = null;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

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
                LatLng latlng = new LatLng(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
            }
        };
    }

    private void startSyncMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
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

        setupBottomSheet();
        setupMarkerController();
        mDatabase.startSync();

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
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (locationPermissionGranted) {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(mActivity, location -> {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = location;
                LatLng latlng = new LatLng(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
                LatLng end = new LatLng(10.7546664,106.415032);
                findRoutes(latlng, end, AbstractRouting.TravelMode.DRIVING);
            });
        }
    }

    // Prompts the user for permission to use the device location.
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
            getDeviceLocation();
        }
    }

    // find routes from startLatLng to endLatLng with specific travelMode.
    public void findRoutes(LatLng startLatLng, LatLng endLatLng, AbstractRouting.TravelMode travelMode)
    {
        if(startLatLng==null || endLatLng==null)
            Toast.makeText(mActivity,"Unable to get location",Toast.LENGTH_LONG).show();
        else {
            Routing routing = new Routing.Builder()
                    .travelMode(travelMode)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(startLatLng, endLatLng)
                    .alternativeRoutes(true)
                    .key("AIzaSyCQjSbW4ANku5u4VMlkIWtpp4m6yTi4EPA")
                    .build();
            routing.execute();
        }
    }


    /***** start of routing call back functions *****/
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        clearRoutesUI();

        polylines = new ArrayList<>();

        // add route(s) to the map using polyline
        int color;
        for (int i = 0; i < route.size(); i++) {
            if (i == shortestRouteIndex)
                color = ContextCompat.getColor(mActivity, R.color.purple_500);
            else
                color = ContextCompat.getColor(mActivity, R.color.purple_transparent);
            addRouteToMap(route.get(i).getPoints(), color);
        }
    }

    private void addRouteToMap(List<LatLng> points, int color) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(color);
        polyOptions.width(POLYLINE_WIDTH);
        polyOptions.addAll(points);
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);
    }

    private void clearRoutesUI() {
        if (polylines != null) {
            for (int i = 0; i < polylines.size(); ++i)
                polylines.get(i).remove();
            polylines.clear();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
    }
    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingCancelled() {
    }
    /***** end of routing call back functions *****/

    private void setupBottomSheet() {
        View view = getView();
        if (view == null)
            return;

        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        BottomSheetBehaviorGoogleMapsLike<View> behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);

        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
    }

    private void setupMarkerController() {
        MarkerController markerController = new MarkerController(mMap);
        mDatabase.setModifyLocationListener(
                markerController.getLocationListener());
        mDatabase.setModifyCaptureListener(
                markerController.getCaptureListener());
    }
}