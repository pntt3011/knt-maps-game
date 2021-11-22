package com.example.maplogin.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.maplogin.R;
import com.example.maplogin.struct.LocationInfo;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.HashSet;

public class MarkerController {
    // Convert color
    private static final float NULL_OPACITY = 1.0f;
    private static final float CAPTURE_OPACITY = 1.0f;

    // Store all markers for future use
    private final HashMap<String, Marker> mMarkerMap;
    private final HashMap<String, Bitmap> mMarkerIconMap;

    // PicassoMarkers need to be alive until bitmap icon is loaded
    private final HashSet<PicassoMarker> mPicassoMarkerSet;

    // Save a map reference to draw to it on callback
    private final GoogleMap mMap;

    private final Activity mActivity;

    // Marker tag
    public static class Tag {
        public String id;
        public String url;

        public Tag(String id, String url) {
            this.id = id;
            this.url = url;
        }
        public String toString() {
            return "ID: " + id + " URL: " + url;
        }
    }

    public HashMap<String, Marker> getMarkerMap() {
        return mMarkerMap;
    }

    public HashMap<String, Bitmap> getMarkerIconMap() {
        return mMarkerIconMap;
    }

    public MarkerController(Activity activity, GoogleMap map) {
        mMarkerMap = new HashMap<>();
        mPicassoMarkerSet = new HashSet<>();
        mMap = map;
        mMarkerIconMap = new HashMap<>();
        mActivity = activity;
    }

    public DatabaseAdapter.OnModifyLocationListener getLocationListener() {
        return new DatabaseAdapter.OnModifyLocationListener() {
            @Override
            public void add(String id, LocationInfo locationInfo) {
                addMarker(id, locationInfo.latitude,
                        locationInfo.longitude,
                        locationInfo.iconUrl,
                        NULL_OPACITY);
            }

            @Override
            public void change(String id, LocationInfo locationInfo) {
                addMarker(id, locationInfo.latitude,
                        locationInfo.longitude,
                        locationInfo.iconUrl,
                        NULL_OPACITY);
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
            public void add(String id) { }

            @Override
            public void remove(String id) { }
        };
    }

    public Marker findMarkerById(String id) {
        return mMarkerMap.getOrDefault(id, null);
    }

    public void updateAllIcons(HashMap<String, Boolean> isNearMap) {
        for (HashMap.Entry<String, Marker> entry : mMarkerMap.entrySet()) {
            String key = entry.getKey();
            Marker marker = entry.getValue();

            updateMarkerIcon(marker, mMarkerIconMap.get(key), isNearMap.get(key));
        }
    }

    private void updateMarkerIcon(Marker marker, Bitmap bitmap, Boolean isNear) {
        Bitmap bitmap_temp = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(bitmap_temp);
        if (isNear) {
            // get exclamation drawable and draw to bitmap
            Drawable exclamationDrawable = ContextCompat.getDrawable(mActivity, R.drawable.ic_exclamation);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            exclamationDrawable.setBounds(width / 3, height / 4, width, height * 7 / 8);
            exclamationDrawable.draw(canvas);
        }
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap_temp));
    }

    private void addMarker(String id, Double lat, Double lng, String iconUrl, float opacity) {
        LatLng latLng = new LatLng(lat, lng);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .alpha(opacity)
                // this is the default icon so we will hide it here
                .visible(false));

        if (marker != null) {
            marker.setTag(new Tag(id, iconUrl));
            Log.e("hehe", marker.getTag().toString());
            PicassoMarker picassoMarker = new PicassoMarker(
                    marker,
                    mMarkerMap,
                    mMarkerIconMap,
                    mPicassoMarkerSet);
            mPicassoMarkerSet.add(picassoMarker);

            // Load icon async and show it
            Picasso.get().load(iconUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .resize(64,64)
                    .into(picassoMarker);
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
