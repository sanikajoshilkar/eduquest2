package com.tkiet.eduquest.ui.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tkiet.eduquest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private RecyclerView companiesRecyclerView;
    private FloatingActionButton addFab;
    private DatabaseReference companiesReference;
    private List<String> companyList;
    private CompanyAdapter companyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        companiesRecyclerView = view.findViewById(R.id.companies_recycler_view);
        addFab = view.findViewById(R.id.fab_add_company);

        companiesReference = FirebaseDatabase.getInstance().getReference("Companies");

        companyList = new ArrayList<>();
        companyAdapter = new CompanyAdapter(companyList, getContext());

        companiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        companiesRecyclerView.setAdapter(companyAdapter);

        // Load Companies
        loadCompanies();

        // FAB Click Listener
        addFab.setOnClickListener(v -> showAddCompanyDialog());

        return view;
    }

    private void loadCompanies() {
        companiesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                companyList.clear();
                for (DataSnapshot companySnapshot : snapshot.getChildren()) {
                    companyList.add(companySnapshot.getKey());
                }
                companyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load companies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCompanyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Company and Question");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_company, null, false);
        builder.setView(dialogView);

        EditText companyNameEditText = dialogView.findViewById(R.id.edit_text_company_name);
        EditText questionEditText = dialogView.findViewById(R.id.edit_text_question);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String companyName = companyNameEditText.getText().toString().trim();
            String question = questionEditText.getText().toString().trim();

            if (TextUtils.isEmpty(companyName) || TextUtils.isEmpty(question)) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            addQuestionToDatabase(companyName, question);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void addQuestionToDatabase(String companyName, String question) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String questionId = companiesReference.child(companyName).push().getKey();
        if (questionId != null) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", question);
            questionData.put("addedBy", uid);

            companiesReference.child(companyName).child(questionId).setValue(questionData)
                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Question added", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add question", Toast.LENGTH_SHORT).show());
        }
    }
}
