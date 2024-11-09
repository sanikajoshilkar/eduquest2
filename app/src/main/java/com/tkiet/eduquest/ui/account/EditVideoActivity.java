package com.tkiet.eduquest.ui.account;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;

public class EditVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private EditText titleEditText, descriptionEditText, tagsEditText;
    private ImageView thumbnailImageView;
    private String videoId;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        // Initialize views
        videoView = findViewById(R.id.videoView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        tagsEditText = findViewById(R.id.tagsEditText);
        thumbnailImageView = findViewById(R.id.thumbnailImageView);

        // Get video ID from Intent
        videoId = getIntent().getStringExtra("videoId");

        if (videoId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("videos").child(videoId);
            loadVideoData();
        } else {
            Toast.makeText(this, "Video ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadVideoData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    VideoModel video = dataSnapshot.getValue(VideoModel.class);

                    if (video != null) {
                        // Set video URL to the VideoView
                        videoView.setVideoPath(video.getVideoUrl());
                        videoView.start();  // Start playing the video

                        // Set other fields
                        titleEditText.setText(video.getTitle());
                        descriptionEditText.setText(video.getDescription());
                        tagsEditText.setText(video.getTags());

                        // Load the thumbnail using Glide
                        Glide.with(EditVideoActivity.this)
                                .load(video.getThumbnailUrl())
                                .into(thumbnailImageView);
                    }
                } else {
                    Toast.makeText(EditVideoActivity.this, "Video data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditVideoActivity.this, "Failed to load video data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
