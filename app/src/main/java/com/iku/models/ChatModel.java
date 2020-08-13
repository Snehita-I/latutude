package com.iku.models;

public class ChatModel {

    private String message;
    private String UID;

    private String userName;

    private long timestamp;

    private ChatModel() {
    }

    private ChatModel(String message, long timestamp, String UID,String userName) {
        this.message = message;
        this.UID = UID;
        this.timestamp = timestamp;
        this.userName = userName;
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
}
