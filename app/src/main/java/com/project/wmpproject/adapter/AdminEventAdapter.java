package com.project.wmpproject.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.project.wmpproject.EditEventActivity;
import com.project.wmpproject.R;
import com.project.wmpproject.model.Event;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    public List<Event> events;
    private FirebaseFirestore db;

    public AdminEventAdapter() {

    }

    public AdminEventAdapter(List<Event> events) {
        this.events = events;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_admin, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        Picasso.get().load(event.getImageUrl()).into(holder.eventImage);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        holder.eventDateTime.setText(event.getDateTime());
        holder.eventLocation.setText(event.getLocation());

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditEventActivity.class);
            intent.putExtra("eventId", event.getEventId());
            v.getContext().startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> deleteEvent(event.getEventId(), position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void deleteEvent(String eventId, int position) {

        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Event deleted successfully!
                    events.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> Log.w("EventAdapter", "Error deleting event", e));
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventDescription, eventDateTime, eventLocation;
        Button editButton, deleteButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}