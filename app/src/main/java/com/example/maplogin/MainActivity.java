package com.example.maplogin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.maplogin.ui.map.MapFragment;
import com.example.maplogin.ui.user.UserFragment;
import com.example.maplogin.utils.DatabaseAdapter;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maplogin.databinding.ActivityNavigationDrawerBinding;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Handle navigation item select
    private NavigationView navigationView;

    // Supporting modules
    private DatabaseAdapter mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupDatabase();
        setupNavigationView(savedInstanceState);
    }

    private void setupDatabase() {
        DatabaseAdapter.updateUserInfo();
        mDatabase = DatabaseAdapter.getInstance();
    }

    private void setupNavigationView(Bundle savedInstanceState) {
        ActivityNavigationDrawerBinding binding =
                ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.appBarNavigationDrawer.toolbar;
        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            switchToMap();
        }
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
        navigationView.setCheckedItem(R.id.nav_map);
    }

    private void switchToUser() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new UserFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_user);
    }

    private void linkAccount() {
        if (mDatabase.isAnonymousUser()) {
            mDatabase.startLoginActivity(true, this);
        } else {
            Toast.makeText(this, "This account has already been linked.", Toast.LENGTH_SHORT).show();
        }
    }
}