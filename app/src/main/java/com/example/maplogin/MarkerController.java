package com.example.maplogin;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MarkerController {
    // Store all markers for future use
    private final HashMap<String, Marker> mMarkerMap;
    private final GoogleMap mMap;

    // Convert color
    private static final float NULL_OPACITY = 0.5f;
    private static final float CAPTURE_OPACITY = 1.0f;

    // Marker tag
    public static class Tag {
        public String id;
        public String url;

        public Tag(String id, String url) {
            this.id = id;
            this.url = url;
        }
    }

    public MarkerController(GoogleMap map) {
        mMarkerMap = new HashMap<>();
        mMap = map;
    }

    public DatabaseAdapter.OnModifyLocationListener getLocationListener() {
        return new DatabaseAdapter.OnModifyLocationListener() {
            @Override
            public void add(String id, LocationMarker locationMarker) {
                if (mMarkerMap.containsKey(id)) {
                    addMarker(id, locationMarker.latitude,
                            locationMarker.longitude,
                            locationMarker.iconUrl,
                            CAPTURE_OPACITY);
                } else {
                    addMarker(id, locationMarker.latitude,
                            locationMarker.longitude,
                            locationMarker.iconUrl,
                            NULL_OPACITY);
                }
            }

            @Override
            public void change(String id, LocationMarker marker) {
            }

            @Override
            public void remove(String id) {
                Marker marker = findMarkerById(id);
                if (marker != null) {
                    marker.remove();
                }
            }
        };
    }

    public DatabaseAdapter.OnModifyCaptureListener getCaptureListener() {
        return new DatabaseAdapter.OnModifyCaptureListener() {
            @Override
            public void add(String id) {
                if (mMarkerMap.containsKey(id)) {
                    Marker marker = findMarkerById(id);
                    switchMarkerType(id, marker, CAPTURE_OPACITY);
                } else {
                    mMarkerMap.put(id, null);
                }
            }

            @Override
            public void remove(String id) {
                Marker marker = findMarkerById(id);
                if (marker != null) {
                    switchMarkerType(id, marker, NULL_OPACITY);
                }
            }
        };
    }

    private Marker findMarkerById(String id) {
        return mMarkerMap.getOrDefault(id, null);
    }

    private void addMarker(String id, Double lat, Double lng, String iconUrl, float opacity) {
        LatLng latLng = new LatLng(lat, lng);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .alpha(opacity));

        if (marker != null) {
            marker.setTag(new Tag(id, iconUrl));
            mMarkerMap.put(id, marker);
        }
    }

    private void switchMarkerType(String id, Marker marker, float dstOpacity) {
        Tag t = (Tag) marker.getTag();
        addMarker(id, marker.getPosition().latitude,
                marker.getPosition().longitude,
                t != null ? t.url : null,
                dstOpacity);
        marker.remove();
    }
}
