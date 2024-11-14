package com.tkiet.eduquest.ui.home;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;
import com.tkiet.eduquest.ui.account.*;
import com.tkiet.eduquest.ui.account.VideoAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class ProfileDetailActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView, skillsTextView;
    private RecyclerView videosRecyclerView;
    private VideosAdapter videosAdapter;
    private List<VideoModel> uploadedVideos;
    private String userId;
    private ImageView likeIcon;
    private TextView likeCountTextView;
    private DatabaseReference userLikesRef;
    private FirebaseUser currentUser;
    private boolean isLiked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        skillsTextView = findViewById(R.id.skillsTextView);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();
        loadUploadedVideos();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String profileUserId = getIntent().getStringExtra("userId");

        userLikesRef = FirebaseDatabase.getInstance().getReference("Likes").child(profileUserId);

        likeIcon = findViewById(R.id.like_icon);
        likeCountTextView = findViewById(R.id.like_count);

        // Check if user has liked this profile
        checkIfLiked(profileUserId);

        // Set click listener on the like icon
        likeIcon.setOnClickListener(v -> toggleLike(profileUserId));
    }

    private void loadUserProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String skills = snapshot.child("skills").getValue(String.class);

                    Glide.with(ProfileDetailActivity.this).load(imageUrl).into(profileImageView);
                    nameTextView.setText(name);
                    skillsTextView.setText(skills);
                } else {
                    Toast.makeText(ProfileDetailActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUploadedVideos() {
        uploadedVideos = new ArrayList<>();
        DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference("videos");
        videosRef.orderByChild("addedBy").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploadedVideos.clear();
                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                    VideoModel video = videoSnapshot.getValue(VideoModel.class);

                    // Check if video is not null before setting the videoId
                    if (video != null) {
                        video.setVideoId(videoSnapshot.getKey()); // Set the unique key as videoId
                        uploadedVideos.add(video);
                    }
                }

                // Pass context and video list to VideoAdapter
                videosAdapter = new VideosAdapter(ProfileDetailActivity.this, uploadedVideos);
                videosRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileDetailActivity.this));
                videosRecyclerView.setAdapter(videosAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkIfLiked(String profileUserId) {
        userLikesRef.child("likeCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Update like count
                long count = snapshot.getValue(Long.class) != null ? snapshot.getValue(Long.class) : 0;
                likeCountTextView.setText(String.valueOf(count));

                // Check if current user has liked this profile
                userLikesRef.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        isLiked = userSnapshot.exists();
                        updateLikeIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileDetailActivity.this, "Failed to check like status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "Failed to load like count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike(String profileUserId) {
        DatabaseReference likeRef = userLikesRef.child("users").child(currentUser.getUid());

        // Toggle like status and update Firebase
        if (isLiked) {
            // Remove like and decrement count
            likeRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userLikesRef.child("likeCount").setValue(ServerValue.increment(-1));
                    isLiked = false;
                    updateLikeIcon();
                    updateLikeCountDisplay(-1);
                }
            });
        } else {
            // Add like and increment count
            likeRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userLikesRef.child("likeCount").setValue(ServerValue.increment(1));
                    isLiked = true;
                    updateLikeIcon();
                    updateLikeCountDisplay(1);
                }
            });
        }
    }

    private void updateLikeIcon() {
        if (isLiked) {
            likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.Red), PorterDuff.Mode.SRC_IN);
        } else {
            likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.blue_accent_300), PorterDuff.Mode.SRC_IN);
        }
    }

    private void updateLikeCountDisplay(int delta) {
        int currentCount = Integer.parseInt(likeCountTextView.getText().toString());
        likeCountTextView.setText(String.valueOf(currentCount + delta));
    }


}
