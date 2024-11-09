package com.tkiet.eduquest.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private EditText titleEditText, descriptionEditText, tagsEditText;
    private ImageView thumbnailImageView;
    private Button saveButton, changeThumbnailButton, playPauseButton;
    private SeekBar videoSeekBar;
    private String videoId;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private boolean isPlaying = false;
    private Uri newThumbnailUri;
    private String videoUrl;  // Declare videoUrl variable to store the video URL

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
        saveButton = findViewById(R.id.saveButton);
        changeThumbnailButton = findViewById(R.id.changeThumbnailButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        videoSeekBar = findViewById(R.id.videoSeekBar);

        // Get video ID from Intent
        videoId = getIntent().getStringExtra("videoId");

        if (videoId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("videos").child(videoId);
            storageReference = FirebaseStorage.getInstance().getReference();
            loadVideoData();
        } else {
            Toast.makeText(this, "Video ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Play/Pause button logic
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                videoView.pause();
                playPauseButton.setText("Play");
            } else {
                videoView.start();
                playPauseButton.setText("Pause");
            }
            isPlaying = !isPlaying;
        });

        // Change thumbnail button logic
        changeThumbnailButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        // Save Button logic to save the data
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String tags = tagsEditText.getText().toString();

            if (!title.isEmpty() && !description.isEmpty()) {
                saveVideoData(title, description, tags);
            } else {
                Toast.makeText(this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to load video data
    private void loadVideoData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    VideoModel video = dataSnapshot.getValue(VideoModel.class);

                    if (video != null) {
                        // Set video URL to the VideoView
                        videoUrl = video.getVideoUrl();  // Assign the video URL to the videoUrl variable
                        videoView.setVideoPath(videoUrl);
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

    // Method to save updated video data
    private void saveVideoData(String title, String description, String tags) {
        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a new VideoModel object and set the fields
        VideoModel video = new VideoModel(title, description, tags, videoUrl, currentUserId);  // Pass addedBy as the currentUserId

        // If a new thumbnail is selected, upload it to Firebase Storage
        if (newThumbnailUri != null) {
            String thumbnailPath = "thumbnails/" + videoId + ".jpg";
            storageReference.child(thumbnailPath).putFile(newThumbnailUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageReference.child(thumbnailPath).getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    video.setThumbnailUrl(uri.toString()); // Set the new thumbnail URL
                                    updateVideoInDatabase(video); // Update the video in Firebase
                                });
                    });
        } else {
            updateVideoInDatabase(video); // If no new thumbnail, directly update video
        }
    }

    private void updateVideoInDatabase(VideoModel video) {
        // Update the video in the Firebase Realtime Database
        databaseReference.setValue(video).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditVideoActivity.this, "Video updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after success
            } else {
                Toast.makeText(EditVideoActivity.this, "Failed to update video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle result from image picker (Change Thumbnail)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri selectedImage = data.getData();
            thumbnailImageView.setImageURI(selectedImage);
            newThumbnailUri = selectedImage;
        }
    }
}
