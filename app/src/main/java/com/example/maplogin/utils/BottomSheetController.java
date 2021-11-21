package com.example.maplogin.utils;

import static com.example.maplogin.ui.MapFragment.VALID_RANGE;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.example.maplogin.R;
import com.example.maplogin.struct.InfoType;
import com.example.maplogin.struct.LocationInfo;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Map;

public class BottomSheetController {
    private final Activity mActivity;
    private final View mBottomSheet;
    private final BottomSheetBehaviorGoogleMapsLike<View> mBehavior;
    private final DatabaseAdapter mDatabase;
    private final OnFindDirectionListener mFindDirectionListener;
    private final OnCheckInListener mCheckInListener;

    public interface OnCheckInListener {
        boolean canCheckIn(LatLng dst);
    }

    public interface OnFindDirectionListener {
        void find(LatLng dst, AbstractRouting.TravelMode mode);
    }

    public BottomSheetController(Activity activity,
                                 OnFindDirectionListener directionlistener,
                                 OnCheckInListener checkinListener) {
        mActivity = activity;
        mBottomSheet = activity.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehaviorGoogleMapsLike.from(mBottomSheet);
        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        mDatabase = DatabaseAdapter.getInstance();
        mFindDirectionListener = directionlistener;
        mCheckInListener = checkinListener;
    }

    public boolean update(Marker marker) {
        MarkerController.Tag t = (MarkerController.Tag) marker.getTag();
        assert t != null;
        mDatabase.queryInfo(t.id, InfoType.LOCATION, info -> {
            if (info != null) {
                LocationInfo locInfo = (LocationInfo) info;

                setupDirectionButton(marker.getPosition());
                setupCheckInButton(marker.getPosition(),locInfo.questions);
                setupShareButton(t.id);

                updateInfo(t, locInfo);
                mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);

            } else {
                Toast.makeText(mActivity, t.id + " doesn't have any info."
                        ,Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    private void setupDirectionButton(LatLng position) {
        ImageButton directionButton = mActivity.findViewById(R.id.bs_direct_button);
        directionButton.setOnClickListener(v ->
                mFindDirectionListener.find(position, AbstractRouting.TravelMode.DRIVING));
    }

    private void setupCheckInButton(LatLng position, ArrayList<String> questionId) {
        ImageButton checkInButton = mActivity.findViewById(R.id.bs_check_in_button);
        checkInButton.setOnClickListener(v -> {
            if (mCheckInListener.canCheckIn(position)) {
                // TODO
            } else {
                Toast.makeText(
                    mActivity,
                    "You must be within a radius of " +
                        (int) (VALID_RANGE * 1000) +
                        "m from the location.",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupShareButton(String id) {
        ImageButton shareButton = mActivity.findViewById(R.id.bs_share_button);
        shareButton.setOnClickListener(v -> {
            if (isCaptured(id)) {
                ShareHelper.shareScreenshot(mActivity);
            } else {
                Toast.makeText(mActivity, "You must pass the check-in quiz first." ,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isCaptured(String id) {
        Map<String, Long> captured = mDatabase.getCapturedLocations();
        return captured.containsKey(id);
    }

    private void updateInfo(MarkerController.Tag tag, LocationInfo info) {
        changeBottomSheetIcon(mBottomSheet, tag.url);
        changeLocationName(info.name);
        changeLocationPoint(tag.id);
        changeLocationAddress(info.address);
        changeLocationPhone(info.phone);
        changeLocationMail(info.email);
        changeLocationImages(info.imageUrls);
        changeLocationDescription(info.description);
    }

    private void changeBottomSheetIcon(View bottomSheet, String url) {
        ImageView icon = bottomSheet.findViewById(R.id.iconMarker);
        if (url.equals("empty")) {
            icon.setImageResource(R.drawable.background);
        } else {
            Picasso.get().load(url).fit().into(icon);
        }
    }

    private void changeLocationName(String name) {
        TextView nameView = mBottomSheet.findViewById(R.id.bottom_sheet_title);
        nameView.setText(name);
    }

    private void changeLocationPoint(String id) {
        TextView pointView = mBottomSheet.findViewById(R.id.point);

        int resid = R.drawable.ic_point_red;
        String point;

        if (mDatabase.getCapturedLocations().containsKey(id)) {
            point = mDatabase.getCapturedLocations().get(id).toString();
            resid = R.drawable.ic_point_green;

        } else if (mDatabase.getFailedLocations().containsKey(id)) {
            point = mDatabase.getFailedLocations().get(id).toString();

        } else {
            point = "0";
        }

        point += "pt";
        pointView.setText(point);
        pointView.setBackgroundResource(resid);
    }

    private void changeLocationAddress(String address) {
        TextView view = mBottomSheet.findViewById(R.id.location_address);
        view.setText(address);
    }

    private void changeLocationPhone(String phone) {
        TextView view = mBottomSheet.findViewById(R.id.phone);
        view.setText(phone);
    }

    private void changeLocationMail(String mail) {
        TextView view = mBottomSheet.findViewById(R.id.mail);
        view.setText(mail);
    }

    private void changeLocationDescription(String des) {
        TextView view = mBottomSheet.findViewById(R.id.description);
        view.setText(des);
    }

    private void changeLocationImages(ArrayList<String> imageUrls) {
        LinearLayout layout = mBottomSheet.findViewById(R.id.location_images);
        layout.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity= Gravity.CENTER_HORIZONTAL;

        Picasso.get().setLoggingEnabled(true);
        for (String url: imageUrls) {
            ImageView imageView =  new ImageView(mActivity);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(params);

            addImage(imageView, url);
            layout.addView(imageView);
        }
    }

    private void addImage(final ImageView imageView, final String imgURL) {
        Picasso.get()
                .load(imgURL)
                .into(new Target() {
                    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        imageView.setImageBitmap(bitmap);
                        imageView.invalidate();
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) { }

                    @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
                });
    }
}
