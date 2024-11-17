package com.tkiet.eduquest.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tkiet.eduquest.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DashboardFragment extends Fragment {

    private DatabaseReference companyRef;
    private DatabaseReference userRef;
    private ArrayList<String> companySuggestions;
    private ArrayAdapter<String> companyAdapter;
    private ArrayList<String> companyNames;
    private CompanyAdapter recyclerAdapter;
    private AutoCompleteTextView autoCompleteSearch;
    private ArrayAdapter<String> autoCompleteAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firebase references
        companyRef = FirebaseDatabase.getInstance().getReference("Companies");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize RecyclerView
        RecyclerView companyRecyclerView = root.findViewById(R.id.companyRecyclerView);
        companyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        companyNames = new ArrayList<>();
        recyclerAdapter = new CompanyAdapter(companyNames, requireContext()); // Pass context as the second parameter
        companyRecyclerView.setAdapter(recyclerAdapter);

        // Initialize AutoCompleteTextView
        autoCompleteSearch = root.findViewById(R.id.autoCompleteSearch);
        autoCompleteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoCompleteSearch.setAdapter(autoCompleteAdapter);

// Add listener to handle search
        autoCompleteSearch.setOnItemClickListener((parent, view, position, id) -> filterCompanies(autoCompleteAdapter.getItem(position)));


        // Floating Action Button to open dialog
        root.findViewById(R.id.addCompanyFab).setOnClickListener(v -> openAddCompanyDialog());

        // Fetch and display company names
        fetchCompanyNames();

        return root;
    }

    private void fetchCompanyNames() {
        companyRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                companyNames.clear();
                ArrayList<String> suggestions = new ArrayList<>();

                for (DataSnapshot companySnapshot : snapshot.getChildren()) {
                    String companyName = companySnapshot.getKey();
                    if (companyName != null) {
                        companyNames.add(companyName);
                        suggestions.add(companyName);
                    }
                }

                // Update RecyclerView
                recyclerAdapter.notifyDataSetChanged();

                // Update AutoCompleteTextView suggestions
                autoCompleteAdapter.clear();
                autoCompleteAdapter.addAll(suggestions);
                autoCompleteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("DashboardFragment", "Failed to fetch company names: " + error.getMessage());
            }
        });
    }

    private void openAddCompanyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_company, null);
        builder.setView(dialogView);

        AutoCompleteTextView companyName = dialogView.findViewById(R.id.autoCompleteCompanyName);
        LinearLayout questionsLayout = dialogView.findViewById(R.id.questionsLayout);
        ImageView addQuestionBtn = dialogView.findViewById(R.id.addQuestionBtn);

        // Populate AutoComplete suggestions
        fetchCompanySuggestions();
        companyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, companySuggestions);
        companyName.setAdapter(companyAdapter);

        // Add new EditText for questions
        addQuestionBtn.setOnClickListener(v -> addNewQuestionField(questionsLayout));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String company = companyName.getText().toString().trim();
            if (TextUtils.isEmpty(company)) {
                Toast.makeText(requireContext(), "Company name is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> questions = new ArrayList<>();
            for (int i = 0; i < questionsLayout.getChildCount(); i++) {
                EditText questionField = (EditText) questionsLayout.getChildAt(i);
                String question = questionField.getText().toString().trim();
                if (!TextUtils.isEmpty(question)) {
                    questions.add(question);
                }
            }

            if (questions.isEmpty()) {
                Toast.makeText(requireContext(), "At least one question is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveCompanyQuestions(company, questions);
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void addNewQuestionField(LinearLayout questionsLayout) {
        EditText newQuestionField = new EditText(requireContext());
        newQuestionField.setHint("Enter question");
        newQuestionField.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        questionsLayout.addView(newQuestionField);
    }

    private void fetchCompanySuggestions() {
        companySuggestions = new ArrayList<>();
        companyRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot companySnapshot : snapshot.getChildren()) {
                    companySuggestions.add(companySnapshot.getKey());
                }
                if (companyAdapter != null) {
                    companyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e("DashboardFragment", "Failed to fetch company suggestions: " + error.getMessage());
            }
        });
    }



    private void saveCompanyQuestions(String company, ArrayList<String> questions) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String questionsString = TextUtils.join("\n", questions);

        HashMap<String, Object> data = new HashMap<>();
        data.put("addedBy", currentUserId);
        data.put("questions", questionsString);

        companyRef.child(company).push().setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "Questions added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to add questions.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void filterCompanies(String query) {
        if (TextUtils.isEmpty(query)) {
            recyclerAdapter.updateData(companyNames);
        } else {
            ArrayList<String> filteredList = new ArrayList<>();
            for (String companyName : companyNames) {
                if (companyName.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(companyName);
                }
            }
            recyclerAdapter.updateData(filteredList);
        }
    }
}
