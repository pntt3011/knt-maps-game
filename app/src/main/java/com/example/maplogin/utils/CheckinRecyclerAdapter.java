package com.example.maplogin.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.struct.LocationInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckinRecyclerAdapter extends RecyclerView.Adapter<CheckinRecyclerAdapter.LocationViewHolder> {
    private final LayoutInflater layoutInflater;
    private final ArrayList<Map.Entry<String, LocationInfo>> items;
    private final Map<String, Long> mCaptured;

    public CheckinRecyclerAdapter(Context context,
                                  ArrayList<Map.Entry<String, LocationInfo>> locations,
                                  @NonNull Map<String, Long> captured) {
        layoutInflater = LayoutInflater.from(context);
        items = locations;
        mCaptured = captured;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.checkin_place_item, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Map.Entry<String, LocationInfo> currentEntry = items.get(position);
        LocationInfo currentLocation = currentEntry.getValue();
        holder.setIcon(currentLocation.iconUrl);
        holder.setName(currentLocation.name);
        String point = mCaptured.get(currentEntry.getKey()) + "pt";
        holder.setPoint(point);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder{
        private final ImageView iconView;
        private final TextView nameView;
        private final TextView textViewPoint;

        public LocationViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.iconLocation);
            nameView = itemView.findViewById(R.id.titleLocation);

            textViewPoint = itemView.findViewById(R.id.textViewPoint);
        }

        public void setName(String name) {
            nameView.setText(name);
        }

        public void setIcon(String url) {
            if (url.equals("empty")) {
                iconView.setImageResource(R.drawable.background);
            }
            else {
                Picasso.get().load(url).fit().into(iconView);
            }
        }

        public void setPoint(String point) {
            textViewPoint.setText(point);
        }
    }
}
