package com.example.maplogin.utils;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.struct.LocationInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NearestRecyclerAdapter extends RecyclerView.Adapter<NearestRecyclerAdapter.LocationViewHolder> {
    private LayoutInflater layoutInflater;
    private ArrayList<LocationInfo> items;

    public NearestRecyclerAdapter(Context context, ArrayList<LocationInfo> locations) {
        layoutInflater = LayoutInflater.from(context);
        items = locations;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.nearest_location_item, parent, false);
        return new LocationViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationInfo currentLocation = items.get(position);
        holder.setIcon(currentLocation.iconUrl);
        holder.setName(currentLocation.name);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder{
        private ImageView iconView = null;
        private TextView nameView = null;
        private NearestRecyclerAdapter adapter;

        public LocationViewHolder(View itemView, NearestRecyclerAdapter songAdapter) {
            super(itemView);
            adapter = songAdapter;
            iconView = itemView.findViewById(R.id.iconLocation);
            nameView = itemView.findViewById(R.id.titleLocation);

//            btnPlay = itemView.findViewById(R.id.btnPlay);
//            btnPlay.setOnClickListener(this);
//            edtTitle = itemView.findViewById(R.id.edtTitle);
//            edtTitle.setKeyListener(null);
//            edtTitle.setTextColor(Color.RED);
        }

        private void setName(String name) {
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
    }
}
