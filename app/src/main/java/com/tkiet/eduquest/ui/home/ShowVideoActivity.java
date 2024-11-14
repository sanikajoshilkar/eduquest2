package com.tkiet.eduquest.ui.home;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;
import com.tkiet.eduquest.classes.Video;

public class ShowVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView titleTextView, descriptionTextView, tagsTextView;
    private DatabaseReference videoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        videoView = findViewById(R.id.videoView);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        tagsTextView = findViewById(R.id.tagsTextView);

        // Get the video ID from Intent
        String videoId = getIntent().getStringExtra("VIDEO_ID");
        if (videoId == null) {
            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase reference to video data
        videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoId);

        // Load video details from Firebase
        loadVideoData();

        // Setup media controls for video playback
        setupMediaController();
    }

    private void loadVideoData() {
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Video video = snapshot.getValue(Video.class);
                if (video != null) {
                    titleTextView.setText(video.getTitle() != null ? video.getTitle() : "Title not available");
                    descriptionTextView.setText(video.getDescription() != null ? video.getDescription() : "Description not available");
                    tagsTextView.setText(video.getTags() != null ? video.getTags() : "Tags not available");

                    // Set the video URI for playback
                    Uri videoUri = Uri.parse(video.getVideoUrl());
                    videoView.setVideoURI(videoUri);
                } else {
                    Toast.makeText(ShowVideoActivity.this, "Video not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowVideoActivity.this, "Failed to load video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMediaController() {
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> videoView.start()); // Auto-play when video is loaded

        videoView.setOnCompletionListener(mp ->
                Toast.makeText(ShowVideoActivity.this, "Playback completed", Toast.LENGTH_SHORT).show());
    }

}
