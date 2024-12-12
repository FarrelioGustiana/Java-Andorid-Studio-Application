package com.project.wmpproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView profileImage;
    private TextInputEditText usernameEditText, emailEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Picasso.get().load(uri).into(profileImage);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        profileImage = findViewById(R.id.profileImage);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        MaterialButton saveButton = findViewById(R.id.saveButton);
        MaterialButton signOutButton = findViewById(R.id.signOutButton);

        loadUserProfile();

        profileImage.setOnClickListener(v -> pickImage.launch("image/*"));

        saveButton.setOnClickListener(v -> saveProfile());

        signOutButton.setOnClickListener(v -> signOut());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                    usernameEditText.setText(username);
                    emailEditText.setText(email);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_default_profile);
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveProfile() {
        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", username);
            updates.put("email", email);

            if (selectedImageUri != null) {
                StorageReference imageRef = storageRef.child("profile_images/" + userId + ".jpg");
                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    updates.put("profileImageUrl", uri.toString());
                                    updateFirestore(userRef, updates);
                                }))
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
            } else {
                updateFirestore(userRef, updates);
            }
        }
    }

    private void updateFirestore(DocumentReference userRef, Map<String, Object> updates) {
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    loadUserProfile(); // Reload the profile to reflect changes
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, AuthActivity.class));
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity and return to the login screen
    }
}

