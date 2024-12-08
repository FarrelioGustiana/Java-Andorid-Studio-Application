package com.project.wmpproject;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class Auth extends AppCompatActivity {

    private Button registerToggleButton, loginToggleButton, authButton;
    private boolean isLoginMode = false;
    private LinearLayout usernameField;
    private TextView forgotPasswordTextView;
    private CheckBox termsCheckBox;
    private Button btnSubmit;

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

        registerToggleButton = findViewById(R.id.registerToggleButton);
        loginToggleButton = findViewById(R.id.loginToggleButton);
        usernameField = findViewById(R.id.usernameField);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        btnSubmit = findViewById(R.id.btnSubmit);


        registerToggleButton.setOnClickListener(v -> setAuthMode(false));
        loginToggleButton.setOnClickListener(v -> setAuthMode(true));

        setAuthMode(isLoginMode);

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
}