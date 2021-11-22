package com.example.maplogin.utils;

import android.content.Context;
import android.location.Location;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.struct.LocationInfo;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class NearestRecyclerAdapter extends RecyclerView.Adapter<NearestRecyclerAdapter.LocationViewHolder> {
    private LayoutInflater layoutInflater;
    private ArrayList<Map.Entry<String, LocationInfo>> items;
    public onButtonGoListener goListener;

    public interface onButtonGoListener {
        void onButtonGoListener(String locationKey);
    }

    public NearestRecyclerAdapter(Context context, ArrayList<Map.Entry<String, LocationInfo>> locationEntries,
                                  onButtonGoListener goListener) {
        this.goListener = goListener;
        layoutInflater = LayoutInflater.from(context);
        items = locationEntries;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.near_place_item, parent, false);
        return new LocationViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Map.Entry<String, LocationInfo> currentEntry = items.get(position);
        LocationInfo currentLocation = currentEntry.getValue();
        holder.setIcon(currentLocation.iconUrl);
        holder.setName(currentLocation.name);
        holder.setButtonGoListener(view -> {
            goListener.onButtonGoListener(currentEntry.getKey());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder{
        private ImageView iconView = null;
        private TextView nameView = null;
        private AppCompatImageButton buttonGo = null;
        private NearestRecyclerAdapter adapter;

        public LocationViewHolder(View itemView, NearestRecyclerAdapter locationAdapter) {
            super(itemView);
            adapter = locationAdapter;
            iconView = itemView.findViewById(R.id.iconLocation);
            nameView = itemView.findViewById(R.id.titleLocation);

            buttonGo = itemView.findViewById(R.id.go_button);
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

        public void setButtonGoListener(View.OnClickListener listener) {
            buttonGo.setOnClickListener(listener);
        }
    }
}
