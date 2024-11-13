package com.tkiet.eduquest.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewProfiles;
    private DatabaseReference usersRef;
    private List<UserProfile> profileList;
    private ProfileAdapter profileAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewProfiles = view.findViewById(R.id.recyclerViewProfiles);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the profile list and adapter
        profileList = new ArrayList<>();
        profileAdapter = new ProfileAdapter(profileList, getContext());
        recyclerViewProfiles.setAdapter(profileAdapter);

        loadUserProfiles();

        return view;
    }

    private void loadUserProfiles() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("HomeFragment", "data found.");
                    profileList.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Avoid adding current user's profile
                        if (!userSnapshot.getKey().equals(currentUserId)) {
                            UserProfile userProfile = userSnapshot.getValue(UserProfile.class);
                            if (userProfile != null) {
                                userProfile.setUserId(userSnapshot.getKey());
                                profileList.add(userProfile);
                            }
                        }
                    }
                    Log.d("HomeFragment", "Profile list size: " + profileList.size());
                    profileAdapter.notifyDataSetChanged();
                } else {
                    Log.d("HomeFragment", "No data found.");
                    Toast.makeText(getContext(), "No profiles available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profiles", Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error: " + error.getMessage());
            }
        });
    }
}
