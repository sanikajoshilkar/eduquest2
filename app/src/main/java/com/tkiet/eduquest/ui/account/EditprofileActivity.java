package com.tkiet.eduquest.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tkiet.eduquest.R;

public class EditprofileActivity extends AppCompatActivity {

    private TextInputEditText editName, editSkills, editPhone;
    private ImageView profileImageView;
    private Button selectProfilePhotoButton;
    private MaterialButton btnSaveProfile;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        // Initialize Firebase references
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages").child(currentUser.getUid());

        // Initialize views
        editName = findViewById(R.id.edit_profile_name);
        editSkills = findViewById(R.id.edit_skills);
        editPhone = findViewById(R.id.edit_profile_phone);
        profileImageView = findViewById(R.id.profile_image_view);
        selectProfilePhotoButton = findViewById(R.id.select_profile_photo_button);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");

        // Load existing user data from Firebase
        loadUserProfile();

        // Set up profile photo selection
        selectProfilePhotoButton.setOnClickListener(v -> selectProfilePhoto());

        // Set up save changes button
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load existing data if present, otherwise set as blank
                String name = snapshot.child("name").getValue(String.class);
                String skills = snapshot.child("skills").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String profileImageUrl = snapshot.child("imageUrl").getValue(String.class);

                // Display the data in the text fields
                editName.setText(name != null ? name : "");
                editSkills.setText(skills != null ? skills : "");
                editPhone.setText(phone != null ? phone : "");

                // Load profile image using Glide
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(EditprofileActivity.this).load(profileImageUrl).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditprofileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectProfilePhoto() {
        // Open image selector
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        // Check that all fields are filled out
        String name = editName.getText().toString().trim();
        String skills = editSkills.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(skills) || TextUtils.isEmpty(phone) ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (imageUri != null) {
            // Save profile image to Firebase Storage
            storageReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Get the download URL and save other user data
                    storageReference.getDownloadUrl().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String profileImageUrl = task1.getResult().toString();
                            saveUserData(name, skills, phone, profileImageUrl);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(EditprofileActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(EditprofileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveUserData(name, skills, phone, null);
        }
    }

    private void saveUserData(String name, String skills, String phone, String profileImageUrl) {
        // Create a HashMap to save user details
        databaseReference.child("name").setValue(name);
        databaseReference.child("skills").setValue(skills);
        databaseReference.child("phone").setValue(phone);
        if (profileImageUrl != null) {
            databaseReference.child("imageUrl").setValue(profileImageUrl);
        }

        progressDialog.dismiss();
        Toast.makeText(EditprofileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
