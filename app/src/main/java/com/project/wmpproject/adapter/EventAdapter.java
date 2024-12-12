package com.project.wmpproject.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.wmpproject.EventDetailsActivity;
import com.project.wmpproject.R;
import com.project.wmpproject.model.Event;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventDescription, eventDateTime, eventLocation;
        Button viewDetailsButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }

        void bind(Event event) {
            Picasso.get().load(event.getImageUrl()).into(eventImage);
            eventTitle.setText(event.getTitle());
            eventDescription.setText(event.getDescription());
            eventDateTime.setText(event.getDateTime());
            eventLocation.setText(event.getLocation());

            viewDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String eventId = event.getEventId();
                    Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
                    intent.putExtra("eventId", eventId); // Assuming you have the eventId available
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}