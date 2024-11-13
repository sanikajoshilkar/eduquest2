package com.tkiet.eduquest;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;
import com.tkiet.eduquest.ui.account.*;
import com.tkiet.eduquest.ui.account.VideoAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProfileDetailActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView, phoneTextView, skillsTextView;
    private RecyclerView videosRecyclerView;
    private VideoAdapter videosAdapter;
    private List<VideoModel> uploadedVideos;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
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
                    phoneTextView.setText(phone);
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
                    uploadedVideos.add(video);
                }
                // Pass context and video list to VideoAdapter
                videosAdapter = new VideoAdapter(ProfileDetailActivity.this, uploadedVideos);
                videosRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileDetailActivity.this));
                videosRecyclerView.setAdapter(videosAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileDetailActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
