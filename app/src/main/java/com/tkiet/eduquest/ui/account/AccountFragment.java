package com.tkiet.eduquest.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.LoginActivity;
import com.tkiet.eduquest.R;

public class AccountFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ImageView profileImageView, likeIcon;
    private TextView profileName, likeCount;
    private CardView editProfile, myVideos, addVideo, signOut;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadUserProfile();
        }

        // Initialize views
        profileImageView = view.findViewById(R.id.imageView);
        profileName = view.findViewById(R.id.profile_name);
      //  likeIcon = view.findViewById(R.id.like_icon);
       // likeCount = view.findViewById(R.id.like_count);
        editProfile = view.findViewById(R.id.account_profile_tv);
        myVideos = view.findViewById(R.id.my_videos);
        addVideo = view.findViewById(R.id.add_video);
        signOut = view.findViewById(R.id.account_sign_out);

        // Set up button click listeners
        setButtonListeners();
    }

    private void loadUserProfile() {
        // Retrieve user's name and profile image from Firebase Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Assuming "name" and "profileImageUrl" are keys in your database
                    String name = snapshot.child("name").getValue(String.class);
                    String profileImageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Set name
                    profileName.setText(name);

                    // Load image using Glide
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext()).load(profileImageUrl).into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.user_image); // Default image
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonListeners() {
        // Set up a single click listener for all card views
        View.OnClickListener listener = v -> {
            Intent intent = null;

            // Use if-else statements instead of switch for view IDs
            if (v.getId() == R.id.account_profile_tv) {
                intent = new Intent(getActivity(), EditprofileActivity.class);
            } else if (v.getId() == R.id.my_videos) {
                intent = new Intent(getActivity(), MyvideosActivity.class);
            } else if (v.getId() == R.id.add_video) {
                intent = new Intent(getActivity(), AddVideoActivity.class);
            } else if (v.getId() == R.id.account_sign_out) {
                auth.signOut();
                requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLoggedIn", false)
                        .apply();
                Toast.makeText(getContext(), "Signed Out", Toast.LENGTH_SHORT).show();
                intent = new Intent(getActivity(), LoginActivity.class);
                requireActivity().finish();
            }

            // Start the activity if intent is set
            if (intent != null) {
                startActivity(intent);
            }
        };

        // Assign the listener to each card view
        editProfile.setOnClickListener(listener);
        myVideos.setOnClickListener(listener);
        addVideo.setOnClickListener(listener);
        signOut.setOnClickListener(listener);
    }
}
