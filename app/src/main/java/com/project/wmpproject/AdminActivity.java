package com.project.wmpproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.wmpproject.adapter.AdminEventAdapter;
import com.project.wmpproject.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView eventsRecyclerView;
    private AdminEventAdapter eventAdapter; // Using the admin adapter
    private List<Event> eventList;
    private FirebaseFirestore db;
    private FloatingActionButton addEventButton;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        addEventButton = findViewById(R.id.addEventButton);
        signOutButton = findViewById(R.id.signOutButton); // Initialize signOutButton

        eventList = new ArrayList<>();
        eventAdapter = new AdminEventAdapter(eventList); // Initialize the admin adapter

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);

        fetchEvents();

        addEventButton.setOnClickListener(view -> startActivity(new Intent(AdminActivity.this, AddEventActivity.class)));

        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminActivity.this, MainActivity.class));
            finish();
        });
    }

    private void fetchEvents() {
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Event event = document.toObject(Event.class);
                                event.setEventId(document.getId());
                                eventList.add(event);
                            }
                            eventAdapter.notifyDataSetChanged();
                        } else {
                            Log.w("AdminActivity", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}