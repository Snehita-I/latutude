package com.iku.models;

import java.util.List;

public class FeedbackImageModel {

    List<String> attachments;
    String subject, html;

    public FeedbackImageModel() {

    }

    public FeedbackImageModel(List<String> asList, String subject, String html) {
        this.attachments = asList;
        this.subject = subject;
        this.html = html;
    }


    public String getSubject() {
        return subject;
    }

    public String getHtml() {
        return html;
    }

    public List<String> getAttachments() {
        return attachments;
    }
}
