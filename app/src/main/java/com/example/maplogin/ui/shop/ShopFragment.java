package com.example.maplogin.ui.shop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.example.maplogin.databinding.FragmentShopBinding;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ShopFragment extends Fragment {
    private FragmentShopBinding binding;
    private ShopRecyclerAdapter adapter;
    private TextView point;
    private ShopViewModel viewModel;
    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        assert activity != null;
        point = activity.findViewById(R.id.shop_point);
        initRecyclerView(view);
        subscribeObservers();
    }

    private void initRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_shop);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        adapter = new ShopRecyclerAdapter();
        adapter.setOnItemClickListener(this::showInfo);
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void subscribeObservers() {
        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        viewModel.getItemsLiveData().removeObservers(getViewLifecycleOwner());
        viewModel.getItemsLiveData().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setItems(items);
            }
        });

        viewModel.getBuyResult().removeObservers(getViewLifecycleOwner());
        viewModel.getBuyResult().observe(getViewLifecycleOwner(), status -> {
            if (status == null || status == ShopViewModel.BuyState.ERROR) {
                Toast.makeText(activity, "Fail to buy this item.", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getPointsLiveData().removeObservers(getViewLifecycleOwner());
        viewModel.getPointsLiveData().observe(getViewLifecycleOwner(), point -> {
            Long pt = point == null ? 0: point;
            this.point.setText(pt + "pt");
        });
    }

    private void showInfo(Map.Entry<String, ShopRecyclerAdapter.ShopItemExt> item) {
        if (item.getValue().hasOwned) {
            Toast.makeText(activity, "You already own this item.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog infoDialog = new Dialog(activity);
        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infoDialog.setContentView(R.layout.item_infor_dialog);

        Window window = infoDialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        infoDialog.setCancelable(true);

        addInfo(infoDialog, item);
        subscribeListeners(infoDialog, item.getKey());
        infoDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void addInfo(Dialog infoDialog, Map.Entry<String, ShopRecyclerAdapter.ShopItemExt> item) {
        ImageView photo = infoDialog.findViewById(R.id.item_dialog_photo);
        TextView name =  infoDialog.findViewById(R.id.item_dialog_name);
        TextView point =  infoDialog.findViewById(R.id.item_dialog_point);

        name.setText(item.getValue().name);
        point.setText(item.getValue().point + "pt");
        Picasso.get().load(Uri.parse(item.getValue().photo))
                .fit().into(photo);
    }

    private void subscribeListeners(Dialog infoDialog, String itemId) {
        Button yes = infoDialog.findViewById(R.id.item_dialog_yes);
        Button no = infoDialog.findViewById(R.id.item_dialog_no);

        no.setOnClickListener(v -> infoDialog.dismiss());
        yes.setOnClickListener(v -> {
            viewModel.buyItem(itemId);
            infoDialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
