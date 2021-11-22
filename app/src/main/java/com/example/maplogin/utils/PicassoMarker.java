package com.example.maplogin.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

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

    public PicassoMarker(Marker marker,
                         HashMap<String, Bitmap> markerIconMap,
                         HashSet<PicassoMarker> set) {
        mMarker = marker;
        mPicassoMarkerSet = set;
        mMarkerIconMap = markerIconMap;
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
        assert t != null;
        mMarkerIconMap.put(t.id, bitmap);
        cleanUpResource();
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        cleanUpResource();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        mPicassoMarkerSet.add(this);
    }

    private void cleanUpResource() {
        mMarker.setVisible(true);
        mPicassoMarkerSet.remove(this);
    }
}

