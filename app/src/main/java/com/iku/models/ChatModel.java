package com.iku.models;

public class ChatModel {

    private String message;
    private String UID;

    private String userName, type, imageUrl;

    private long timestamp;

    private ChatModel() {
    }

    private ChatModel(String message, long timestamp, String UID, String userName, String type, String imageUrl) {
        this.message = message;
        this.UID = UID;
        this.timestamp = timestamp;
        this.userName = userName;
        this.type = type;
        this.imageUrl = imageUrl;
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
}
