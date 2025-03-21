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
// Inside ProfileAdapter.java

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        // Display profile details
        holder.textViewName.setText(profile.getName());
        holder.textViewSkills.setText(profile.getSkills());

        // Handle the profile image using Glide
        Glide.with(context)
                .load(profile.getImageUrl())
                .placeholder(R.drawable.loading)
                .into(holder.imageViewProfile);

        // Fetch and display like count from Firebase
        DatabaseReference likesRef = FirebaseDatabase.getInstance()
                .getReference("Likes")
                .child(profile.getUserId())
                .child("likeCount");

        // Attach a listener to retrieve the like count
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long likeCount = snapshot.getValue(Long.class);
                    holder.likeCountTextView.setText(String.valueOf(likeCount));
                } else {
                    holder.likeCountTextView.setText("0"); // Default to 0 if no likes
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileAdapter", "Failed to load like count", error.toException());
            }
        });

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
        TextView textViewName, textViewSkills, likeCountTextView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewSkills = itemView.findViewById(R.id.textViewSkills);
            likeCountTextView = itemView.findViewById(R.id.like_count); // Initialize the like count TextView
        }
    }
}
