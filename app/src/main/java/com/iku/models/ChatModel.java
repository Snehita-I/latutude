package com.iku.models;

import java.util.ArrayList;

public class ChatModel {

    private String message;
    private String UID;

    private String userName, type, imageUrl;

    private int upvoteCount;

    private long timestamp;

    private ArrayList<String> userUIDs;

    private ChatModel() {
    }

    private ChatModel(String message, long timestamp, String UID, String userName, String type, int upvoteCount, String imageUrl, ArrayList<String> userUIDs) {
        this.message = message;
        this.UID = UID;
        this.timestamp = timestamp;
        this.userName = userName;
        this.type = type;
        this.imageUrl = imageUrl;
        this.upvoteCount = upvoteCount;
        this.userUIDs = userUIDs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getimageUrl() {
        return imageUrl;
    }

    public void setimageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public ArrayList<String> getUserUIDs() {
        return userUIDs;
    }

    public void setUserUIDs(ArrayList<String> userUIDs) {
        this.userUIDs = userUIDs;
    }
}
