package com.example.maplogin.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashSet;

public class PicassoMarker implements Target {
    private final Marker mMarker;
    private final HashSet<PicassoMarker> mPicassoMarkerSet;

    public PicassoMarker(Marker marker, HashSet<PicassoMarker> set) {
        mMarker = marker;
        mPicassoMarkerSet = set;
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

