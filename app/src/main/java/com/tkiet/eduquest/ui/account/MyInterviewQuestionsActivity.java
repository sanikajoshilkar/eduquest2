package com.tkiet.eduquest.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class MyInterviewQuestionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyQuestionsAdapter adapter;
    private DatabaseReference databaseReference;
    private String currentUserId; // Your authenticated user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_interview_questions);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Companies");
        // Initialize toolbar and set title
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the navigation icon

        // Handle navigation icon (logout button) click
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });
        loadUserQuestions();
    }

    private void loadUserQuestions() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuestionItem> questionsList = new ArrayList<>();
                for (DataSnapshot companySnapshot : snapshot.getChildren()) {
                    String companyName = companySnapshot.getKey();
                    for (DataSnapshot questionSnapshot : companySnapshot.getChildren()) {
                        String addedBy = questionSnapshot.child("addedBy").getValue(String.class);
                        if (addedBy != null && addedBy.equals(currentUserId)) {
                            String question = questionSnapshot.child("questions").getValue(String.class);
                            String questionKey = questionSnapshot.getKey();
                            questionsList.add(new QuestionItem(companyName, questionKey, question));
                        }
                    }
                }
                adapter = new MyQuestionsAdapter(questionsList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyInterviewQuestionsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteQuestion(String companyName, String questionKey) {
        DatabaseReference questionRef = databaseReference.child(companyName).child(questionKey);
        questionRef.removeValue().addOnSuccessListener(aVoid -> {
            // Check if the company has no more questions
            databaseReference.child(companyName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Delete company node
                        databaseReference.child(companyName).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            Toast.makeText(MyInterviewQuestionsActivity.this, "Question deleted", Toast.LENGTH_SHORT).show();
            loadUserQuestions(); // Refresh the list
        });
    }

    class MyQuestionsAdapter extends RecyclerView.Adapter<MyQuestionsAdapter.ViewHolder> {
        private final List<QuestionItem> questions;

        MyQuestionsAdapter(List<QuestionItem> questions) {
            this.questions = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interview_question, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuestionItem questionItem = questions.get(position);
            holder.companyNameTextView.setText(questionItem.companyName);
            holder.questionTextView.setText(questionItem.question);

            holder.deleteImageView.setOnClickListener(v -> deleteQuestion(questionItem.companyName, questionItem.questionKey));
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView companyNameTextView, questionTextView;
            ImageView deleteImageView;

            ViewHolder(View itemView) {
                super(itemView);
                companyNameTextView = itemView.findViewById(R.id.companyNameTextView);
                questionTextView = itemView.findViewById(R.id.questionTextView);
                deleteImageView = itemView.findViewById(R.id.deleteImageView);
            }
        }
    }

    static class QuestionItem {
        String companyName, questionKey, question;

        QuestionItem(String companyName, String questionKey, String question) {
            this.companyName = companyName;
            this.questionKey = questionKey;
            this.question = question;
        }
    }
}
