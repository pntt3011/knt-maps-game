package com.example.maplogin.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentUserBinding;
import com.example.maplogin.models.UserLocation;
import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.utils.CheckinRecyclerAdapter;
import com.example.maplogin.utils.DatabaseAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserFragment extends Fragment {
    private FragmentUserBinding binding;
    DatabaseAdapter mDatabase;
    private Activity mActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mActivity = this.getActivity();
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = DatabaseAdapter.getInstance();

        FirebaseUser user = mDatabase.getCurrentUser();
        HashMap<String, UserLocation> capturedLocation =
                (HashMap<String, UserLocation>) mDatabase.getCapturedLocations();
        String displayName = mDatabase.isAnonymousUser() ? "Anonymous user" : user.getDisplayName();
        String textNumCheckin = Integer.toString(capturedLocation.size());

        bindAvatarUser(view);
        bindText2TextView(view, R.id.name, displayName);
        bindText2TextView(view, R.id.numCheckin, textNumCheckin);
        bindText2TextView(view, R.id.numBadges, "0");
        if (!mDatabase.isAnonymousUser()) {
            bindText2TextView(view, R.id.uid_text, mDatabase.getCurrentUser().getUid());
        }
        else {
            bindText2TextView(view, R.id.uid_text, "Google Account only");
        }
        
        addDataToRecyclerView(view);

        view.findViewById(R.id.copy_button).setOnClickListener(v -> {
            TextView uid = view.findViewById(R.id.uid_text);
            ClipboardManager myClipboard =
                    (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            String text = uid.getText().toString();

            ClipData myClip = ClipData.newPlainText("text", text);
            myClipboard.setPrimaryClip(myClip);
            Toast.makeText(getContext(), "UID copied", Toast.LENGTH_SHORT).show();
        });
    }

    private void addDataToRecyclerView(View view) {
        // get location and captured location info
        HashMap<String, LocationInfo> locationInfoHashMap =
                (HashMap<String, LocationInfo>) mDatabase.getAllLocations();
        HashMap<String, UserLocation> mCaptured =
                (HashMap<String, UserLocation>) DatabaseAdapter.getInstance().getCapturedLocations();

        // filter non-captured location out from list of entries
        ArrayList<Map.Entry<String, LocationInfo>> locationEntries = new ArrayList<>();
        for (Map.Entry<String, LocationInfo> entry:  locationInfoHashMap.entrySet())
            if (mCaptured.containsKey(entry.getKey()))
                locationEntries.add(entry);

        // set recycler view adapter
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_people);
        CheckinRecyclerAdapter adapter = new CheckinRecyclerAdapter(
                mActivity,
                locationEntries,
                mDatabase.getCapturedLocations());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
    }

    private void bindText2TextView(View view, int id, String text) {
        TextView textView = view.findViewById(id);
        textView.setText(text);
    }

    private void bindAvatarUser(View view) {
        ImageView avatar = view.findViewById(R.id.avatar);
        if (!mDatabase.isAnonymousUser()) {
            Picasso.get().load(mDatabase.getCurrentUser().getPhotoUrl())
                    .fit().into(avatar);
        } else {
            avatar.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}