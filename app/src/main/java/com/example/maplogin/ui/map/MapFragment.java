package com.example.maplogin.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentMapBinding;
import com.example.maplogin.utils.DatabaseAdapter;
import com.example.maplogin.utils.MarkerController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayout;
import com.mahc.custombottomsheetbehavior.MergedAppBarLayoutBehavior;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentMapBinding binding;
    private DatabaseAdapter mDatabase;
    private GoogleMap mMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mDatabase = DatabaseAdapter.getInstance();
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startSyncMap();
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
        Context ctx = getActivity();
        if (ctx != null) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_style));
            mMap.setPadding(0, 0, 0, R.dimen.bottomSheetPeekHeight);
        }

        setupBottomSheet();
        setupMarkerController();
        mDatabase.startSync();
    }

    private void setupBottomSheet() {
        View view = getView();
        if (view == null)
            return;

        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        BottomSheetBehaviorGoogleMapsLike<View> behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);

        MergedAppBarLayout mergedAppBarLayout = view.findViewById(R.id.mergedappbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("Title Dummy");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(v ->
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT));

//        ItemPagerAdapter adapter = new ItemPagerAdapter(this,mDrawables);
//        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
//        viewPager.setAdapter(adapter);

        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
    }

    private void setupMarkerController() {
        MarkerController markerController = new MarkerController(mMap);
        mDatabase.setModifyLocationListener(
                markerController.getLocationListener());
        mDatabase.setModifyCaptureListener(
                markerController.getCaptureListener());
    }
}