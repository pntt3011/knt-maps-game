package com.example.maplogin.ui.ar;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class SelectingRecyclerAdapter extends RecyclerView.Adapter<SelectingRecyclerAdapter.SelectingViewHolder> {

    private final OnSelectItemListener onSelectItemListener;
    private final LayoutInflater layoutInflater;
    private final ArrayList<Map.Entry<String, ArViewModel.ShopItemExt>> items;

    public interface OnSelectItemListener {
        void processSelectedItem(Map.Entry<String, ArViewModel.ShopItemExt> entry);
    }

    public SelectingRecyclerAdapter(Context context, ArrayList<Map.Entry<String, ArViewModel.ShopItemExt>> items,
                                    OnSelectItemListener onSelectItemListener) {
        this.onSelectItemListener = onSelectItemListener;
        this.layoutInflater = LayoutInflater.from(context);
        this.items = items;
    }

    @NonNull
    @Override
    public SelectingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =this.layoutInflater.inflate(R.layout.selecting_item, parent, false);
        return new SelectingRecyclerAdapter.SelectingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectingViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class SelectingViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;;
        private final ImageView photo;
        private final Button buttonSelect;

        public SelectingViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.shop_item_name);
            photo = itemView.findViewById(R.id.shop_item_photo);
            buttonSelect = itemView.findViewById(R.id.btn_select);
        }

        public void bind(Map.Entry<String, ArViewModel.ShopItemExt> entry) {
            name.setText(entry.getValue().name);
            Picasso.get().load(Uri.parse(entry.getValue().photo))
                    .fit().into(photo);
            buttonSelect.setOnClickListener(v -> onSelectItemListener.processSelectedItem(entry));
        }
    }
}
