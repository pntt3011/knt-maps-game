package com.example.maplogin;

import android.annotation.SuppressLint;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maplogin.ui.ar.MyArFragment;
import com.example.maplogin.ui.follow.FollowFragment;
import com.example.maplogin.ui.MapFragment;
import com.example.maplogin.ui.UserFragment;
import com.example.maplogin.ui.history.HistoryFragment;
import com.example.maplogin.ui.shop.ShopFragment;
import com.example.maplogin.utils.DatabaseAdapter;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.maplogin.databinding.ActivityNavigationDrawerBinding;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Handle navigation drawer view
    private NavigationView mNavigationView;
    private View mHeaderView;

    // Just to change title
    private Toolbar mToolbar;

    // Supporting modules
    private DatabaseAdapter mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setupDatabase();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            setupNavigation(savedInstanceState);

        } else {
            Toast.makeText(this, "Please enable GPS to use this app.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupDatabase() {
        DatabaseAdapter.updateUserInfo();
        mDatabase = DatabaseAdapter.getInstance();
    }

    private void setupNavigation(Bundle savedInstanceState) {
        ActivityNavigationDrawerBinding binding =
                ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupNavigationView(binding);
        setupNavigationToggle(binding);
        setupNavigationUserInfo();

        if (savedInstanceState == null) {
            switchToMap();
        }
    }

    private void setupNavigationView(ActivityNavigationDrawerBinding binding) {
        mNavigationView = binding.navView;
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void setupNavigationToggle(ActivityNavigationDrawerBinding binding) {
        mToolbar = binding.appBarNavigationDrawer.toolbar;
        setSupportActionBar(mToolbar);
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationUserInfo() {
        mHeaderView = mNavigationView.getHeaderView(0);
        setupNavigationPhoto();
        setupNavigationName();
        setupNavigationEmail();
    }

    private void setupNavigationPhoto() {
        ImageView photoView = mHeaderView.findViewById(R.id.nav_header_photo);
        if (!mDatabase.isAnonymousUser()) {
            Picasso.get().load(mDatabase.getCurrentUser().getPhotoUrl())
                    .fit().into(photoView);
        } else {
            photoView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void setupNavigationName() {
        TextView nameView = mHeaderView.findViewById(R.id.nav_header_name);
        String name;
        if (!mDatabase.isAnonymousUser()) {
            name = mDatabase.getCurrentUser().getDisplayName();
        } else {
            name = "Anonymous user";
        }
        nameView.setText(name);
    }

    private void setupNavigationEmail() {
        TextView emailView = mHeaderView.findViewById(R.id.nav_header_email);
        String email;
        if (!mDatabase.isAnonymousUser()) {
            email = mDatabase.getCurrentUser().getEmail();
        } else {
            email = "";
        }
        emailView.setText(email);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                switchToMap();
                break;

            case R.id.nav_user:
                switchToUser();
                break;

            case R.id.nav_ar:
                switchToAr();
                break;

            case R.id.nav_history:
                switchToHistory();
                break;

            case R.id.nav_follow:
                switchToFollow();
                break;

            case R.id.nav_shop:
                switchToShop();
                break;

            case R.id.nav_link:
                linkAccount();
                break;

            case R.id.nav_logout:
                mDatabase.logoutCurrentUser(this);
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchToAr() {
        switchToFragment(new MyArFragment(), R.id.nav_ar, "AR");
    }

    private void switchToMap() {
        switchToFragment(new MapFragment(), R.id.nav_map, "Map");
    }

    private void switchToUser() {
        switchToFragment(new UserFragment(), R.id.nav_user, "User info");
    }

    private void switchToHistory() {
        switchToFragment(new HistoryFragment(), R.id.nav_history, "History");
    }

    private void switchToFollow() {
        if (!mDatabase.isAnonymousUser()) {
            switchToFragment(new FollowFragment(), R.id.nav_follow, "Following");
        } else {
            Toast.makeText(this, "Anonymous user cannot follow other users.", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToShop() {
        switchToFragment(new ShopFragment(), R.id.nav_shop, "Shop");
    }

    private void switchToFragment(Fragment fragment, int id, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
        mNavigationView.setCheckedItem(id);
        changeTitle(title);
    }

    private void changeTitle(String s) {
        if (mToolbar != null) {
            mToolbar.setTitle(s);
        }
    }

    private void linkAccount() {
        if (mDatabase.isAnonymousUser()) {
            mDatabase.startLoginActivity(true, this);
        } else {
            Toast.makeText(this, "This account has already been linked.", Toast.LENGTH_SHORT).show();
        }
    }
}