package com.iku.models;

import com.google.firebase.database.PropertyName;

public class FeatureUpvoteModel {

    @PropertyName("title")
    private String title;
    @PropertyName("description")
    private String description;
    @PropertyName("image")
    private String image;
    @PropertyName("timestamp")
    private long timestamp;
    @PropertyName("upvote_count")
    private long upvote_count;

    public FeatureUpvoteModel() {
    }

    public FeatureUpvoteModel(String title, String description, String image, long timestamp, long upvote_count) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.timestamp = timestamp;
        this.upvote_count = upvote_count;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getUpvote_count() {
        return upvote_count;
    }
}
