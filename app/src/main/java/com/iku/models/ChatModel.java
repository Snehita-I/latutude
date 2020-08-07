package com.iku.models;

public class ChatModel {

    private String itemID;
    private String message, UID;

    private long timestamp;

    private ChatModel() {
    }

    private ChatModel(String message, long timestamp, String UID,String itemID) {
        this.message = message;
        this.UID = UID;
        this.timestamp = timestamp;
        this.itemID = itemID;
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

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}
