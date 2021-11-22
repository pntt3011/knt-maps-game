package com.example.maplogin.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.maplogin.R;
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
    private final Activity mActivity;

    public PicassoMarker(Activity activity,
                         Marker marker,
                         HashMap<String, Marker> markerMap,
                         HashMap<String, Bitmap> markerIconMap,
                         HashSet<PicassoMarker> set) {
        mActivity = activity;
        mMarker = marker;
        Log.e("PicassoMarker", marker.getTag().toString());
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
        Log.e("onBitmapLoaded", mMarker.getTag().toString());
        MarkerController.Tag t = (MarkerController.Tag) mMarker.getTag();
        mMarkerIconMap.put(t.id, bitmap);
        mMarkerMap.put(t.id, mMarker);
        cleanUpResource();
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        Bitmap bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.ic_address);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        MarkerController.Tag t = (MarkerController.Tag) mMarker.getTag();
        mMarkerIconMap.put(t.id, bmp);
        mMarkerMap.put(t.id, mMarker);
        cleanUpResource();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) { }

    private void cleanUpResource() {
        mMarker.setVisible(true);
        mPicassoMarkerSet.remove(this);
    }
}

