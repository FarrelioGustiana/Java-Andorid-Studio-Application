package com.project.wmpproject.model;

public class Event {
    private String title;
    private String description;
    private String dateTime;
    private String location;
    private String imageUrl;
    private String eventId;

    public Event() {}

    public Event(String title, String description, String dateTime, String location, String imageUrl) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public Event(String title, String description, String dateTime, String location, String imageUrl, String eventId) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.imageUrl = imageUrl;
        this.eventId = eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDateTime() { return dateTime; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
    public String getEventId() { return eventId; }
}
