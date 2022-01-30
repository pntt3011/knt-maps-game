package com.example.maplogin;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.CameraStream;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;

public class MyArActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private Renderable model;

    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED
    }

    private Anchor anchor;
    private AppAnchorState appAnchorState = AppAnchorState.NONE;
    private boolean isPlaced = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_my_ar);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        prefs = getSharedPreferences("AnchorId", MODE_PRIVATE);
        editor = prefs.edit();

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        loadModels();
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);
        subscribeListeners();
    }

    public void loadModels() {
        WeakReference<MyArActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MyArActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (arFragment.getArSceneView().getSession() == null) {
            Toast.makeText(this, "Load session failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPlaced) {
            anchor = arFragment.getArSceneView().getSession()
                    .hostCloudAnchor(hitResult.createAnchor());
            appAnchorState = AppAnchorState.HOSTING;
            placeModel(anchor);
            isPlaced = true;
        }
    }

    private void placeModel(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMinScale(0.05f);
        model.getScaleController().setMaxScale(1.0f);
        model.setParent(anchorNode);
        model.setRenderable(this.model);
        model.select();
    }

    private void subscribeListeners() {
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (appAnchorState != AppAnchorState.HOSTING)
                return;
            Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

            if (cloudAnchorState.isError()) {
                Toast.makeText(this, cloudAnchorState.toString(), Toast.LENGTH_SHORT).show();

            } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                appAnchorState = AppAnchorState.HOSTED;

                String anchorId = anchor.getCloudAnchorId();
                editor.putString("anchorId", anchorId);
                editor.apply();

                Toast.makeText(this, "Id: " + anchorId, Toast.LENGTH_SHORT).show();
            }
        });

        Button button = findViewById(R.id.btn_resolve);
        button.setOnClickListener(v-> {
            String anchorId = prefs.getString("anchorId", "null");

            if (anchorId.equals("null")) {
                Toast.makeText(this, "No anchor found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (arFragment.getArSceneView().getSession() == null) {
                Toast.makeText(this, "Load session failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);
            placeModel(resolvedAnchor);
        });
    }
}
