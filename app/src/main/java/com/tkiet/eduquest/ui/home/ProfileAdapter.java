package com.tkiet.eduquest.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tkiet.eduquest.R;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<UserProfile> profiles;
    private Context context;

    public ProfileAdapter(List<UserProfile> profiles, Context context) {
        this.profiles = profiles;
        this.context = context;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        UserProfile profile = profiles.get(position);
        Log.d("Profilefragment", "data found.");

        // Display profile details
        holder.textViewName.setText(profile.getName());
        holder.textViewSkills.setText(profile.getSkills());

        // Handle the profile image using Glide
        Glide.with(context)
                .load(profile.getImageUrl())
                .placeholder(R.drawable.account_manage) // Add a default image for empty URLs
                .into(holder.imageViewProfile);

        // Handle item click to open profile
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileDetailActivity.class);
            intent.putExtra("userId", profile.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfile;
        TextView textViewName, textViewSkills;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewSkills = itemView.findViewById(R.id.textViewSkills);
        }
    }
}
