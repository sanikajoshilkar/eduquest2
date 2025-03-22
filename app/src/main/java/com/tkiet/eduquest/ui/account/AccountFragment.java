package com.tkiet.eduquest.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private DatabaseReference databaseReference, likesReference;
    private ImageView profileImageView;
    private TextView profileName, likeCount;
    private CardView editProfile, myVideos, addVideo, signOut, interviewQuestions, adminOption;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Initialize views
        profileImageView = view.findViewById(R.id.imageView);
        profileName = view.findViewById(R.id.profile_name);
        likeCount = view.findViewById(R.id.like_count);
        editProfile = view.findViewById(R.id.account_profile_tv);
        myVideos = view.findViewById(R.id.my_videos);
        addVideo = view.findViewById(R.id.add_video);
        signOut = view.findViewById(R.id.account_sign_out);
        interviewQuestions = view.findViewById(R.id.myinterviewquestions);
        adminOption = view.findViewById(R.id.admin_activity);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            likesReference = FirebaseDatabase.getInstance().getReference("Likes").child(userId);

            loadUserProfile();
            loadLikeCount();
            checkIfAdmin(userId);
        }

        setButtonListeners();
    }

    private void loadUserProfile() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileImageUrl = snapshot.child("imageUrl").getValue(String.class);

                    profileName.setText(name);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext()).load(profileImageUrl).into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.user_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLikeCount() {
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer likeCountValue = snapshot.child("likeCount").getValue(Integer.class);
                    likeCount.setText(likeCountValue != null ? String.valueOf(likeCountValue) : "0");
                } else {
                    likeCount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load like count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAdmin(String userId) {
        DatabaseReference adminReference = FirebaseDatabase.getInstance().getReference("Admin");

        adminReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userId)) {
                    adminOption.setVisibility(View.VISIBLE);
                    adminOption.setOnClickListener(v -> {
                        Log.d("AccountFragment", "Admin Card Clicked");
                        startActivity(new Intent(getActivity(), AdminActivity.class));
                    });
                } else {
                    adminOption.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check admin status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonListeners() {
        View.OnClickListener listener = v -> {
            Intent intent = null;

            if (v.getId() == R.id.account_profile_tv) {
                intent = new Intent(getActivity(), AdminActivity.class);
            } else if (v.getId() == R.id.my_videos) {
                intent = new Intent(getActivity(), MyvideosActivity.class);
            } else if (v.getId() == R.id.add_video) {
                intent = new Intent(getActivity(), AddVideoActivity.class);
            } else if (v.getId() == R.id.myinterviewquestions) {
                intent = new Intent(getActivity(), MyInterviewQuestionsActivity.class);
            } else if (v.getId() == R.id.account_sign_out) {
                auth.signOut();
                requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLoggedIn", false)
                        .apply();
                Toast.makeText(getContext(), "Signed Out", Toast.LENGTH_SHORT).show();
                intent = new Intent(getActivity(), LoginActivity.class);
                requireActivity().finish();
            } else if (v.getId() == R.id.admin_activity) {
                intent = new Intent(getActivity(), AdminActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
            }
        };

        editProfile.setOnClickListener(listener);
        myVideos.setOnClickListener(listener);
        addVideo.setOnClickListener(listener);
        signOut.setOnClickListener(listener);
        interviewQuestions.setOnClickListener(listener);
        adminOption.setOnClickListener(listener);
    }
}
