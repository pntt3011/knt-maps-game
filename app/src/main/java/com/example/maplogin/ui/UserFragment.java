package com.example.maplogin.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentUserBinding;
import com.example.maplogin.utils.DatabaseAdapter;
import com.example.maplogin.utils.MarkerController;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    DatabaseAdapter mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = DatabaseAdapter.getInstance();

        FirebaseUser user = mDatabase.getCurrentUser();
        Map<String, Long> capturedLocation = mDatabase.getCapturedLocations();
        String textNumCheckin = Integer.toString(capturedLocation.size());

        bindAvatarUser(view);
        bindText2TextView(view, R.id.name, user.getDisplayName());
        bindText2TextView(view, R.id.numCheckin, textNumCheckin);
        bindText2TextView(view, R.id.numBadges, "0");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void bindText2TextView(View view, int id, String text) {
        TextView textView = view.findViewById(id);
        textView.setText(text);
    }

    private void bindAvatarUser(View view) {
        ImageView avatar = view.findViewById(R.id.avatar);
        avatar.setImageBitmap(mDatabase.getUserIcon());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}