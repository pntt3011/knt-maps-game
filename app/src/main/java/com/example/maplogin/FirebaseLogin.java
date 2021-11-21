package com.example.maplogin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class FirebaseLogin extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    // below is the list which we have created in which
    // we can add the authentication which we have to
    // display inside our app.
    private List<AuthUI.IdpConfig> providers;

    // variable for Firebase Auth
    private FirebaseAuth mFirebaseAuth;

    // creating an auth listener for our Firebase auth
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_placeholder);

        boolean isUpgrade = checkIsUpgrade();
        setupProviders(isUpgrade);
        checkUserLogIn(isUpgrade);
    }

    private boolean checkIsUpgrade() {
        return getIntent().getBooleanExtra("UPGRADE", false);
    }

    private void setupProviders(boolean isUpgrade) {
        if (isUpgrade) {
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());

        } else {
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.AnonymousBuilder().build());
        }
    }

    private void checkUserLogIn(boolean isUpgrade) {
        // below line is for getting instance
        // for our firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // below line is used for calling auth listener
        // for oue Firebase authentication.
        mAuthStateListener = firebaseAuth -> {

            // we are calling method for on authentication state changed.
            // below line is used for getting current user which is
            // authenticated previously.
            FirebaseUser user = firebaseAuth.getCurrentUser();

            // checking if the user
            // is null or not.
            if (user != null && !isUpgrade) {
                // if the user is already authenticated then we will
                // redirect our user to next screen which is our home screen.
                // we are redirecting to new screen via an intent.
                startMainActivity();

            } else {
                // below line is used for getting
                // our authentication instance.
                launchSignInProviders();
            }
        };
    }

    private void launchSignInProviders() {
        // Custom layout
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_login)
                .setGoogleButtonId(R.id.buttonLoginGoogle)
                .setAnonymousButtonId(R.id.buttonLoginGuest)
                .build();

        Intent intent = AuthUI.getInstance()
                // below line is used to
                // create our sign in intent
                .createSignInIntentBuilder()

                // below line is used for adding smart
                // lock for our authentication.
                // smart lock is used to check if the user
                // is authentication through different devices.
                // currently we are disabling it.
                .setIsSmartLockEnabled(false)

                // we are adding different login providers which
                // we have mentioned above in our list.
                // we can add more providers according to our
                // requirement which are available in firebase.
                .setAvailableProviders(providers)

                // Enabling anonymous user upgrade
                .enableAnonymousUsersAutoUpgrade()

                .setTheme(R.style.Theme_MapLogin)

                // Set custom authUI
                .setAuthMethodPickerLayout(customLayout)

                // after setting our theme and logo
                // we are calling a build() method
                // to build our login screen.
                .build();

        signInLauncher.launch(intent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            startMainActivity();

        } else {
            // Sign in failed
            if (response.getError().getErrorCode() == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                Toast.makeText(
                        FirebaseLogin.this,
                        "This account is being used.",
                        Toast.LENGTH_SHORT)
                        .show();
                startMainActivity();

            } else {
                Toast.makeText(
                        FirebaseLogin.this,
                        "Login error",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(FirebaseLogin.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // we are calling our auth
        // listener method on app resume.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // here we are calling remove auth
        // listener method on stop.
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
}