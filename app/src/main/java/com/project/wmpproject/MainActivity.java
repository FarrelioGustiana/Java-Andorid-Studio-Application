package com.project.wmpproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            checkUserRole();
        }

        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Auth.class);
            startActivity(intent);
        });
    }

    private void checkUserRole() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                startActivity(new Intent(this, AdminActivity.class));
                            } else {
                                startActivity(new Intent(this, HomeActivity.class));
                            }
                            finish();
                        } else {
                            // Handle the case where the user document doesn't exist
                            Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the error
                        Toast.makeText(this, "Error checking user role", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

