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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditprofileActivity extends AppCompatActivity {

    private TextInputEditText editName, skillsInput;
    private ImageView profileImageView;
    private Button selectProfilePhotoButton, addSkillButton;
    private MaterialButton btnSaveProfile;
    private RecyclerView skillsRecyclerView;

    private SkillsAdapter skillsAdapter;
    private List<String> skillsList = new ArrayList<>();

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private Uri imageUri;
    private ProgressDialog progressDialog;
    private TextInputEditText certificationsInput;
    private Button addCertificationButton;
    private RecyclerView certificationsRecyclerView;
    private List<String> certificationsList = new ArrayList<>();
    private CertificationsAdapter certificationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        // Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages").child(currentUser.getUid());

        // Initialize views
        editName = findViewById(R.id.edit_profile_name);
        skillsInput = findViewById(R.id.skills_input);
        profileImageView = findViewById(R.id.profile_image_view);
        selectProfilePhotoButton = findViewById(R.id.select_profile_photo_button);
        addSkillButton = findViewById(R.id.add_skill_button);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        skillsRecyclerView = findViewById(R.id.skills_recycler_view);

        // Initialize certification views
        certificationsInput = findViewById(R.id.certifications_input);
        addCertificationButton = findViewById(R.id.add_certification_button);
        certificationsRecyclerView = findViewById(R.id.certifications_recycler_view);

// Initialize RecyclerView for certifications
        certificationsAdapter = new CertificationsAdapter(certificationsList, this::removeCertification);
        certificationsRecyclerView.setAdapter(certificationsAdapter);
        certificationsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        // Initialize RecyclerView
        skillsAdapter = new SkillsAdapter(skillsList, this::removeSkill);
        skillsRecyclerView.setAdapter(skillsAdapter);
        skillsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");

        // Load user profile
        loadUserProfile();

        // Add skill button logic
        addSkillButton.setOnClickListener(v -> {
            String skill = skillsInput.getText().toString().trim();
            if (!TextUtils.isEmpty(skill)) {
                skillsList.add(skill);
                skillsAdapter.notifyDataSetChanged();
                skillsInput.setText("");
            } else {
                Toast.makeText(this, "Enter a skill", Toast.LENGTH_SHORT).show();
            }
        });
        addCertificationButton.setOnClickListener(v -> {
            String certification = certificationsInput.getText().toString().trim();
            if (!TextUtils.isEmpty(certification)) {
                certificationsList.add(certification);
                certificationsAdapter.notifyDataSetChanged();
                certificationsInput.setText("");
            } else {
                Toast.makeText(this, "Enter a certification", Toast.LENGTH_SHORT).show();
            }
        });

        // Save profile changes
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("imageUrl").getValue(String.class);
                String skills = snapshot.child("skills").getValue(String.class);

                editName.setText(name != null ? name : "");
                String certifications = snapshot.child("certifications").getValue(String.class);

// Populate certifications list
                if (certifications != null) {
                    certificationsList.clear();
                    certificationsList.addAll(Arrays.asList(certifications.split(",")));
                    certificationsAdapter.notifyDataSetChanged();
                }


                // Populate skills list
                if (skills != null) {
                    skillsList.clear();
                    skillsList.addAll(Arrays.asList(skills.split(",")));
                    skillsAdapter.notifyDataSetChanged();
                }

                if (profileImageUrl != null) {
                    Glide.with(EditprofileActivity.this).load(profileImageUrl).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditprofileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String name = editName.getText().toString().trim();
        String skills = String.join(",", skillsList);
        if (TextUtils.isEmpty(name) ) {
            Toast.makeText(this, "Name  is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String certifications = String.join(",", certificationsList);
        databaseReference.child("certifications").setValue(certifications).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditprofileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditprofileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });

        progressDialog.show();

        databaseReference.child("name").setValue(name);
        databaseReference.child("skills").setValue(skills).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(EditprofileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditprofileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeSkill(String skill) {
        skillsList.remove(skill);
        skillsAdapter.notifyDataSetChanged();
    }
    private void removeCertification(String certification) {
        certificationsList.remove(certification);
        certificationsAdapter.notifyDataSetChanged();
    }

}
