package com.example.maplogin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maplogin.ui.MapFragment;
import com.example.maplogin.ui.UserFragment;
import com.example.maplogin.utils.DatabaseAdapter;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

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

        setupDatabase();
        setupNavigation(savedInstanceState);
    }

    private void setupDatabase() {
        DatabaseAdapter.updateUserInfo(this);
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
        mDatabase.loadUserIcon(this, photoView);
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

    private void switchToMap() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MapFragment()).commit();
        mNavigationView.setCheckedItem(R.id.nav_map);
        changeTitle("Map");
    }

    private void switchToUser() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new UserFragment()).commit();
        mNavigationView.setCheckedItem(R.id.nav_user);
        changeTitle("User info");
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