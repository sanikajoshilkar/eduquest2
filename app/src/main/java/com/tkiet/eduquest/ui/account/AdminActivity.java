package com.tkiet.eduquest.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tkiet.eduquest.R;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements UnverifiedVideoAdapter.OnVideoActionListener {

    private RecyclerView recyclerView;
    private UnverifiedVideoAdapter adapter;
    private List<VideoModel> unverifiedVideos = new ArrayList<>();
    private DatabaseReference videosReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();
        videosReference = FirebaseDatabase.getInstance().getReference("videos");

        loadUnverifiedVideos();
    }

    private void loadUnverifiedVideos() {
        videosReference.orderByChild("isVerified").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unverifiedVideos.clear();
                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                    VideoModel video = videoSnapshot.getValue(VideoModel.class);
                    if (video != null) {
                        video.setVideoId(videoSnapshot.getKey());
                        unverifiedVideos.add(video);
                    }
                }
                adapter = new UnverifiedVideoAdapter(AdminActivity.this, unverifiedVideos, AdminActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                Log.e("AdminActivity", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onAccept(String videoId) {
        videosReference.child(videoId).child("isVerified").setValue(true);
        Toast.makeText(this, "Video Accepted", Toast.LENGTH_SHORT).show();
        loadUnverifiedVideos(); // Refresh List
    }

    @Override
    public void onReject(String videoId, String videoUrl) {
        StorageReference videoRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        videoRef.delete().addOnSuccessListener(aVoid -> {
            videosReference.child(videoId).removeValue();
            Toast.makeText(this, "Video Rejected and Deleted", Toast.LENGTH_SHORT).show();
            loadUnverifiedVideos(); // Refresh List
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete video", Toast.LENGTH_SHORT).show());
    }
}
