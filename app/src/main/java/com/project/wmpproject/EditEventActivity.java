package com.project.wmpproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.wmpproject.model.Event;
import com.squareup.picasso.Picasso;

public class EditEventActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, dateTimeEditText, locationEditText, imageUrlEditText;
    private ImageView eventImageView;
    private Button saveChangesButton;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        titleEditText = findViewById(R.id.editEventTitle);
        descriptionEditText = findViewById(R.id.editEventDescription);
        dateTimeEditText = findViewById(R.id.editEventDateTime);
        locationEditText = findViewById(R.id.editEventLocation);
        imageUrlEditText = findViewById(R.id.editEventImageUrl);
        eventImageView = findViewById(R.id.editEventImage);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        fetchEventData(eventId);

        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void fetchEventData(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Event event = task.getResult().toObject(Event.class);
                        if (event != null) {
                            populateEventFields(event);
                        } else {
                            // Handle the case where event data is not found
                            Log.d("EditEventActivity", "Event not found");
                            finish(); // Close the activity
                        }
                    } else {
                        Log.w("EditEventActivity", "Error fetching event data", task.getException());
                    }
                });
    }

    private void populateEventFields(Event event) {
        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());
        dateTimeEditText.setText(event.getDateTime());
        locationEditText.setText(event.getLocation());
        imageUrlEditText.setText(event.getImageUrl());
        Picasso.get().load(event.getImageUrl()).into(eventImageView);
    }

    private void saveChanges() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String dateTime = dateTimeEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String imageUrl = imageUrlEditText.getText().toString();

        Event updatedEvent = new Event(title, description, dateTime, location, imageUrl, eventId);

        db.collection("events").document(eventId)
                .set(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Log.w("EditEventActivity", "Error updating event", e));
    }
}