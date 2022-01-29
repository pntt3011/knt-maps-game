package com.example.maplogin.ui.follow;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentFollowBinding;
import com.example.maplogin.models.User;
import com.example.maplogin.models.UserLocation;
import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.utils.CheckinRecyclerAdapter;
import com.example.maplogin.utils.DatabaseAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FollowFragment extends Fragment {
    private FragmentFollowBinding binding;
    private FollowViewModel viewModel;
    private FollowRecyclerAdapter adapter;
    private DatabaseAdapter database;
    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFollowBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = DatabaseAdapter.getInstance();
        activity = getActivity();
        initRecyclerView(view);
        subscribeListeners(view);
        subscribeObservers();
    }

    private void initRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_people);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        adapter = new FollowRecyclerAdapter();
        adapter.setOnItemClickListener(this::showInfo);
        recyclerView.setAdapter(adapter);
    }

    private void subscribeListeners(View view) {
        ImageButton addBtn = view.findViewById(R.id.follow_button);
        EditText uid = view.findViewById((R.id.id_search_text));
        addBtn.setOnClickListener(v -> viewModel.followUser(uid.getText().toString()));
    }

    private void subscribeObservers() {
        viewModel = new ViewModelProvider(this).get(FollowViewModel.class);
        viewModel.getFollowsLiveData().removeObservers(getViewLifecycleOwner());
        viewModel.getFollowsLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                adapter.setFollows(users);
            }
        });

        viewModel.getAddFollowResult().removeObservers(getViewLifecycleOwner());
        viewModel.getAddFollowResult().observe(getViewLifecycleOwner(), status -> {
            if (status == null || status.equals("ERROR")) {
                Toast.makeText(activity, "Invalid user id.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInfo(Map.Entry<String, User> user) {
        final Dialog infoDialog = new Dialog(activity);
        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infoDialog.setContentView(R.layout.person_infor_dialog);

        Window window = infoDialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        infoDialog.setCancelable(true);

        addInfo(infoDialog, user);
        infoDialog.show();
    }

    private void addInfo(Dialog infoDialog, Map.Entry<String, User> user) {
        bindAvatarUser(infoDialog, user.getValue().photo_url);
        bindText2TextView(infoDialog, R.id.name, getUserName(user.getValue()));
        bindText2TextView(infoDialog, R.id.numCheckin, getUserCheckInSize(user.getValue()));
        bindText2TextView(infoDialog, R.id.numBadges, "0");
        bindText2TextView(infoDialog, R.id.uid_text, user.getKey());
        addDataToRecyclerView(infoDialog, getUserCaptured(user.getValue()));

        infoDialog.findViewById(R.id.copy_button).setOnClickListener(v -> {
            TextView uid = infoDialog.findViewById(R.id.uid_text);
            ClipboardManager myClipboard =
                    (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            String text = uid.getText().toString();

            ClipData myClip = ClipData.newPlainText("text", text);
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(activity, "UID copied", Toast.LENGTH_SHORT).show();
        });
    }

    private String getUserName(User value) {
        if (value.name == null)
            return "";
        return value.name;
    }

    private Map<String, UserLocation> getUserCaptured(User value) {
        if (value.captured == null)
            return new HashMap<>();
        return value.captured;
    }

    private String getUserCheckInSize(User user) {
        return Long.toString(getUserCaptured(user).size());
    }

    private void bindAvatarUser(Dialog dialog, String url) {
        ImageView avatar = dialog.findViewById(R.id.avatar);
        Picasso.get().load(Uri.parse(url))
                .fit().into(avatar);
    }

    private void bindText2TextView(Dialog dialog, int id, String text) {
        TextView textView = dialog.findViewById(id);
        textView.setText(text);
    }

    private void addDataToRecyclerView(Dialog dialog, Map<String, UserLocation> captured) {
        // get location and captured location info
        HashMap<String, LocationInfo> locationInfoHashMap =
                (HashMap<String, LocationInfo>) database.getAllLocations();

        // filter non-captured location out from list of entries
        ArrayList<Map.Entry<String, LocationInfo>> locationEntries = new ArrayList<>();
        for (Map.Entry<String, LocationInfo> entry:  locationInfoHashMap.entrySet())
            if (captured.containsKey(entry.getKey()))
                locationEntries.add(entry);

        // set recycler view adapter
        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_view_people);
        CheckinRecyclerAdapter adapter = new CheckinRecyclerAdapter(activity, locationEntries, captured);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
