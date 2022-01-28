package com.example.maplogin.ui.follow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.Map;

public class FollowFragment extends Fragment {
    private FragmentFollowBinding binding;
    private FollowViewModel viewModel;
    private FollowRecyclerAdapter adapter;

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
        initRecyclerView(view);
        subscribeListeners(view);
        subscribeObservers();
    }

    private void initRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_people);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                Toast.makeText(getContext(), "Invalid user id.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInfo(Map.Entry<String, User> user) {
        // TODO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
