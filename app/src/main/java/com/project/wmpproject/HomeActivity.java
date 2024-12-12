package com.project.wmpproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.wmpproject.adapter.EventAdapter;
import com.project.wmpproject.model.Event;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView eventsRecyclerView;
    private FirebaseFirestore db;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView profileImage = findViewById(R.id.profileImage);
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton searchButton = findViewById(R.id.searchButton);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        db = FirebaseFirestore.getInstance();

        getEventsFromFirestore();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        // Set up RecyclerView
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(new ArrayList<>()); // Initialize with an empty list
        eventsRecyclerView.setAdapter(adapter);

    }
    private void getEventsFromFirestore() {
        db.collection("events").addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("HomeActivity", "Listen failed.", error);
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        events.add(event);
                    }
                    adapter.events = events;
                    adapter.notifyDataSetChanged();
                });
    }


}