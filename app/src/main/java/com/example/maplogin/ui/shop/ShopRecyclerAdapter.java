package com.example.maplogin.ui.shop;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.models.ShopItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopRecyclerAdapter
    extends RecyclerView.Adapter<ShopRecyclerAdapter.ShopViewHolder> {

    private final List<Map.Entry<String, ShopItemExt>> items = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ShopItemExt extends ShopItem {
        public boolean hasOwned;

        public ShopItemExt(ShopItem item, boolean hasOwned) {
            super(item);
            this.hasOwned = hasOwned;
        }
    }

    public void setItems(Map<String, ShopItemExt> items){
        this.items.clear();
        this.items.addAll(items.entrySet());
        notifyDataSetChanged();
    }

    public class ShopViewHolder extends RecyclerView.ViewHolder{
        private final TextView name;
        private final TextView point;
        private final ImageView photo;
        private final TextView owned;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.shop_item_name);
            point = itemView.findViewById(R.id.shop_item_point);
            photo = itemView.findViewById(R.id.shop_item_photo);
            owned = itemView.findViewById(R.id.shop_item_owned);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Map.Entry<String, ShopItemExt> item) {
            name.setText(item.getValue().name);
            point.setText(item.getValue().point + "pt");
            if (!item.getValue().hasOwned) {
                owned.setText("");
            } else {
                owned.setText("You already own this model.");
            }
            Picasso.get().load(Uri.parse(item.getValue().photo))
                    .fit().into(photo);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Map.Entry<String, ShopItemExt> item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

