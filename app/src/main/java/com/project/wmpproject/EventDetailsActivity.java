package com.project.wmpproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.wmpproject.adapter.AttendanceAdapter;
import com.project.wmpproject.model.Attendance;
import com.project.wmpproject.model.Event;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    private ImageView eventImage;
    private TextView eventTitle, eventDescription, eventDateTime, eventLocation;
    private Button checkInButton;
    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        eventImage = findViewById(R.id.eventImage);
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventDateTime = findViewById(R.id.eventDateTime);
        eventLocation = findViewById(R.id.eventLocation);
        checkInButton = findViewById(R.id.checkInButton);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            fetchEventData(eventId);
        } else {
            // Handle the case where eventId is null
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkInButton.setOnClickListener(v -> checkInToEvent());
    }

    private void fetchEventData(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        event = task.getResult().toObject(Event.class);
                        if (event != null) {
                            displayEventData(event);
                            fetchAttendanceData(eventId);
                        } else {
                            // Handle the case where event data is not found
                            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.w("EventDetailsActivity", "Error fetching event data", task.getException());
                        Toast.makeText(this, "Error fetching event data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayEventData(Event event) {
        Picasso.get().load(event.getImageUrl()).into(eventImage);
        eventTitle.setText(event.getTitle());
        eventDescription.setText(event.getDescription());
        eventDateTime.setText(event.getDateTime());
        eventLocation.setText(event.getLocation());
    }

    private void checkInToEvent() {
        if (event == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Check if the user has already checked in
        db.collection("events").document(eventId).collection("attendance")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // User has not checked in, proceed with check-in
                            performCheckIn(userId);
                        } else {
                            // User has already checked in
                            Toast.makeText(this, "You have already checked in to this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("EventDetailsActivity", "Error checking attendance", task.getException());
                        Toast.makeText(this, "Error checking attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performCheckIn(String userId) {
        String dateTime = event.getDateTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(dateTime);
            Date currentDate = new Date();

            if (currentDate.before(eventDate)) {
                Toast.makeText(this, "You can only check in after the scheduled time.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the subcollection reference
            CollectionReference attendanceRef = db.collection("events").document(eventId).collection("attendance");

            Map<String, Object> attendanceData = new HashMap<>();
            attendanceData.put("userId", userId);
            attendanceData.put("checkinTime", FieldValue.serverTimestamp());

            // Add a new document to the subcollection
            attendanceRef.add(attendanceData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(EventDetailsActivity.this, "Checked in successfully!", Toast.LENGTH_SHORT).show();
                        fetchAttendanceData(eventId); // Refresh attendance list
                    })
                    .addOnFailureListener(e -> {
                        Log.w("EventDetailsActivity", "Error checking in", e);
                        Toast.makeText(EventDetailsActivity.this, "Error checking in", Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException e) {
            Log.e("EventDetailsActivity", "Error parsing date", e);
            Toast.makeText(this, "Error parsing date", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchAttendanceData(String eventId) {
        db.collection("events").document(eventId).collection("attendance")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Attendance> attendanceData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("userId");
                            Timestamp checkinTimestamp = document.getTimestamp("checkinTime");
                            Timestamp checkinTime = checkinTimestamp != null ? checkinTimestamp : null;
                            attendanceData.add(new Attendance(userId, checkinTime));
                        }
                        populateAttendanceList(attendanceData);
                    } else {
                        Log.w("EventDetailsActivity", "Error fetching attendance data", task.getException());
                        Toast.makeText(this, "Error fetching attendance data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void populateAttendanceList(List<Attendance> attendanceData) {
        attendanceAdapter = new AttendanceAdapter(attendanceData, db);
        attendanceRecyclerView.setAdapter(attendanceAdapter);
    }
}