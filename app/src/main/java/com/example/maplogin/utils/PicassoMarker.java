package com.example.maplogin.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.HashSet;

public class PicassoMarker implements Target {
    private final Marker mMarker;
    private final HashSet<PicassoMarker> mPicassoMarkerSet;
    private final HashMap<String, Bitmap> mMarkerIconMap;
    private final HashMap<String, Marker> mMarkerMap;

    public PicassoMarker(Marker marker,
                         HashMap<String, Marker> markerMap,
                         HashMap<String, Bitmap> markerIconMap,
                         HashSet<PicassoMarker> set) {
        mMarker = marker;
        mPicassoMarkerSet = set;
        mMarkerIconMap = markerIconMap;
        mMarkerMap = markerMap;
    }

    @Override
    public int hashCode() {
        return mMarker.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof PicassoMarker) {
            Marker marker = ((PicassoMarker) o).mMarker;
            return mMarker.equals(marker);
        } else {
            return false;
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        MarkerController.Tag t = (MarkerController.Tag) mMarker.getTag();
        mMarkerIconMap.put(t.id, bitmap);
        mMarkerMap.put(t.id, mMarker);
        cleanUpResource();
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        cleanUpResource();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) { }

    private void cleanUpResource() {
        mMarker.setVisible(true);
        mPicassoMarkerSet.remove(this);
    }
}

