package com.project.wmpproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AuthActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button registerToggleButton, loginToggleButton, authButton;
    private boolean isLoginMode = false;
    private LinearLayout usernameField;
    private TextView forgotPasswordTextView;
    private CheckBox termsCheckBox;
    private Button btnSubmit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize XML Elements by it's own ID
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerToggleButton = findViewById(R.id.registerToggleButton);
        loginToggleButton = findViewById(R.id.loginToggleButton);
        usernameField = findViewById(R.id.usernameField);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        registerToggleButton.setOnClickListener(v -> setAuthMode(false));
        loginToggleButton.setOnClickListener(v -> setAuthMode(true));

        btnSubmit.setOnClickListener(v -> handleSubmit());

        // Set the auth into login mode as the default mode
        setAuthMode(true);

    }

    private void setAuthMode(boolean loginMode) {
        isLoginMode = loginMode;

        if (isLoginMode) {
            registerToggleButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            loginToggleButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            usernameField.setVisibility(View.GONE);
            forgotPasswordTextView.setVisibility(View.VISIBLE);
            termsCheckBox.setVisibility(View.GONE);
            btnSubmit.setText("Login");
        } else {
            registerToggleButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            loginToggleButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            usernameField.setVisibility(View.VISIBLE);
            forgotPasswordTextView.setVisibility(View.GONE);
            termsCheckBox.setVisibility(View.VISIBLE);
            btnSubmit.setText("Register");
        }
    }

    private void handleSubmit() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLoginMode) {
            String username = usernameEditText.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!termsCheckBox.isChecked()) {
                Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                return;
            }
            registerUser(email, password, username.trim());
        } else {
            userLogin(email, password);
        }
    }

    private void registerUser(String email, String password, String username) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // If Succeed
                    if (task.isSuccessful()) {
                        // Get the newly created user ID
                        String userId = task.getResult().getUser().getUid();

                        // Create a user data map
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("email", email);

                        // Save the user data in Firestore
                        db.collection("users").document(userId).set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AuthActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AuthActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        // If failed
                        Toast.makeText(AuthActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void userLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        String userId = task.getResult().getUser().getUid();
                        checkUserRole(userId); // Check the user's role
                    } else {
                        // Login failed
                        Toast.makeText(AuthActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // User data found
                            String role = task.getResult().getString("role");
                            if ("admin".equals(role)) {
                                // Go to AdminActivity
                                startActivity(new Intent(AuthActivity.this, AdminActivity.class));
                            } else {
                                // Go to HomeActivity (or handle other roles)
                                startActivity(new Intent(AuthActivity.this, HomeActivity.class));
                            }
                            finish();
                        } else {
                            // User data not found (handle this case appropriately)
                            Toast.makeText(AuthActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error fetching user data
                        Toast.makeText(AuthActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}