package com.tkiet.eduquest.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tkiet.eduquest.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddVideoActivity extends AppCompatActivity {

    private ImageView videoPreviewImage;
    private Uri videoUri;
    private FirebaseAuth auth;
    private StorageReference videoStorageRef;
    private DatabaseReference databaseRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        videoPreviewImage = findViewById(R.id.videoPreviewImage);
        auth = FirebaseAuth.getInstance();
        videoStorageRef = FirebaseStorage.getInstance().getReference("videos");
        databaseRef = FirebaseDatabase.getInstance().getReference("videos");

        findViewById(R.id.select_profile_photo_button).setOnClickListener(v -> selectVideo());
        findViewById(R.id.save_video).setOnClickListener(v -> uploadVideoWithThumbnail());
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            try {
                createVideoThumbnail(videoUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createVideoThumbnail(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            Bitmap thumbnail = retriever.getFrameAtTime(1000000); // 1 second into the video
            if (thumbnail != null) {
                videoPreviewImage.setImageBitmap(thumbnail);
            } else {
                Toast.makeText(this, "Failed to generate thumbnail", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error generating thumbnail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            retriever.release();
        }
    }

    private void uploadVideoWithThumbnail() {
        if (videoUri == null) {
            Toast.makeText(this, "Please select a video first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Video");
        progressDialog.setMessage("Please wait while the video is being uploaded...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String videoId = databaseRef.push().getKey();
        String userId = auth.getCurrentUser().getUid();
        StorageReference videoRef = videoStorageRef.child(videoId + ".mp4");
        StorageReference thumbnailRef = videoStorageRef.child(videoId + "_thumbnail.jpg");

        // Upload the video
        UploadTask videoUploadTask = videoRef.putFile(videoUri);
        videoUploadTask.addOnProgressListener(snapshot -> {
            int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
            progressDialog.setProgress(progress);
        }).addOnSuccessListener(videoTask -> {
            videoRef.getDownloadUrl().addOnSuccessListener(videoUrl -> {

                // Capture ImageView content as thumbnail
                videoPreviewImage.setDrawingCacheEnabled(true);
                Bitmap thumbnail = Bitmap.createBitmap(videoPreviewImage.getDrawingCache());
                videoPreviewImage.setDrawingCacheEnabled(false);

                // Convert bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] thumbnailData = baos.toByteArray();

                // Upload the captured thumbnail
                UploadTask thumbnailUploadTask = thumbnailRef.putBytes(thumbnailData);
                thumbnailUploadTask.addOnProgressListener(snapshot -> {
                    int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    progressDialog.setProgress(progress);
                }).addOnSuccessListener(thumbnailTask -> {
                    thumbnailRef.getDownloadUrl().addOnSuccessListener(thumbnailUrl -> {

                        // Save video and thumbnail URLs to database
                        Map<String, Object> videoData = new HashMap<>();
                        videoData.put("uid", videoId);
                        videoData.put("addedBy", userId);
                        videoData.put("title", ((TextInputEditText) findViewById(R.id.title)).getText().toString().trim());
                        videoData.put("description", ((TextInputEditText) findViewById(R.id.description)).getText().toString().trim());
                        videoData.put("tags", ((TextInputEditText) findViewById(R.id.tags)).getText().toString().trim());
                        videoData.put("videoUrl", videoUrl.toString());
                        videoData.put("thumbnailUrl", thumbnailUrl.toString());

                        databaseRef.child(videoId).setValue(videoData).addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddVideoActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(AddVideoActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        });

                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload thumbnail", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });

            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

}
