package com.example.maplogin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.maplogin.utils.Constants;
import com.google.zxing.Result;

public class ScanActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private boolean cameraPermissionGranted;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCameraPermission();
        locationId = getIntent().getStringExtra(Constants.LOCATION_ID);
    }

    private void setupCamera() {
        setContentView(R.layout.qr_scanner_layout);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                String id = result.getText();
                Log.d("hehe", id + " " + locationId);
                if (!id.equals(locationId)) {
                    runOnUiThread(() ->
                            Toast.makeText(ScanActivity.this,
                                    "You have scanned wrong QR code, please try again with different one",
                                    Toast.LENGTH_SHORT).show());
                }
                else {
                    runOnUiThread(() ->
                            Toast.makeText(ScanActivity.this,
                                    "Scanned correct barcode, starting quiz...",
                                    Toast.LENGTH_LONG).show());
                    Intent i = new Intent(ScanActivity.this, QuizActivity.class);
                    i.putExtra(Constants.LOCATION_ID, id);
                    ScanActivity.this.startActivity(i);
                }
            }
        });
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true;
            setupCamera();
        }
        else {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        cameraPermissionGranted = isGranted;
                        if (!isGranted) { finish(); }
                        else { setupCamera(); }
                    });
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
