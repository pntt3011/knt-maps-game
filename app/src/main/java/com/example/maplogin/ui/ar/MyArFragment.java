package com.example.maplogin.ui.ar;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.MediatorLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentMyArBinding;
import com.example.maplogin.models.ShopItem;
import com.example.maplogin.ui.ar.MultiArViewModel.AnchorExt;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.CameraStream;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MyArFragment extends Fragment implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private FragmentMyArBinding binding;
    private Activity mActivity;
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
    private MultiArViewModel viewModel;
    private MediatorLiveData<ShopItem> currentItemLiveData;
    private MediatorLiveData<HashMap<String, ArViewModel.ShopItemExt>> ownedItemsLiveData;
    private MediatorLiveData<HashMap<String, MultiArViewModel.AnchorExt>> anchorsInfoLiveData;
    private SelectingRecyclerAdapter adapter;
    private Dialog dialog;
    private String comment;
    private HashSet<String> syncModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        getChildFragmentManager().addFragmentOnAttachListener(this);

        viewModel = new MultiArViewModel();
        syncModel = new HashSet<>();
        subscribeObservers();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // create new AR fragment
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.arFragment, ArFragment.class, null)
                    .addToBackStack("")
                    .commit();
        }
        binding = FragmentMyArBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
        mActivity = getActivity();
        arSceneView.getCameraStream()
                .setDepthOcclusionMode(CameraStream.DepthOcclusionMode.DEPTH_OCCLUSION_ENABLED);
        subscribeListeners();
    }

    private void subscribeObservers() {
        // setup callback to load model when select item
        currentItemLiveData = viewModel.getCurrentItemLiveData();
        currentItemLiveData.observe(this, currentItem -> loadModels(currentItem.model));

        // load all anchor into user view
        anchorsInfoLiveData = viewModel.getAnchorsInfoLiveData();
        anchorsInfoLiveData.observe(this, anchors -> {
            if (arFragment.getArSceneView().getSession() == null) {
                Toast.makeText(mActivity, "Load session failed.", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Map.Entry<String, MultiArViewModel.AnchorExt> anchor: anchors.entrySet()) {
                if (!syncModel.contains(anchor.getKey())) {
                    Anchor resolvedAnchor = arFragment.getArSceneView().getSession()
                            .resolveCloudAnchor(anchor.getKey());
                    syncModel.add(anchor.getKey());
                    loadAndPlaceCloudAnchorsModel(anchor.getValue(), resolvedAnchor);
                }
            }
        });

        // set callback for adapter of recycler view that show all owned item for selecting
        ownedItemsLiveData = viewModel.getOwnedItemsLiveData();
        ownedItemsLiveData.observe(this, itemHashMap -> {
            ArrayList<Map.Entry<String, ArViewModel.ShopItemExt>> itemList = new ArrayList<>(itemHashMap.entrySet());
            adapter = new SelectingRecyclerAdapter(mActivity, itemList, entry -> {
                viewModel.selectItem(entry.getKey());
                Toast.makeText(mActivity, "Selected model " + entry.getValue().name, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
        });
    }

    private void subscribeListeners() {
        // upload anchor when push successful to Cloud Anchor
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (appAnchorState != AppAnchorState.HOSTING)
                return;
            Anchor.CloudAnchorState cloudAnchorState = anchor.getCloudAnchorState();

            if (cloudAnchorState.isError()) {
                Toast.makeText(mActivity, cloudAnchorState.toString(), Toast.LENGTH_SHORT).show();
//                Log.d("hehe", cloudAnchorState.toString());

            } else if (cloudAnchorState == Anchor.CloudAnchorState.SUCCESS) {
                appAnchorState = AppAnchorState.HOSTED;

                String anchorId = anchor.getCloudAnchorId();
                viewModel.placeAnchor(anchorId, comment);
                Toast.makeText(mActivity, "Id: " + anchorId, Toast.LENGTH_SHORT).show();
            }
        });

        Button button = binding.btnSelect;
        button.setOnClickListener(v -> showSelectingDialog());
    }

    public void loadModels(String modelUrl) {
        WeakReference<MyArFragment> weakFragment = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(mActivity, Uri.parse(modelUrl))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MyArFragment fragment = weakFragment.get();
                    if (fragment != null) {
                        fragment.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            mActivity, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void loadAndPlaceCloudAnchorsModel(AnchorExt anchorExt, Anchor anchor) {
        ModelRenderable.builder()
                .setSource(mActivity, Uri.parse(anchorExt.modelID))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> loadAndPlaceComment(anchorExt, anchor, model))
                .exceptionally(throwable -> {
                    Toast.makeText(
                            mActivity, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void loadAndPlaceComment(AnchorExt anchorExt, Anchor anchor, Renderable model) {
        ViewRenderable.builder()
                .setView(mActivity, R.layout.comment_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    fillCommentInfo(viewRenderable.getView(), anchorExt);
                    placeModel(anchor, model, viewRenderable);
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            mActivity, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void fillCommentInfo(View v, AnchorExt anchorExt) {
        TextView name = v.findViewById(R.id.comment_name);
        if (anchorExt.userInfo.name != null) {
            name.setText(anchorExt.userInfo.name);
        } else {
            name.setText("Anonymous");
        }

        TextView content = v.findViewById(R.id.comment_content);
        content.setText(anchorExt.comment);
    }

    private void placeModel(Anchor anchor, Renderable modelRenderable,
                            Renderable userCommentRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMinScale(0.05f);
        model.getScaleController().setMaxScale(1.0f);
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);

        if (userCommentRenderable != null) {
            Node titleNode = new Node();
            titleNode.setParent(model);
            titleNode.setEnabled(false);
            titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
            titleNode.setRenderable(userCommentRenderable);
            titleNode.setEnabled(true);
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null) {
            Toast.makeText(mActivity, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPlaced) {
            AnchorExt anchorExt = createTempAnchorExt();
            showCommentDialog(anchorExt, hitResult);
        }
    }

    private AnchorExt createTempAnchorExt() {
        return new AnchorExt(
                new com.example.maplogin.models.Anchor(),
                viewModel.getCurrentUserInfo()
        );
    }


    private void showCommentDialog(AnchorExt anchorExt, HitResult hitResult) {
        Dialog dialog = createDialog(R.layout.comment_dialog);
        if (dialog == null)
            return;

        Button placeButton = dialog.findViewById(R.id.button_place_model);
        placeButton.setOnClickListener(v -> onPlaceModelListener(dialog, anchorExt, hitResult));
        dialog.show();
    }


    private Dialog createDialog(int layoutID) {
        Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(layoutID);

        Window window = dialog.getWindow();

        if (window == null) {
            return null;
        }

        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        return dialog;
    }

    private void onPlaceModelListener(Dialog dialog, AnchorExt anchorExt, HitResult hitResult) {
        // Retrieve comment
        EditText commentEdTxt = dialog.findViewById(R.id.edit_text_comment);
        comment = commentEdTxt.getText().toString();
        anchorExt.comment = comment;

        // Host anchor
        if (arFragment.getArSceneView().getSession() == null) {
            Toast.makeText(mActivity, "Load session failed.", Toast.LENGTH_SHORT).show();
            return;
        }
        anchor = arFragment.getArSceneView().getSession()
                .hostCloudAnchor(hitResult.createAnchor());
        appAnchorState = AppAnchorState.HOSTING;
        isPlaced = true;

        dialog.dismiss();
    }

    // function showing dialog for selecting model
    private void showSelectingDialog() {
        dialog = createDialog(R.layout.dialog_select_item);
        if (dialog == null)
            return;

        dialog.show();

        // binding data (list of owned item) adapter to recycler view
        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_view_select_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(this.adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
