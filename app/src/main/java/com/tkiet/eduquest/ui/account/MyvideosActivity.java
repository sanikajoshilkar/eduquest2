package com.tkiet.eduquest.ui.account;

import android.os.Bundle;
import android.view.View;
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
import com.tkiet.eduquest.R;
import com.tkiet.eduquest.ui.account.VideoModel;
import com.tkiet.eduquest.ui.account.VideoAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyvideosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoModel> videoList;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid(); // Get current user ID
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        recyclerView = findViewById(R.id.videosRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList);
        recyclerView.setAdapter(videoAdapter);

        fetchUserVideos();
    }

    private void fetchUserVideos() {
        databaseReference.orderByChild("addedBy").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        videoList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            VideoModel videoModel = snapshot.getValue(VideoModel.class);
                            if (videoModel != null) {
                                videoModel.setVideoId(snapshot.getKey());
                                videoList.add(videoModel);
                            }
                        }
                        videoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MyvideosActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
