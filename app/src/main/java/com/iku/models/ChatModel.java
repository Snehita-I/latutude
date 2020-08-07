package com.iku.models;

public class ChatModel {

    private String message, UID;

    private long timestamp;

    private ChatModel() {
    }

    private ChatModel(String message, long timestamp, String UID) {
        this.message = message;
        this.UID = UID;
        this.timestamp = timestamp;
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
}
