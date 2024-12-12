package com.project.wmpproject;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.wmpproject.model.Event;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText, dateTimeEditText, locationEditText;
    private ImageView eventImageView;
    private Button saveChangesButton;
    private FirebaseFirestore db;
    private String eventId;
    private Uri selectedImageUri; // To store the URI of the new image
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private Calendar selectedDate;
    private String prevImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        titleEditText = findViewById(R.id.editEventTitle);
        descriptionEditText = findViewById(R.id.editEventDescription);
        dateTimeEditText = findViewById(R.id.editEventDateTime);
        locationEditText = findViewById(R.id.editEventLocation);
        eventImageView = findViewById(R.id.editEventImage);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Initialize the ActivityResultLauncher
        pickMedia = registerForActivityResult(new PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                selectedImageUri = uri;
                eventImageView.setImageURI(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        fetchEventData(eventId);

        eventImageView.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        saveChangesButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                // If a new image is selected, upload it
                uploadImageAndSaveChanges();
            } else {
                // Otherwise, update the event with existing image URL
                saveChanges();
            }
        });

        selectedDate = Calendar.getInstance();
        dateTimeEditText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year1);
                    selectedDate.set(Calendar.MONTH, monthOfYear);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeEditText();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateTimeEditText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDate.getTime());
        dateTimeEditText.setText(formattedDate);
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
                            Log.d("EditEventActivity", "Event not found");
                            finish();
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
        this.prevImageUrl = event.getImageUrl();
        Picasso.get().load(event.getImageUrl()).into(eventImageView);
    }

    private void uploadImageAndSaveChanges() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageName = "event_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        StorageReference imageRef = storageRef.child("events/" + imageName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the event with the new image URL
                    String imageUrl = uri.toString();
                    String title = titleEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();
                    String dateTime = dateTimeEditText.getText().toString();
                    String location = locationEditText.getText().toString();

                    Event updatedEvent = new Event(title, description, dateTime, location, imageUrl, eventId);

                    db.collection("events").document(eventId)
                            .set(updatedEvent)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            })
                            .addOnFailureListener(e -> Log.w("EditEventActivity", "Error updating event", e));
                }))
                .addOnFailureListener(e -> {
                    Log.w("EditEventActivity", "Error uploading image", e);
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChanges() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String dateTime = dateTimeEditText.getText().toString();
        String location = locationEditText.getText().toString();
        // Use the existing image URL when no new image is selected
        String imageUrl = prevImageUrl;

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