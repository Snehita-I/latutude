package com.iku.models;

import com.google.firebase.firestore.PropertyName;

public class FeedbackImageModel {

    @PropertyName("subject")
    private String subject;
    @PropertyName("html")
    private String html;

    public FeedbackImageModel() {
    }

    public FeedbackImageModel(String subject, String html) {
        this.subject = subject;
        this.html = html;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}
