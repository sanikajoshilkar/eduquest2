package com.tkiet.eduquest.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ToggleButton;
public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewProfiles;
    private DatabaseReference usersRef;
    private List<UserProfile> profileList, filteredList;
    private ProfileAdapter profileAdapter;
    private ToggleButton toggleFilter;
    private EditText searchField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewProfiles = view.findViewById(R.id.recyclerViewProfiles);
        recyclerViewProfiles.setLayoutManager(new GridLayoutManager(getContext(), 2));


        profileList = new ArrayList<>();
        filteredList = new ArrayList<>();
        profileAdapter = new ProfileAdapter(filteredList, getContext());
        recyclerViewProfiles.setAdapter(profileAdapter);

        toggleFilter = view.findViewById(R.id.toggleFilter);
        searchField = view.findViewById(R.id.home_search_edit_text); // Add an EditText for search

        loadUserProfiles();

        // Add TextWatcher for search functionality
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterProfiles(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void loadUserProfiles() {
        // Get the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return; // Exit if the user is not logged in
        }

        String currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        if (!userSnapshot.getKey().equals(currentUserId)) {
                            UserProfile userProfile = userSnapshot.getValue(UserProfile.class);
                            if (userProfile != null) {
                                userProfile.setUserId(userSnapshot.getKey());
                                profileList.add(userProfile);
                            }
                        }
                    }
                    filterProfiles(searchField.getText().toString());
                } else {
                    Toast.makeText(getContext(), "No profiles available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profiles", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filterProfiles(String query) {
        filteredList.clear();
        boolean filterByProfile = toggleFilter.isChecked();

        for (UserProfile profile : profileList) {
            if (filterByProfile) {
                // Filter by profile name
                if (profile.getName() != null && profile.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(profile);
                }
            } else {
                // Filter by skills
                if (profile.getSkills() != null && profile.getSkills().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(profile);
                }
            }
        }

        profileAdapter.notifyDataSetChanged();
    }
}