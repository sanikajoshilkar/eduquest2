package com.tkiet.eduquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.tkiet.eduquest.ui.account.RegistrationActivity;
import android.content.SharedPreferences;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private TextView registerButton;
    private SwitchMaterial rememberMeSwitch;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private TextView forgotPasswordTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.input_email);
        passwordEditText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        registerButton = findViewById(R.id.registerTV);
        rememberMeSwitch = findViewById(R.id.login_rem_switch);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMainActivity();
        }

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Save login state if "Remember Me" is checked
                        if (rememberMeSwitch.isChecked()) {
                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();
                        }

                        navigateToMainActivity();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
    private void showForgotPasswordDialog() {
        // Create an AlertDialog builder
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        // Add an input field for the email
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your email");
        builder.setView(emailInput);

        // Set the positive button for sending the reset link
        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });

        // Set a cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
