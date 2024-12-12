package com.project.wmpproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

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

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private EventAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(new ArrayList<>());
        searchResultsRecyclerView.setAdapter(adapter);


        db = FirebaseFirestore.getInstance();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchQuery = s.toString().trim();
                searchEvents(searchQuery);
            }
        });

    }

    private void searchEvents(String searchQuery) {
        if (searchQuery.isEmpty()) {
            adapter.events.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("events").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w("Search Activity", "Listen failed.", error);
                return;
            }

            List<Event> events = new ArrayList<>();

            for (QueryDocumentSnapshot document : value) {
                Event event = document.toObject(Event.class);
                event.setEventId(document.getId());
                if (event.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(searchQuery.toLowerCase())) {
                    events.add(event);
                }

                adapter.events = events;
                adapter.notifyDataSetChanged();

            }

        });

    }
}