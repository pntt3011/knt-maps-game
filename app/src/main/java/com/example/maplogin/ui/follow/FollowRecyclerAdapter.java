package com.example.maplogin.ui.follow;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowRecyclerAdapter
        extends RecyclerView.Adapter<FollowRecyclerAdapter.FollowViewHolder> {

    private static final String TAG = "FollowRecyclerAdapter";
    private final List<Map.Entry<String, User>> follows = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.people_item, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        holder.bind(follows.get(position));
    }

    @Override
    public int getItemCount() {
        return follows.size();
    }

    public void setFollows(Map<String, User> userMaps){
        follows.clear();
        follows.addAll(userMaps.entrySet());
        Log.d(TAG, follows.size() + "");
        notifyDataSetChanged();
    }

    public class FollowViewHolder extends RecyclerView.ViewHolder{
        private final TextView name;
        private final ImageView avatar;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.people_text);
            avatar = itemView.findViewById(R.id.people_image);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(follows.get(position));
                }
            });
        }

        public void bind(Map.Entry<String, User> user) {
            name.setText(user.getValue().name);
            Picasso.get().load(Uri.parse(user.getValue().photo_url))
                    .fit().into(avatar);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Map.Entry<String, User> userEntry);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
