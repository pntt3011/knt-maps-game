package com.example.maplogin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.example.maplogin.databinding.ActivityMapsBinding;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Google map
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Supporting modules
    private FacebookShare mShare;
    private DatabaseAdapter mDatabase;
    private MarkerController mMarkerController;

    // Bottom sheet
    private TextView mBottomSheetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupMap();
        setupFbShare();
        setupDatabase();
        setupBottomSheet();
        startSyncMap();
    }

    private void setupMap() {
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setupFbShare() {
        mShare = new FacebookShare(this);
    }

    private void setupDatabase() {
        DatabaseAdapter.updateUserInfo();
        mDatabase = DatabaseAdapter.getInstance();
    }

    private void setupBottomSheet() {
        /*
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });

        MergedAppBarLayout mergedAppBarLayout = findViewById(R.id.mergedappbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("Title Dummy");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(v ->
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT));

        mBottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
//        ItemPagerAdapter adapter = new ItemPagerAdapter(this,mDrawables);
//        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
//        viewPager.setAdapter(adapter);

        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
        //behavior.setCollapsible(false);
    }

    private void startSyncMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.setPadding(0, 0, 0, R.dimen.bottomSheetPeekHeight);

        setupLogoutButton();
        setupShareButton();
        setupLinkButton();
        setupMarkerController();

        mDatabase.startSync();
    }

    private void setupLogoutButton() {
        ImageButton logoutBtn = findViewById(R.id.logout_button);
        // adding onclick listener for our logout button.
        logoutBtn.setOnClickListener(v -> mDatabase.logoutCurrentUser(this));
    }

    private void setupShareButton() {
        ImageButton shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v ->
                mShare.shareLink("DH KHTN", "https://www.fit.hcmus.edu.vn/vn/"));
    }

    private void setupLinkButton() {
        ImageButton linkButton = findViewById(R.id.link_button);
        if (mDatabase.isAnonymousUser()) {
            linkButton.setOnClickListener(v ->
                    mDatabase.startLoginActivity(true, this));
        } else {
            linkButton.setClickable(false);
        }
    }

    private void setupMarkerController() {
        mMarkerController = new MarkerController(mMap);
        mDatabase.setModifyLocationListener(
                mMarkerController.getLocationListener());
        mDatabase.setModifyCaptureListener(
                mMarkerController.getCaptureListener());
    }
}