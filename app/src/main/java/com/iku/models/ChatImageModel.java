package com.iku.models;

public class ChatImageModel {
    private String mImageUrl, mName;

    public ChatImageModel() {

    }

    public ChatImageModel(String imageUrl, String name) {
        if (name.trim().equals("")) {
            mName = "NO Name";
        } else
            mName = name;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}