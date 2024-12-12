package com.project.wmpproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.wmpproject.R;
import com.project.wmpproject.model.Attendance;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<Attendance> attendanceList;
    private FirebaseFirestore db;
    private SimpleDateFormat timeFormat;

    public AttendanceAdapter(List<Attendance> attendanceList, FirebaseFirestore db) {
        this.attendanceList = attendanceList;
        this.db = db;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        String userId = attendance.userId;

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String username = doc.getString("username");
                String profileImage = doc.getString("profileImageUrl");
                Timestamp checkInTime = (Timestamp) attendance.checkinTime;

                holder.usernameTextView.setText(username);

                if (profileImage != null) {
                    Picasso.get()
                            .load(profileImage)
                            .placeholder(R.drawable.ic_default_profile) // Placeholder image
                            .error(R.drawable.ic_default_profile) // Error image
                            .into(holder.profileImageView);
                }

                if (checkInTime != null) {
                   // Convert Timestamp to Date
                    Date checkInDate = checkInTime.toDate();
                    String formattedTime = timeFormat.format(checkInDate);
                    holder.checkinTimeTextView.setText("Checked in at " + formattedTime);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(holder.itemView.getContext(),
                    "Error fetching user data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView usernameTextView;
        TextView checkinTimeTextView;

        AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            checkinTimeTextView = itemView.findViewById(R.id.checkinTimeTextView);
        }
    }
}