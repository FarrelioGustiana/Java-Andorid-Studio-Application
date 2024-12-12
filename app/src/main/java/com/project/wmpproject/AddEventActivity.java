package com.project.wmpproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.wmpproject.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText,  locationEditText;
    private ImageView eventImageView;
    private Button addButton;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private EditText dateEditText, timeEditText;
    private Calendar selectedDate;
    private ImageView dateTimeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        titleEditText = findViewById(R.id.addEventTitle);
        descriptionEditText = findViewById(R.id.addEventDescription);
        dateEditText = findViewById(R.id.addEventDate);
        timeEditText = findViewById(R.id.addEventTime);
        locationEditText = findViewById(R.id.addEventLocation);
        eventImageView = findViewById(R.id.addEventImage);
        addButton = findViewById(R.id.addEventButton);

        eventImageView.setImageResource(R.drawable.ic_camera);

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

        selectedDate = Calendar.getInstance(); // Initialize with current date

        dateEditText.setOnClickListener(v -> showDatePickerDialog());
        timeEditText.setOnClickListener(v -> showTimePickerDialog());

        eventImageView.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        addButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageAndAddEvent();
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });
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
                    updateDateEditText();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDate.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute1);
                    updateTimeEditText();
                }, hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    private void updateDateEditText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDate.getTime());
        dateEditText.setText(formattedDate);
    }

    private void updateTimeEditText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedTime = timeFormat.format(selectedDate.getTime());
        timeEditText.setText(formattedTime);
    }


    private void uploadImageAndAddEvent() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageName = "event_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        StorageReference imageRef = storageRef.child("events/" + imageName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(this::addEventToFirestore))
                .addOnFailureListener(e -> {
                    Log.w("AddEventActivity", "Error uploading image", e);
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }

    private void addEventToFirestore(Uri uri) {
        String imageUrl = uri.toString();
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String dateTime = dateEditText.getText().toString() + "T" + timeEditText.getText().toString();  // Get the date and time string from the DateInput
        String location = locationEditText.getText().toString();

        String eventId = UUID.randomUUID().toString();

        Event newEvent = new Event(title, description, dateTime, location, imageUrl, eventId);

        db.collection("events")
                .document(eventId)
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("AddEventActivity", "Error adding event", e);
                    Toast.makeText(this, "Error adding event", Toast.LENGTH_SHORT).show();
                });
    }
}