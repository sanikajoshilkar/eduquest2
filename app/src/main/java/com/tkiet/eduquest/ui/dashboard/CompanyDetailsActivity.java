package com.tkiet.eduquest.ui.dashboard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tkiet.eduquest.R;

import java.util.ArrayList;
import java.util.List;

public class CompanyDetailsActivity extends AppCompatActivity {

    private RecyclerView questionsRecyclerView;
    private List<Question> questionList;
    private QuestionAdapter questionAdapter;
    private DatabaseReference companiesReference;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);

        // Initialize toolbar and set title
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the navigation icon

        // Handle navigation icon (logout button) click
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });

        // Retrieve the company name and set it in the toolbar title
        String companyName = getIntent().getStringExtra("companyName");
        if (companyName != null) {
            toolbar.setTitle(companyName);
        } else {
            Toast.makeText(this, "Company not found", Toast.LENGTH_SHORT).show();
        }

        // Initialize RecyclerView
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        questionList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questionList, this);
        questionsRecyclerView.setAdapter(questionAdapter);

        // Firebase references
        companiesReference = FirebaseDatabase.getInstance().getReference("Companies");
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        // Load questions for the company
        if (companyName != null) {
            loadQuestions(companyName);
        }
    }


    private void loadQuestions(String companyName) {
        companiesReference.child(companyName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    String questionText = questionSnapshot.child("questions").getValue(String.class);
                    String addedByUid = questionSnapshot.child("addedBy").getValue(String.class);

                    if (questionText != null && addedByUid != null) {
                        fetchUserDetails(questionText, addedByUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CompanyDetailsActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails(String questionText, String addedByUid) {
        usersReference.child(addedByUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String userPhotoUrl = snapshot.child("imageUrl").getValue(String.class);

                questionList.add(new Question(questionText, userName, userPhotoUrl));
                questionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CompanyDetailsActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
